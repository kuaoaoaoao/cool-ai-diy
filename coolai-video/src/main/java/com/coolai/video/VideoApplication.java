package com.coolai.video;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.coolai.video", "com.coolai.common"})
@MapperScan("com.coolai.video.mapper")
public class VideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoApplication.class, args);
    }
}
