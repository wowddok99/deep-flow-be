package com.deepflow.api.interceptor;

import com.deepflow.application.exception.ErrorCode;
import com.deepflow.application.port.out.ratelimit.RateLimiter;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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

    @Value("${app.rate-limit.session-start-cost:10}")
    private long sessionStartCost;

    @Value("${app.rate-limit.write-operation-cost:5}")
    private long writeOperationCost;

    @Value("${app.rate-limit.read-operation-cost:1}")
    private long readOperationCost;

    private static final String SESSION_START_URI = "/api/v1/sessions/start";

    private final RateLimiter rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String clientIp = getClientIp(request);
            String userId = getUserId(request);

            String ipKey = "rate_limit:ip:" + clientIp;
            String userKey = userId != null ? "rate_limit:user:" + userId : null;
            long cost = calculateCost(request);

            boolean isPenalty = rateLimiter.isInPenaltyBox(clientIp);
            if (isPenalty) {
                log.warn("페널티 박스 적용 중: IP={}", clientIp);
                response.addHeader("X-Rate-Limit-Penalty", "true");
            }

            // IP 기반 1차 검증
            Bucket ipBucket = rateLimiter.resolveBucket(ipKey, isPenalty);
            ConsumptionProbe ipProbe = ipBucket.tryConsumeAndReturnRemaining(cost);

            if (ipProbe.isConsumed()) {
                // IP 통과 시, 로그인 유저는 User Bucket으로 2차 검증
                if (userKey != null) {
                    Bucket userBucket = rateLimiter.resolveBucket(userKey, false);
                    ConsumptionProbe userProbe = userBucket.tryConsumeAndReturnRemaining(cost);

                    if (!userProbe.isConsumed()) {
                        log.warn("Rate limit 초과 (유저): IP={}, userId={}, URI={}", clientIp, userId, request.getRequestURI());
                        return handleRateLimitExceeded(response, userProbe.getNanosToWaitForRefill(), clientIp);
                    }
                    response.addHeader("X-Rate-Limit-User-Remaining", String.valueOf(userProbe.getRemainingTokens()));
                }

                response.addHeader("X-Rate-Limit-Ip-Remaining", String.valueOf(ipProbe.getRemainingTokens()));
                return true;
            } else {
                rateLimiter.incrementViolationCount(clientIp);
                log.warn("Rate limit 초과 (위반 누적): IP={}, userId={}, URI={}", clientIp, userId, request.getRequestURI());
                return handleRateLimitExceeded(response, ipProbe.getNanosToWaitForRefill(), clientIp);
            }
        } catch (Exception e) {
            log.error("Rate limit 검사 실패, fail-open 처리: {}", e.getMessage());
            return true;
        }
    }

    private boolean handleRateLimitExceeded(HttpServletResponse response, long nanosToWait, String key) throws IOException {
        // sendError() 를 쓰면 Tomcat 이 /error 로 forward → Spring Security 필터 재진입 → 401 로 변조됨.
        // 직접 응답 작성으로 우회하면서 다른 에러 응답들과 동일한 {success,error{code,message}} 포맷 유지.
        long waitForRefill = nanosToWait / 1_000_000_000;
        response.setStatus(ErrorCode.RATE_LIMIT_EXCEEDED.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        response.getWriter().write(
                "{\"success\":false,\"error\":{\"code\":\"" + ErrorCode.RATE_LIMIT_EXCEEDED.name()
                        + "\",\"message\":\"" + ErrorCode.RATE_LIMIT_EXCEEDED.getMessage() + "\"}}");
        return false;
    }

    private long calculateCost(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        String uri = request.getRequestURI();

        if (SESSION_START_URI.equals(uri)) {
            return sessionStartCost;
        }

        return switch (method) {
            case "POST", "PUT", "DELETE" -> writeOperationCost;
            default -> readOperationCost;
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
