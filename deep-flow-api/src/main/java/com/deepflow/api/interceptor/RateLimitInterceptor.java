package com.deepflow.api.interceptor;

import com.deepflow.core.ratelimit.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.deepflow.api.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String userId = getUserId(request);
        
        // 1. 키 생성 (IP 기반 필수, User 기반 선택)
        String ipKey = "rate_limit:ip:" + clientIp;
        String userKey = userId != null ? "rate_limit:user:" + userId : null;

        // 2. 요청 비용 계산 (API별 차등 적용)
        long cost = calculateCost(request);

        // 3. 페널티 대상 IP 확인 (악성 유저 헤더 추가)
        boolean isPenalty = rateLimiterService.isInPenaltyBox(clientIp);
        if (isPenalty) {
             response.addHeader("X-Rate-Limit-Penalty", "true");
        }

        // 4. IP Bucket 소모 시도
        Bucket ipBucket = rateLimiterService.resolveBucket(ipKey, isPenalty);
        ConsumptionProbe ipProbe = ipBucket.tryConsumeAndReturnRemaining(cost);

        if (ipProbe.isConsumed()) {
             // 5. IP 통과 시, 로그인 유저는 User Bucket으로 2차 검증
             if (userKey != null) {
                 Bucket userBucket = rateLimiterService.resolveBucket(userKey, false);
                 ConsumptionProbe userProbe = userBucket.tryConsumeAndReturnRemaining(cost);
                 
                 // User Bucket 초과 시 차단
                 if (!userProbe.isConsumed()) {
                     return handleRateLimitExceeded(response, userProbe.getNanosToWaitForRefill(), clientIp);
                 }
                 response.addHeader("X-Rate-Limit-User-Remaining", String.valueOf(userProbe.getRemainingTokens()));
             }
             
            // 모두 통과
            response.addHeader("X-Rate-Limit-Ip-Remaining", String.valueOf(ipProbe.getRemainingTokens()));
            return true;
        } else {
            // 6. IP Bucket 초과 시 위반 횟수 증가 및 차단
            rateLimiterService.incrementViolationCount(clientIp);
            return handleRateLimitExceeded(response, ipProbe.getNanosToWaitForRefill(), clientIp);
        }
    }

    // 초과 시 429 Too Many Requests 반환
    private boolean handleRateLimitExceeded(HttpServletResponse response, long nanosToWait, String key) throws IOException {
        long waitForRefill = nanosToWait / 1_000_000_000;
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
        return false;
    }

    private long calculateCost(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        String uri = request.getRequestURI();

        // 세션 시작: 무거운 작업 (비용 10)
        if (uri.equals("/api/v1/sessions/start")) {
            return 10;
        }
        
        // CUD: 비용 5, GET: 비용 1
        return switch (method) {
            case "GET" -> 1; 
            case "POST", "PUT", "DELETE" -> 5;
            default -> 1;
        };
    }

    private String getUserId(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal instanceof UsernamePasswordAuthenticationToken token && 
            token.getPrincipal() instanceof CustomUserDetails userDetails) {
            return String.valueOf(userDetails.getUserId());
        }
        return null;
    }

    // 클라이언트 실제 IP 추출
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
