package com.seek.food.config.AutoConfig;


import com.seek.food.config.NacosConfig.MQ.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
// 绑定当前组件对应的属性类
@EnableConfigurationProperties({UserExchangeConfig.class, MerchantExchangeConfig.class, EmployeeExchangeConfig.class
, MealExchangeConfig.class,FundExchangeConfig.class, DeadLetterExchangeConfig.class,VoucherExchangeConfig.class, PromotionExchangeConfig.class
,OrderExchangeConfig.class,RiderExchangeConfig.class,CommentExchangeConfig.class, InteractionExchangeConfig.class,ChatExchangeConfig.class, AdminExchangeConfig.class})
public class MQSubConfig {

    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public UserExchangeConfig userExchangeConfig(UserExchangeConfig userExchangeConfig) {
        return userExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public MerchantExchangeConfig merchantExchangeConfig(MerchantExchangeConfig merchantExchangeConfig) {
        return merchantExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public EmployeeExchangeConfig employeeExchangeConfig(EmployeeExchangeConfig employeeExchangeConfig) {
        return employeeExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public MealExchangeConfig mealExchangeConfig(MealExchangeConfig mealExchangeConfig) {
        return mealExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public FundExchangeConfig fundExchangeConfig(FundExchangeConfig fundExchangeConfig) {
        return fundExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public VoucherExchangeConfig voucherExchangeConfig(VoucherExchangeConfig voucherExchangeConfig) {
        return voucherExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public PromotionExchangeConfig promotionExchangeConfig(PromotionExchangeConfig promotionExchangeConfig) {
        return promotionExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public OrderExchangeConfig orderExchangeConfig(OrderExchangeConfig orderExchangeConfig) {
        return orderExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public RiderExchangeConfig riderExchangeConfig(RiderExchangeConfig riderExchangeConfig) {
        return riderExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public CommentExchangeConfig commentExchangeConfig(CommentExchangeConfig commentExchangeConfig) {
        return commentExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public InteractionExchangeConfig interactionExchangeConfig(InteractionExchangeConfig interactionExchangeConfig) {
        return interactionExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public ChatExchangeConfig chatExchangeConfig(ChatExchangeConfig chatExchangeConfig) {
        return chatExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public AdminExchangeConfig adminExchangeConfig(AdminExchangeConfig adminExchangeConfig) {
        return adminExchangeConfig;
    }
    @Bean
    @Lazy // 用到才实例化，启动不创建对象
    public DeadLetterExchangeConfig deadLetterExchangeConfig(DeadLetterExchangeConfig deadLetterExchangeConfig) {
        return deadLetterExchangeConfig;
    }

}
