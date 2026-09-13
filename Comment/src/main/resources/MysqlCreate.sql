
CREATE DATABASE if not exists seek_food_comment;
USE seek_food_comment;
drop table if exists  `first_comment`;
CREATE TABLE `first_comment` (
                                 `first_comment_id` bigint PRIMARY KEY COMMENT '职员id',
                                 `user_id` bigint NOT NULL COMMENT '发布该评论的用户id',
                                 `order_id` bigint NOT NULL UNIQUE COMMENT '评论的订单id',
                                 `meal_id` bigint NOT NULL COMMENT '订单所点餐品的id',
                                 `meal_name` varchar(15) NOT NULL COMMENT '订单所点餐品的名字',
                                 `meal_content` varchar(100) NOT NULL COMMENT '订单所点餐品的内容',
                                 `merchant_id` bigint NOT NULL COMMENT '评论的商家id',
                                 `comment_image_addr` varchar(50) UNIQUE COMMENT '评论展示图片的地址',
                                 `comment_description` varchar(300) COMMENT '评论内容',
                                 `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                 `like_amount` int NOT NULL default 0 COMMENT '点赞数',
                                 `second_comment_amount` int NOT NULL default 0 COMMENT '二级评论数（子评论数）',
                                 `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否删除',
                                 INDEX merchant_index(merchant_id),
                                 INDEX user_index(user_id),
                                 INDEX order_index(order_id),
                                 INDEX meal_index(meal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一级评论表';

drop table if exists  `second_comment`;
CREATE TABLE `second_comment` (
                                  `second_comment_id` bigint PRIMARY KEY COMMENT '该子评论id',
                                  `first_comment_id` bigint NOT NULL COMMENT '一级评论id（父评论id）',
                                  `account_id` bigint NOT NULL COMMENT '发布该评论的账户id',
                                  `comment_image_addr` varchar(50) UNIQUE COMMENT '评论展示图片的地址',
                                  `comment_description` varchar(300) NOT NULL COMMENT '评论内容',
                                  `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                  `like_amount` int NOT NULL default 0 COMMENT '点赞数',
                                  `is_merchant_comment` boolean NOT NULL COMMENT '是否属于商家的回复',
                                  `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否删除',
                                  INDEX account_index(account_id),
                                  INDEX first_comment_index(first_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二级评论表';