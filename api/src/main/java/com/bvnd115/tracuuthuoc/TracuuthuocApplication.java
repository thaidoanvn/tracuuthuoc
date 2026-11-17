package com.bvnd115.tracuuthuoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TracuuthuocApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracuuthuocApplication.class, args);
    }
}
