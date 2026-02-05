package com.mofadanqing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@MapperScan("com.mofadanqing.mapper")
@EnableAsync
public class MofadanqingApplication {
    public static void main(String[] args) {
        SpringApplication.run(MofadanqingApplication.class, args);
    }
}
