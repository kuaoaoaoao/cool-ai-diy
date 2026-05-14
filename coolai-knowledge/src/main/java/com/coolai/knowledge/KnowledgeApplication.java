package com.coolai.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.coolai.knowledge", "com.coolai.common"})
@MapperScan("com.coolai.knowledge.mapper")
public class KnowledgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
    }
}
