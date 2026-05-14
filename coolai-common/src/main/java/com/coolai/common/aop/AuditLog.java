package com.coolai.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audit log annotation - marks methods for automatic audit logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Action name (e.g., "DELETE_KNOWLEDGE_BASE")
     */
    String action();

    /**
     * Resource type (e.g., "knowledge_base")
     */
    String resourceType() default "";
}
