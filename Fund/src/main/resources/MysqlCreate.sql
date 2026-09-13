CREATE DATABASE if not exists seek_food_fund;
USE seek_food_fund;
drop table if exists `fund`;
CREATE TABLE `fund` (
                        `account_id` bigint NOT NULL COMMENT ' 账户id',
                        `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                        `fund_amount` double NOT NULL DEFAULT 0.00 COMMENT '金额量',
                        `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否被删除',
                        PRIMARY KEY (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='余额表';

drop table if exists `fund_order_record`;
CREATE TABLE `fund_order_record` (
                                     `record_id` bigint NOT NULL COMMENT '记录id',
                                     `account_id` bigint NOT NULL COMMENT '账户id',
                                     `order_id` bigint not null COMMENT '订单id',
                                     `order_description` varchar(100) COMMENT '本次订单的详细描述',
                                     `cost` double NOT NULL COMMENT '订单消费的金额量',
                                     `deadline` datetime NOT NULL COMMENT '该支付的有效期',
                                     `able_rollback_time` datetime COMMENT '用户可回滚的时间',
                                     `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                     `is_pay` boolean NOT NULL default false COMMENT '是否支付',
                                     `is_refund` boolean NOT NULL default false COMMENT '是否退款',
                                     PRIMARY KEY (`record_id`),
                                     UNIQUE KEY (`order_id`),
                                     INDEX `account_index`(`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单消费记录表';

#自动用于自动回滚资金并且自动插入退款的触发器
create trigger auto_refund
    after update
    on fund_order_record for each row
begin
    if new.is_refund=true and old.is_refund=false then
        update fund set fund_amount=fund_amount+new.cost where account_id=new.account_id;
        insert into fund_order_refund_record (record_id, account_id, order_id, refund_cost) values (new.record_id,new.account_id,new.order_id,new.cost);
    end if;
end;

drop table if exists `fund_order_refund_record`;
CREATE TABLE `fund_order_refund_record` (
                                            `record_id` bigint NOT NULL COMMENT '记录id',
                                            `account_id` bigint NOT NULL COMMENT '账户id',
                                            `order_id` bigint not null COMMENT '订单id',
                                            `refund_cost` double NOT NULL COMMENT '订单退款的金额量',
                                            `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                            PRIMARY KEY (`record_id`),
                                            UNIQUE KEY (`order_id`),
                                            INDEX `account_index`(`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单退款记录表';

drop table if exists `fund_recharge_record`;
CREATE TABLE `fund_recharge_record` (
                                        `record_id` bigint NOT NULL COMMENT '记录id',
                                        `account_id` bigint NOT NULL COMMENT '账户id',
                                        `recharge_description` varchar(100) COMMENT '本次充值的详细描述',
                                        `recharge_amount` double NOT NULL COMMENT '充值的金额量',
                                        `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                        PRIMARY KEY (`record_id`),
                                        INDEX `account_index`(`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单充值记录表';

drop table if exists `fund_withdraw_record`;
CREATE TABLE `fund_withdraw_record` (
                                        `record_id` bigint NOT NULL COMMENT '记录id',
                                        `account_id` bigint NOT NULL COMMENT '账户id',
                                        `withdraw_description` varchar(100) COMMENT '本次提现的详细描述',
                                        `withdraw_amount` double NOT NULL COMMENT '提现的金额量',
                                        `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                                        PRIMARY KEY (`record_id`),
                                        INDEX `account_index`(`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单提现记录表';































