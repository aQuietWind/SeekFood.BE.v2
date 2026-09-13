package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.ChatSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ChatSubConfig.class)
public @interface ChatImport {
}
