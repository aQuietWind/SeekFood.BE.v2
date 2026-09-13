CREATE DATABASE if not exists seek_food_voucher;
USE seek_food_voucher;


drop table if exists `merchant_voucher`;
CREATE TABLE `merchant_voucher` (
                                    `voucher_id` bigint COMMENT '优惠券id',
                                    `merchant_id` bigint NOT NULL COMMENT '发布该优惠券的商家id',
                                    `voucher_name` varchar(15) COMMENT '该优惠券的展示名称',
                                    `voucher_description` varchar(300) COMMENT '该优惠券的详细描述',
                                    `discount_cost` double NOT NULL COMMENT '打折金额',
                                    `min_cost` double NOT NULL COMMENT '优惠券可用的最小的消费金额',
                                    `start_time` datetime NOT NULL COMMENT '开始可使用时间',
                                    `end_time` datetime NOT NULL COMMENT '截止可使用时间',
                                    `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                    PRIMARY KEY (`voucher_id`),
                                    INDEX `merchant_index`(`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家优惠券表';


drop table if exists `voucher_connection`;
CREATE TABLE `voucher_connection` (
                                      `connection_id` bigint COMMENT '该持有关系id',
                                      `voucher_id` bigint COMMENT '优惠券id',
                                      `user_id` bigint NOT NULL COMMENT '持有该优惠券的用户id',
                                      `promotion_id` bigint NOT NULL COMMENT '获取该优惠券的活动id',
                                      `order_id` bigint COMMENT '使用该优惠券的订单id',
                                      `start_time` datetime NOT NULL COMMENT '开始可使用时间',
                                      `end_time` datetime NOT NULL COMMENT '截止可使用时间',
                                      `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                      `is_lock` boolean default false COMMENT '是否处于冻结状态',
                                      `is_use` boolean default false COMMENT '是否使用',
                                      PRIMARY KEY (`connection_id`),
                                      INDEX `user_index`(`user_id`),
                                      INDEX `order_index`(`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券持有表';































