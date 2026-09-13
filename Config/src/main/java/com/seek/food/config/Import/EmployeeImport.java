package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.EmployeeSubConfig;
import com.seek.food.config.AutoConfig.GatewaySubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EmployeeSubConfig.class)
public @interface EmployeeImport {}
