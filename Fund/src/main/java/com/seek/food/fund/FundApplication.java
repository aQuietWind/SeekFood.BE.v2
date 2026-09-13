package com.seek.food.fund;

import com.seek.food.config.Import.CommonImport;
import com.seek.food.config.Import.FundImport;
import com.seek.food.config.Import.MQImport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MQImport
@CommonImport
@FundImport
public class FundApplication {
    public static void main(String[] args) {
        SpringApplication.run(FundApplication.class, args);
    }
}
