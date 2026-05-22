package com.coolai.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limit Filter - In-memory rate limiting (Redis version requires spring-data-redis-reactive)
 * Uses simple in-memory sliding window for development
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;

    // In-memory rate limit counters (per client per window)
    private final ConcurrentHashMap<String, RateLimitWindow> minuteWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitWindow> hourWindows = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = getClientId(request);

        // Clean up expired windows periodically
        long now = System.currentTimeMillis();

        RateLimitWindow minuteWindow = minuteWindows.computeIfAbsent(clientId,
                k -> new RateLimitWindow(now, new AtomicInteger(0)));
        RateLimitWindow hourWindow = hourWindows.computeIfAbsent(clientId,
                k -> new RateLimitWindow(now, new AtomicInteger(0)));

        // Reset window if expired
        if (now - minuteWindow.startTime > 60_000) {
            minuteWindow.startTime = now;
            minuteWindow.count.set(0);
        }
        if (now - hourWindow.startTime > 3_600_000) {
            hourWindow.startTime = now;
            hourWindow.count.set(0);
        }

        // Check limits
        if (minuteWindow.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            return setErrorResponse(exchange, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded (per minute)");
        }
        if (hourWindow.count.incrementAndGet() > MAX_REQUESTS_PER_HOUR) {
            return setErrorResponse(exchange, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded (per hour)");
        }

        return chain.filter(exchange);
    }

    private String getClientId(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return "user:" + auth.substring(7, Math.min(auth.length(), 20));
        }
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return "ip:" + ip;
    }

    private Mono<Void> setErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"success\":false,\"error\":\"" + message + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return 2;
    }

    private static class RateLimitWindow {
        volatile long startTime;
        AtomicInteger count;

        RateLimitWindow(long startTime, AtomicInteger count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
