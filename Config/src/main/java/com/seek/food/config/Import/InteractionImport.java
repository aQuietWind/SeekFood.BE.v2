package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.InteractionSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(InteractionSubConfig.class)
public @interface InteractionImport {
}
