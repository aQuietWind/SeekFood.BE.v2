package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.OrderSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OrderSubConfig.class)
public @interface OrderImport {}
