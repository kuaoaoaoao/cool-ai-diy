package com.coolai.singing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.coolai.singing", "com.coolai.common"})
@MapperScan("com.coolai.singing.mapper")
public class SingingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SingingApplication.class, args);
    }
}
