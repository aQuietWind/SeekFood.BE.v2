CREATE DATABASE if not exists seek_food_promotion;
USE seek_food_promotion;
drop table if exists `merchant_login_promotion`;
CREATE TABLE `merchant_login_promotion` (
                                            `promotion_id` bigint COMMENT '活动d',
                                            `merchant_id` bigint NOT NULL COMMENT '发布该活动的商家id',
                                            `voucher_id` bigint COMMENT '发送的优惠券的id',
                                            `promotion_title` varchar(15) COMMENT '该活动的标题',
                                            `promotion_description` varchar(500) COMMENT '该活动的详细描述',
                                            `promotion_notice` varchar(200) COMMENT '该活动的注意事项',
                                            `participant_amount` bigint default 0 COMMENT '参与者的数量',
                                            `start_time` datetime NOT NULL COMMENT '活动开始时间',
                                            `end_time` datetime NOT NULL COMMENT '活动截止时间',
                                            `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                            PRIMARY KEY (`promotion_id`),
                                            INDEX `merchant_index`(`merchant_id`),
                                            INDEX `voucher_index`(`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家登陆即得优惠券活动表';
drop table if exists `merchant_grab_promotion`;
CREATE TABLE `merchant_grab_promotion` (
                                           `promotion_id` bigint COMMENT '活动d',
                                           `merchant_id` bigint NOT NULL COMMENT '发布该活动的商家id',
                                           `voucher_id` bigint COMMENT '发送的优惠券的id',
                                           `promotion_title` varchar(15) COMMENT '该活动的标题',
                                           `promotion_description` varchar(500) COMMENT '该活动的详细描述',
                                           `promotion_notice` varchar(200) COMMENT '该活动的注意事项',
                                           `voucher_origin_amount` bigint not null COMMENT '优惠券的原始数量',
                                           `voucher_now_amount` bigint default 0 COMMENT '优惠券的现有数量',
                                           `start_time` datetime NOT NULL COMMENT '活动开始时间',
                                           `end_time` datetime NOT NULL COMMENT '活动截止时间',
                                           `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                           PRIMARY KEY (`promotion_id`),
                                           INDEX `merchant_index`(`merchant_id`),
                                           INDEX `voucher_index`(`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家优惠券抢杀活动表';






























