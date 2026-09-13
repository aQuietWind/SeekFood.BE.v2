package com.seek.food.config.NativeConfig.CommonConfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;

// 仅普通Servlet微服务生效，WebFlux网关不会实例化这个类
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration
public class EsConfig {
    @Bean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        // 全局开启驼峰自动转下划线（官方标准API）
        mappingContext.setFieldNamingStrategy(new SnakeCaseFieldNamingStrategy());
        return mappingContext;
    }
}
