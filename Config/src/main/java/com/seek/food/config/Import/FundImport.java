package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.FundSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(FundSubConfig.class)
public @interface FundImport {}