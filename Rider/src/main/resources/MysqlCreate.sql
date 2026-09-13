CREATE DATABASE if not exists seek_food_rider;
USE seek_food_rider;
drop table if exists `rider`;
CREATE TABLE `rider` (
                         `rider_id` bigint COMMENT '餐品id',
                         `rider_name` varchar(50) NOT NULL COMMENT '骑手真实姓名',
                         `rider_code` varchar(18) not null COMMENT '骑手身份证号',
                         `rider_phone_number` varchar(30) not null COMMENT '骑手手机号',
                         `rider_password` varchar(20) not null COMMENT '骑手密码',
                         `rider_person_image_addr` varchar(50) COMMENT '骑手本人的展示照片',
                         `rider_sex` int not null COMMENT '骑手性别',
                         `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                         `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否删除',
                         PRIMARY KEY (rider_id),
                         UNIQUE KEY `uk_phone` (rider_phone_number),
                         INDEX phone_index(rider_phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骑手表';

