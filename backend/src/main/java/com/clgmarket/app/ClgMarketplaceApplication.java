package com.clgmarket.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClgMarketplaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClgMarketplaceApplication.class, args);
    }
}
