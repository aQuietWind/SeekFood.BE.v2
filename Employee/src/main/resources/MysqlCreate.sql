CREATE DATABASE if not exists seek_food_employee;
USE seek_food_employee;
drop table if exists  `employee`;
CREATE TABLE `employee` (
                            `employee_id` bigint PRIMARY KEY COMMENT '职员id',
                            `merchant_id` bigint NOT NULL COMMENT '所属商家id',
                            `employee_name` varchar(50) NOT NULL COMMENT '职员名称',
                            `employee_code` varchar(18) COMMENT '职员身份证号,可能一个职员在多家商家打工，故不为唯一',
                            `employee_phone_number` varchar(30) COMMENT '职员手机号',
                            `employee_addr` varchar(60) COMMENT '职员家庭住址',
                            `employee_person_image_addr` varchar(50) UNIQUE COMMENT '职员照片地址',
                            `employee_month_salary` int COMMENT '职员月薪',
                            `employee_year_salary` int COMMENT '职员年薪',
                            `employee_description` varchar(300) COMMENT '职员简介',
                            `employee_position_name` varchar(15) COMMENT '职员职位名称',
                            `employee_dep_name` varchar(15) COMMENT '职员所属部门名称',
                            `create_time` datetime NOT NULL default now() COMMENT '创建时间',
                            `is_resign` boolean NOT NULL DEFAULT false COMMENT '是否解雇',
                            `is_delete` boolean NOT NULL DEFAULT false COMMENT '是否删除',
                            INDEX merchant_index(merchant_id,is_delete),
                            INDEX merchant_name_index(merchant_id,employee_name,is_delete),
                            INDEX merchant_dep_index(merchant_id,employee_dep_name,is_delete),
                            INDEX merchant_resign_index(merchant_id,is_resign,is_delete)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职员表';