CREATE DATABASE if not exists seek_food_interaction;
USE seek_food_interaction;
drop table if exists  `merchant_like_connection`;
CREATE TABLE `like_connection` (
                                            `connection_id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '该关联关系的id',
                                            `aim_id` bigint COMMENT '目标id',
                                            `account_id` bigint COMMENT '执行方的id',
                                            `update_time` datetime NOT NULL default now() COMMENT '更新时间',
                                            `type` int NOT NULL COMMENT '类型,0为商家,1为一级评论,2为二级评论',
                                            `is_like` boolean NOT NULL DEFAULT false COMMENT '是否点赞',
                                            unique key uk_like_key(aim_id,account_id),
                                            INDEX aim_index(aim_id),
                                            INDEX account_index(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞关系表';
drop table if exists  `merchant_collect_connection`;
CREATE TABLE `collect_connection` (
                                               `connection_id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '该关联关系的id',
                                               `aim_id` bigint COMMENT '目标id',
                                               `account_id` bigint COMMENT '执行方的id',
                                               `update_time` datetime NOT NULL default now() COMMENT '更新时间',
                                               `type` int NOT NULL COMMENT '类型,0为商家',
                                               `is_collect` boolean NOT NULL DEFAULT false COMMENT '是否收藏',
                                               unique key uk_collect_key(aim_id,account_id),
                                               INDEX aim_index(aim_id),
                                               INDEX account_index(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏关系表';