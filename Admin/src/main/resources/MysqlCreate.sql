
CREATE DATABASE if not exists seek_food_admin;
USE seek_food_admin;
drop table if exists  `suggestion`;
CREATE TABLE `suggestion` (
                              `suggestion_id` bigint PRIMARY KEY COMMENT '职员id',
                              `account_id` bigint NOT NULL COMMENT '发布该建议的账户id',
                              `suggestion_image_addr` varchar(50) UNIQUE COMMENT '该建议图片的地址',
                              `suggestion_description` varchar(500) COMMENT '建议内容',
                              `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                              `is_ack` boolean NOT NULL default false COMMENT '是否确认查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='建议表';
