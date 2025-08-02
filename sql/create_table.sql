# 数据库初始化

-- 创建库
create database if not exists my_db;

-- 切换库
use my_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '手机号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/vip',
    vipExpireTime datetime     null comment '会员过期时间',
    vipCode       varchar(128) null comment '会员兑换码',
    vipNumber     bigint       null comment '会员编号',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 热点数据表
CREATE TABLE `hot` (
                       `id` bigint(20) NOT NULL COMMENT 'ID',
                       `photo` Text DEFAULT NULL COMMENT '头图',
                       `title` varchar(255) DEFAULT NULL COMMENT '标题',
                       `content` text COMMENT '内容',
                       `thumb_num` int(11) DEFAULT 0 COMMENT '点赞数',
                       `favour_num` int(11) DEFAULT 0 COMMENT '收藏数',
                       `user_id` bigint(20) DEFAULT NULL COMMENT '创建用户ID',
                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                       PRIMARY KEY (`id`),
                       KEY `idx_user_id` (`user_id`),
                       KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热点表';



CREATE TABLE `information` (
                               `id` bigint(20) NOT NULL COMMENT 'id',
                               `photo_path` varchar(255) DEFAULT NULL COMMENT '头图',
                               `title` varchar(255) DEFAULT NULL COMMENT '标题',
                               `content` text COMMENT '内容',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                               PRIMARY KEY (`id`),
                               KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息表';


CREATE TABLE `spirit_detail` (
                                 `id` bigint(20) NOT NULL COMMENT 'id',
                                 `room_id` bigint(20) DEFAULT NULL COMMENT 'roomId',
                                 `name` varchar(255) DEFAULT NULL COMMENT '姓名',
                                 `stone_total` decimal(30,0) DEFAULT NULL COMMENT '灵石总数',
                                 `stone_level` varchar(255) DEFAULT NULL COMMENT '灵石阶级',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_information_id` (`information_id`),
                                 KEY `idx_room_id` (`room_id`),
                                 KEY `idx_is_delete` (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='矿主详情表';


CREATE TABLE `stone_statistics` (
                                    `id` bigint(20) NOT NULL COMMENT 'id',
                                    `one_total` decimal(30,0) DEFAULT NULL COMMENT '区间1总和（根据业务定义对应区间）',
                                    `two_total` decimal(30,0) DEFAULT NULL COMMENT '区间2总和',
                                    `three_total` decimal(30,0) DEFAULT NULL COMMENT '区间3总和',
                                    `four_total` decimal(30,0) DEFAULT NULL COMMENT '区间4总和',
                                    `five_total` decimal(30,0) DEFAULT NULL COMMENT '区间5总和',
                                    `six_total` decimal(30,0) DEFAULT NULL COMMENT '区间6总和',
                                    `seven_total` decimal(30,0) DEFAULT NULL COMMENT '区间7总和',
                                    `eight_total` decimal(30,0) DEFAULT NULL COMMENT '区间8总和',
                                    `nine_total` decimal(30,0) DEFAULT NULL COMMENT '区间9总和',
                                    `ten_total` decimal(30,0) DEFAULT NULL COMMENT '区间10总和',
                                    `total` decimal(30,0) DEFAULT NULL COMMENT '所有ore总和',
                                    `user_quantity` varchar(255) DEFAULT NULL COMMENT '矿主数量',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵石统计数据表';


CREATE TABLE `stone_user_detail` (
                                     `id` bigint(20) NOT NULL COMMENT 'id',
                                     `name` varchar(255) DEFAULT NULL COMMENT '姓名',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='灵石用户信息表';

CREATE TABLE `spirit_mine_description` (
                                           `id` bigint(20) NOT NULL COMMENT '主键ID',
                                           `name` varchar(255) DEFAULT NULL COMMENT '灵矿名字',
                                           `input_stone` varchar(255) DEFAULT NULL COMMENT '投入灵石',
                                           `output_stone` varchar(255) DEFAULT NULL COMMENT '产出灵石',
                                           `monthly_profit` varchar(255) DEFAULT NULL COMMENT '月利润',
                                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灵矿说明表';

CREATE TABLE `armor_work_shop` (
                                                                                            `id` bigint(20) NOT NULL COMMENT 'id',
                                                                                            `daily_totalr` double DEFAULT NULL COMMENT '护甲产出总量',
                                                                                            `won_armor` double DEFAULT NULL COMMENT '摇中护甲',
                                                                                            `cost_per_share` double DEFAULT NULL COMMENT '投入每份需要消费矿石',
                                                                                            `monthly_profit` double DEFAULT NULL COMMENT '月利润',
                                                                                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                                                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                                                            `is_delete` tinyint(1) DEFAULT 0 COMMENT '是否删除(0-未删除,1-已删除)',
                                                                                            PRIMARY KEY (`id`),
                                                                                            KEY `idx_create_time` (`create_time`)
                                                         ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护甲坊';

CREATE TABLE `ore_pledge` (
                              `id` bigint(20) NOT NULL COMMENT '主键ID',
                              `today_total_ore` double DEFAULT NULL COMMENT '今日产出总量',
                              `everyday_ore` double DEFAULT NULL COMMENT '每日矿石资源总量',
                              `yesterday_total_ore` double DEFAULT NULL COMMENT '较昨日',
                              `weekday_total_ore` double DEFAULT NULL COMMENT '较七日前',
                              `monthday_total_ore` double DEFAULT NULL COMMENT '较30日前',
                              `user_use_ore` double DEFAULT NULL COMMENT '玩家持有矿石总量',
                              `user_yesterday_ore` double DEFAULT NULL COMMENT '玩家持有量较昨日变化',
                              `immortal_hall_ore` double DEFAULT NULL COMMENT '神仙殿持有矿石总量',
                              `immortal_yesterday_ore` double DEFAULT NULL COMMENT '神仙殿持有量较昨日变化',
                              `salary_pool_ore` double DEFAULT NULL COMMENT '工资池持有矿石总量',
                              `salary_pool_yesterday_ore` double DEFAULT NULL COMMENT '工资池持有量较昨日变化',
                              `other_ore` double DEFAULT NULL COMMENT '其他矿石总量',
                              `other_yesterday_ore` double DEFAULT NULL COMMENT '其他矿石较昨日变化',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `is_delete` tinyint(1) DEFAULT '0' COMMENT '逻辑删除(0-未删除,1-已删除)',
                              PRIMARY KEY (`id`),
                              KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引，用于按时间查询',
                              KEY `idx_is_delete` (`is_delete`) COMMENT '逻辑删除索引，优化查询效率'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='矿石质押表';
//乌龟表
CREATE TABLE `turtle_dart` (
                               `id` bigint NOT NULL COMMENT 'id',
                               `turtle_level` varchar(255) DEFAULT NULL COMMENT '乌龟等级',
                               `turtle_size` double DEFAULT NULL COMMENT '乌龟数量',
                               `turtle_price` double DEFAULT NULL COMMENT '乌龟价格',
                               `turtle_multiple` varchar(255) DEFAULT NULL COMMENT '乌龟倍数',
                               `create_time` datetime datetime DEFAULT null comment '创建时间',
                               `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                               `is_delete` int DEFAULT NULL COMMENT '是否删除(0-未删除,1-已删除)',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='乌龟镖局表';

-- 热点点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '热点 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '热点点赞';

-- 热点收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '热点 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '热点收藏';
