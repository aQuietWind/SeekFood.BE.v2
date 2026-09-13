CREATE DATABASE if not exists seek_food_meal;
USE seek_food_meal;
drop table if exists `meal`;
CREATE TABLE `meal` (
                        `meal_id` bigint PRIMARY KEY COMMENT '餐品id',
                        `merchant_id` bigint NOT NULL COMMENT '所属商家id',
                        `meal_name` varchar(15) NOT NULL COMMENT '餐品名称',
                        `meal_price` double not null COMMENT '餐品价格',
                        `meal_last_price` double COMMENT '餐品上次价格',
                        `meal_description` varchar(300) COMMENT '餐品介绍',
                        `meal_show_image_addr` varchar(50) COMMENT '餐品展示照片',
                        `meal_content` varchar(100) not null COMMENT '餐品内容',
                        `meal_type` int not null default 0 COMMENT '餐品类型',
                        `meal_sales_volume` int not null default 0 COMMENT '餐品销售总数',
                        `next_discount_time` datetime COMMENT '下次打折时间',
                        `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                        `is_sell` boolean NOT NULL DEFAULT false COMMENT '是否在售',
                        `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否删除',
                        INDEX merchant_index(merchant_id,is_sell,is_delete),
                        INDEX merchant_type_index(merchant_id,meal_type,is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐品表';

