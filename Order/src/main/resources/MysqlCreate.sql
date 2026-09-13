CREATE DATABASE if not exists seek_food_order;
USE seek_food_order;
drop table if exists `order`;
CREATE TABLE `order` (
                         `order_id` bigint COMMENT '订单id',
                         `user_id` bigint NOT NULL COMMENT '用户id',
                         `merchant_id` bigint NOT NULL COMMENT '商家id',
                         `rider_id` bigint COMMENT '骑手id',
                         `voucher_connection_id` bigint COMMENT '优惠券持有关系id',
                         `meal_id` bigint NOT NULL COMMENT '餐品id',
                         `meal_name` varchar(15) NOT NULL COMMENT '餐品名称',
                         `meal_price` double NOT NULL COMMENT '餐品价格',
                         `meal_description` varchar(300) COMMENT '餐品的详细描述',
                         `meal_show_image_addr` varchar(50) COMMENT '餐品的展示图片地址',
                         `meal_content` varchar(100) NOT NULL COMMENT '餐品的包含内容',
                         `meal_type` int NOT NULL COMMENT '餐品的类型',
                         `number` int NOT NULL COMMENT '下单数量',
                         `delivery_address` varchar(60) COMMENT '用户的运输地址',
                         `delivery_lon` double COMMENT '用户的运输经度',
                         `delivery_lat` double COMMENT '用户的运输纬度',
                         `origin_cost` double NOT NULL COMMENT '初始金额',
                         `discount_cost` double NOT NULL COMMENT '优惠券打折金额',
                         `rider_cost` double NOT NULL COMMENT '骑手运输所需金额',
                         `total_cost` double NOT NULL COMMENT '总共金额',
                         `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                         `complete_time` datetime COMMENT '完成时间',
                         `state_now` int NOT NULL default 0 COMMENT '现在所处状态，0代表还未被确认，1代表商家待确认，2代表商家待制作
                                                                    ,3代表骑手待接收订单，4代表骑手待运送订单到指定地点，5代表用户待接收订单,6代表用户已经签收订单',
                         `is_refund` boolean NOT NULL default false COMMENT '是否退款',
                         `is_lock` boolean NOT NULL default false COMMENT '是否被锁定,锁定后再也不能被更改状态和信息',
                         `is_ack` boolean NOT NULL default false COMMENT '是否被确认',
                         `is_complete` boolean NOT NULL default false COMMENT '是否完成',
                         PRIMARY KEY (`order_id`),
                         INDEX `merchant_index`(`merchant_id`),
                         INDEX `user_index`(`user_id`),
                         INDEX `rider_index`(`rider_id`),
                         INDEX `meal_index`(`meal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';































