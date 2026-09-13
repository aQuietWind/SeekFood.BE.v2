package com.seek.food.user;

import com.seek.food.config.Import.CommonImport;
import com.seek.food.config.Import.MQImport;
import com.seek.food.config.Import.UserImport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@UserImport
@CommonImport
@MQImport
@EnableScheduling
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
