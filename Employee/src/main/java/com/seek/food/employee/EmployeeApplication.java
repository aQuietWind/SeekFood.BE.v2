package com.seek.food.employee;

import com.seek.food.config.Import.CommonImport;
import com.seek.food.config.Import.EmployeeImport;
import com.seek.food.config.Import.MQImport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@CommonImport
@MQImport
@EmployeeImport
@EnableScheduling
public class EmployeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeApplication.class, args);
    }

}
