CREATE DATABASE if not exists seek_food_merchant;
USE seek_food_merchant;
drop table if exists  `merchant`;
CREATE TABLE `merchant` (
                            `merchant_id` bigint PRIMARY KEY COMMENT '用户id',
                            `merchant_name` varchar(15) NOT NULL COMMENT '店名',
                            `merchant_master_name` varchar(50) COMMENT '店主姓名',
                            `merchant_master_code` varchar(18) UNIQUE COMMENT '店主身份证号',
                            `merchant_master_image_addr` varchar(50) UNIQUE COMMENT '店主照片',
                            `merchant_master_phone_number` varchar(30) NOT NULL UNIQUE COMMENT '店主手机号，也是登录用的手机号',
                            `merchant_home_image_addr` varchar(50) COMMENT '店家营业证明照片',
                            `merchant_proof_image_addr` JSON NOT NULL COMMENT '店家营业证明照片',
                            `merchant_show_image_addr` JSON NOT NULL COMMENT '店家展示照片',
                            `merchant_show_description` varchar(300) COMMENT '店家简介',
                            `merchant_addr` varchar(60) COMMENT '店家地址',
                            `merchant_lon` double COMMENT '店家经度',
                            `merchant_lat` double COMMENT '店家纬度',
                            `merchant_order_amount` int NOT NULL DEFAULT 0 COMMENT '完成的订单总数',
                            `merchant_first_comment_amount` int NOT NULL DEFAULT 0 COMMENT '拥有的一级评论数',
                            `merchant_like_amount` int NOT NULL DEFAULT 0 COMMENT '点赞数',
                            `merchant_collect_amount` int NOT NULL DEFAULT 0 COMMENT '收藏数',
                            `merchant_employee_amount` int NOT NULL DEFAULT 0 COMMENT '员工数',
                            `merchant_password` varchar(20) NOT NULL COMMENT '登录密码',
                            `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                            `is_qualified` boolean NOT NULL DEFAULT true COMMENT '是否有资格营业',
                            `is_open` boolean NOT NULL DEFAULT false COMMENT '是否被开业',
                            `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否删除',
                            INDEX phone_index(merchant_master_phone_number,is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

