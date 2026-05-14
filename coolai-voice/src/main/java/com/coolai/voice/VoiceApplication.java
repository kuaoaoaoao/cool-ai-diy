package com.coolai.voice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.coolai.voice", "com.coolai.common"})
@MapperScan("com.coolai.voice.mapper")
public class VoiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceApplication.class, args);
    }
}
