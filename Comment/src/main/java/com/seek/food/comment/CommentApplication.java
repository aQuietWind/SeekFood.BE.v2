package com.seek.food.comment;

import com.seek.food.config.Import.CommentImport;
import com.seek.food.config.Import.CommonImport;
import com.seek.food.config.Import.MQImport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@MQImport
@CommonImport
@CommentImport
public class CommentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentApplication.class, args);
    }

}
