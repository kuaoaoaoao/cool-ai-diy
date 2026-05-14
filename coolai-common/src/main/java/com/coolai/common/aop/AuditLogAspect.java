package com.coolai.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Audit Log AOP Aspect - Automatically records operation logs
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, AuditLog auditLog, Object result) {
        recordLog(joinPoint, auditLog, result, null);
    }

    @AfterThrowing(pointcut = "@annotation(auditLog)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, AuditLog auditLog, Exception ex) {
        recordLog(joinPoint, auditLog, null, ex);
    }

    private void recordLog(JoinPoint joinPoint, AuditLog auditLog, Object result, Exception ex) {
        try {
            String action = auditLog.action();
            String resourceType = auditLog.resourceType();
            String userId = null;
            String ipAddress = null;

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                userId = authentication.getPrincipal().toString();
            }

            // Get IP address
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = getClientIp(request);
            }

            // Build details
            Map<String, Object> details = new HashMap<>();
            details.put("method", joinPoint.getSignature().toShortString());
            if (result != null) {
                details.put("result", "success");
            }
            if (ex != null) {
                details.put("result", "failure");
                details.put("error", ex.getMessage());
            }

            log.info("AuditLog: action={}, resourceType={}, userId={}, ip={}, details={}",
                    action, resourceType, userId, ipAddress, objectMapper.writeValueAsString(details));

            // In production, save to operation_logs table via OperationLogMapper
            // OperationLog operationLog = new OperationLog();
            // operationLog.setAction(action);
            // operationLog.setResourceType(resourceType);
            // operationLog.setUserId(userId);
            // operationLog.setIpAddress(ipAddress);
            // operationLog.setDetails(objectMapper.writeValueAsString(details));
            // operationLogMapper.insert(operationLog);

        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
