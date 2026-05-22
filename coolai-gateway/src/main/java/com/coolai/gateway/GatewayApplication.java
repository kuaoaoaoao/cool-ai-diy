package com.coolai.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Gateway 服务 - 基于 Spring Cloud Gateway (Reactive/Netty)
 * 
 * 注意: Gateway 是 Reactive 应用，不使用 Servlet 容器
 * 只扫描 common 中不依赖 Servlet 的包：
 *   - com.coolai.common.util    (JwtUtil, HashUtil, JsonUtil)
 *   - com.coolai.common.model   (ApiResponse, PageResult)
 *   - com.coolai.common.llm     (LlmGatewayService, MockLlmProvider)
 * 不扫描（依赖 Servlet/DataSource/Redis 等）：
 *   - com.coolai.common.config  (RedisConfig, RabbitMqConfig, MinioConfig, MybatisPlusConfig)
 *   - com.coolai.common.service (MinioStorageService)
 *   - com.coolai.common.exception (GlobalExceptionHandler - @RestControllerAdvice)
 *   - com.coolai.common.aop     (AuditLogAspect - HttpServletRequest)
 *   - com.coolai.common.mcp     (McpRegistry, McpTool - 不需要)
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RabbitAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration.class
})
@ComponentScan(basePackages = {
        "com.coolai.gateway",
        "com.coolai.common.util",
        "com.coolai.common.model",
        "com.coolai.common.llm"
})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
