package com.seek.food.config.Import;

import com.seek.food.config.AutoConfig.CommentSubConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CommentSubConfig.class)
public @interface CommentImport {
}
