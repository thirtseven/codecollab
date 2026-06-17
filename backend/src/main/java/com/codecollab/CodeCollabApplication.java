package com.codecollab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.codecollab.mapper")
@EnableAsync
@EnableScheduling
public class CodeCollabApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeCollabApplication.class, args);
    }
}
