package com.xidian.activities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class ActivitiesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitiesApplication.class, args);
    }

}
