package com.coolai.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limit Filter - Redis-based rate limiting with sliding window
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = getClientId(request);
        String minuteKey = "rate_limit:min:" + clientId;
        String hourKey = "rate_limit:hour:" + clientId;

        // Check minute limit
        String minuteCount = redisTemplate.opsForValue().get(minuteKey);
        if (minuteCount != null && Integer.parseInt(minuteCount) >= MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Rate limit exceeded (per minute)\"}");
            return;
        }

        // Check hour limit
        String hourCount = redisTemplate.opsForValue().get(hourKey);
        if (hourCount != null && Integer.parseInt(hourCount) >= MAX_REQUESTS_PER_HOUR) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Rate limit exceeded (per hour)\"}");
            return;
        }

        // Increment counters
        redisTemplate.opsForValue().increment(minuteKey);
        redisTemplate.expire(minuteKey, 60, TimeUnit.SECONDS);
        redisTemplate.opsForValue().increment(hourKey);
        redisTemplate.expire(hourKey, 3600, TimeUnit.SECONDS);

        filterChain.doFilter(request, response);
    }

    private String getClientId(HttpServletRequest request) {
        // Use user ID from auth header, or IP address
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return "user:" + auth.substring(7, Math.min(auth.length(), 20));
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }
}
