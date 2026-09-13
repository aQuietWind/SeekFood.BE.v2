package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.RiderSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RiderSubConfig.class)
public @interface RiderImport {}
