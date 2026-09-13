package com.seek.food.comment.Caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.seek.food.config.NacosConfig.Comment.CommentCaffeineConfig;
import com.seek.food.config.NacosConfig.Employee.EmployeeCaffeineConfig;
import com.seek.food.dto.Comment.FirstCommentDTO;
import com.seek.food.dto.Employee.EmployeeDTO;
import com.seek.food.util.Caffeine.JvmCaffeineParent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FirstCommentCaffeine extends JvmCaffeineParent<Long, FirstCommentDTO> {
    // 构造注入配置
    private final CommentCaffeineConfig commentCaffeineConfig;
    @Autowired
    public FirstCommentCaffeine(CommentCaffeineConfig commentCaffeineConfig) {
        this.commentCaffeineConfig = commentCaffeineConfig;
    }

    // 容器启动构建缓存
    @PostConstruct
    public void init() {
        super.CACHE = Caffeine.newBuilder()
                .maximumSize(commentCaffeineConfig.getFirstComment().getMaxSize())
                .expireAfterWrite(commentCaffeineConfig.getFirstComment().getExpireTime(), TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    // 容器销毁清理缓存
    @PreDestroy
    public void destroy() {
        super.CACHE.cleanUp();
        super.CACHE.invalidateAll();
    }
}
