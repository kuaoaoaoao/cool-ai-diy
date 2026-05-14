package com.coolai.tts;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.coolai.tts", "com.coolai.common"})
@MapperScan("com.coolai.tts.mapper")
public class TtsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TtsApplication.class, args);
    }
}
