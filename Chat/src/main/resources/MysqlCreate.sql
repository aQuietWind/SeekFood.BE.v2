
CREATE DATABASE if not exists seek_food_chat;
USE seek_food_chat;
drop table if exists  `chat_room`;
CREATE TABLE `chat_room` (
                             `room_id` bigint PRIMARY KEY COMMENT '聊天室id',
                             `order_id` bigint NOT NULL UNIQUE COMMENT '订单id',
                             `user_id` bigint NOT NULL COMMENT '用户id',
                             `merchant_id` bigint NOT NULL COMMENT '商家id',
                             `rider_id` bigint NOT NULL COMMENT '骑手id',
                             `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                             `ending_time` datetime NOT NULL COMMENT '默认的终止时间',
                             `is_complete` boolean NOT NULL default false COMMENT '该订单是否已经完成（即终止聊天）',
                             INDEX order_index(order_id),
                             INDEX user_index(user_id),
                             INDEX merchant_index(merchant_id),
                             INDEX rider_index(rider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天室表';

drop table if exists  `chat_record`;
CREATE TABLE `chat_record` (
                               `record_id` bigint PRIMARY KEY COMMENT '聊天室id',
                               `chat_room_id` bigint NOT NULL COMMENT '订单id',
                               `account_id` bigint NOT NULL COMMENT '用户id',
                               `account_type` int NOT NULL COMMENT '发言账户的类型,1为用户,2为商家,3为骑手',
                               `chat_description` varchar(500) NOT NULL COMMENT '该聊天记录的内容',
                               `chat_show_image_addr` varchar(50) COMMENT '该记录展示图片的地址',
                               `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                               `withdraw_deadline` datetime NOT NULL COMMENT '可以撤回的截止时间',
                               `is_withdraw` boolean NOT NULL default false COMMENT '该记录是否撤回',
                               INDEX room_index(chat_room_id),
                               INDEX account_index(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';
