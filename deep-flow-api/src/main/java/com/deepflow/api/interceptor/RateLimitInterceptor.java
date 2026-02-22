package com.deepflow.api.interceptor;

import com.deepflow.infra.ratelimit.RateLimiterService;
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

    private static final long SESSION_START_COST = 10;
    private static final long WRITE_OPERATION_COST = 5;
    private static final long READ_OPERATION_COST = 1;
    private static final String SESSION_START_URI = "/api/v1/sessions/start";

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String userId = getUserId(request);

        String ipKey = "rate_limit:ip:" + clientIp;
        String userKey = userId != null ? "rate_limit:user:" + userId : null;
        long cost = calculateCost(request);

        boolean isPenalty = rateLimiterService.isInPenaltyBox(clientIp);
        if (isPenalty) {
            response.addHeader("X-Rate-Limit-Penalty", "true");
        }

        // IP 기반 1차 검증
        Bucket ipBucket = rateLimiterService.resolveBucket(ipKey, isPenalty);
        ConsumptionProbe ipProbe = ipBucket.tryConsumeAndReturnRemaining(cost); // IP 버킷에서 cost만큼 토큰 소모

        if (ipProbe.isConsumed()) {
            // IP 통과 시, 로그인 유저는 User Bucket으로 2차 검증
            if (userKey != null) {
                Bucket userBucket = rateLimiterService.resolveBucket(userKey, false);
                ConsumptionProbe userProbe = userBucket.tryConsumeAndReturnRemaining(cost); // 유저 버킷에서 cost만큼 토큰 소모

                if (!userProbe.isConsumed()) {
                    return handleRateLimitExceeded(response, userProbe.getNanosToWaitForRefill(), clientIp); // 단순 429 반환 (위반 카운트 증가 없음)
                }
                response.addHeader("X-Rate-Limit-User-Remaining", String.valueOf(userProbe.getRemainingTokens()));
            }

            response.addHeader("X-Rate-Limit-Ip-Remaining", String.valueOf(ipProbe.getRemainingTokens()));
            return true;
        } else {
            // 위반 누적 → 50회 초과 시 다음 요청부터 페널티 (버킷 100→10)
            rateLimiterService.incrementViolationCount(clientIp);
            return handleRateLimitExceeded(response, ipProbe.getNanosToWaitForRefill(), clientIp); // 429 반환
        }
    }

    private boolean handleRateLimitExceeded(HttpServletResponse response, long nanosToWait, String key) throws IOException {
        long waitForRefill = nanosToWait / 1_000_000_000;
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests");
        return false;
    }

    private long calculateCost(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        String uri = request.getRequestURI();

        if (SESSION_START_URI.equals(uri)) {
            return SESSION_START_COST;
        }

        return switch (method) {
            case "POST", "PUT", "DELETE" -> WRITE_OPERATION_COST;
            default -> READ_OPERATION_COST;
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

    // 프록시 환경에서 실제 클라이언트 IP 추출
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For는 "client, proxy1, proxy2" 형태일 수 있음 → 첫 번째가 실제 IP
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
