package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.MQSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MQSubConfig.class)
public @interface MQImport { }
