package com.seek.food.rider;

import com.seek.food.config.Import.CommonImport;
import com.seek.food.config.Import.MQImport;
import com.seek.food.config.Import.RiderImport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@CommonImport
@MQImport
@RiderImport
@EnableScheduling
public class RiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiderApplication.class, args);
    }

}
