package com.coolai.deepfake;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.coolai.deepfake", "com.coolai.common"})
@MapperScan("com.coolai.deepfake.mapper")
public class DeepfakeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeepfakeApplication.class, args);
    }
}
