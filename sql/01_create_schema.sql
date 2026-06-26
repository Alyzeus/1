-- =====================================================================
-- 题目七：旅行社旅游系统
-- 01_create_schema.sql  建库建表（含主键/外键/CHECK/UNIQUE 完整性约束）
-- 环境：MySQL 8.0+  （CHECK 约束自 8.0.16 起强制生效）
-- 执行顺序：01 -> 02 -> 03 -> 04 -> 05
-- =====================================================================

DROP DATABASE IF EXISTS travel_agency;
CREATE DATABASE travel_agency CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travel_agency;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------
-- 1. 旅游线路表
-- ---------------------------------------------------------------
CREATE TABLE route (
    route_no     CHAR(4)      NOT NULL COMMENT '线路编号，如 L001',
    start_place  VARCHAR(50)  NOT NULL COMMENT '起点',
    end_place    VARCHAR(50)  NOT NULL COMMENT '终点',
    days         TINYINT UNSIGNED NOT NULL COMMENT '旅游天数',
    main_spots   VARCHAR(500) COMMENT '主要景点',
    PRIMARY KEY (route_no),
    CONSTRAINT chk_route_days CHECK (days >= 1)
) ENGINE=InnoDB COMMENT='旅游线路';

-- ---------------------------------------------------------------
-- 2. 旅游班次表（一条线路开设多个班次）
-- ---------------------------------------------------------------
CREATE TABLE batch (
    batch_no     CHAR(8)      NOT NULL COMMENT '班次编号，如 B2607001',
    route_no     CHAR(4)      NOT NULL COMMENT '所属线路编号',
    depart_date  DATE         NOT NULL COMMENT '出发日期',
    return_date  DATE         NULL     COMMENT '回程日期（为空时由触发器按线路天数推算）',
    standard     VARCHAR(200) COMMENT '旅游标准（食宿交通等级）',
    price        DECIMAL(10,2) NOT NULL COMMENT '报价（元/人）',
    discount     DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率 0~1，1为无折扣',
    PRIMARY KEY (batch_no),
    CONSTRAINT fk_batch_route FOREIGN KEY (route_no) REFERENCES route(route_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_batch_price    CHECK (price >= 0),
    CONSTRAINT chk_batch_discount CHECK (discount >= 0 AND discount <= 1),
    CONSTRAINT chk_batch_dates    CHECK (return_date IS NULL OR return_date >= depart_date)
) ENGINE=InnoDB COMMENT='旅游班次';

-- ---------------------------------------------------------------
-- 3. 导游表
-- ---------------------------------------------------------------
CREATE TABLE guide (
    guide_no    CHAR(6)     NOT NULL COMMENT '导游编号，如 G00001',
    id_card     CHAR(18)    NOT NULL COMMENT '身份证号',
    name        VARCHAR(30) NOT NULL COMMENT '姓名',
    gender      ENUM('男','女') NOT NULL COMMENT '性别',
    birth_date  DATE        COMMENT '出生日期',
    address     VARCHAR(100) COMMENT '住址',
    phone       VARCHAR(20) COMMENT '联系电话',
    languages   VARCHAR(50) COMMENT '语种，如：普通话、英语',
    glevel      VARCHAR(20) COMMENT '等级：初级/中级/高级/特级',
    PRIMARY KEY (guide_no),
    CONSTRAINT uk_guide_idcard UNIQUE (id_card),
    CONSTRAINT chk_guide_phone CHECK (phone IS NULL OR CHAR_LENGTH(phone) >= 7)
) ENGINE=InnoDB COMMENT='导游';

-- ---------------------------------------------------------------
-- 4. 宾馆表
-- ---------------------------------------------------------------
CREATE TABLE hotel (
    hotel_no      CHAR(6)      NOT NULL COMMENT '宾馆编号，如 H00001',
    hotel_name    VARCHAR(60)  NOT NULL COMMENT '宾馆名称',
    city          VARCHAR(50)  NOT NULL COMMENT '所在城市',
    star          TINYINT      COMMENT '星级 1~5',
    std_price     DECIMAL(8,2) COMMENT '标准房价（元/间·夜）',
    contact_name  VARCHAR(30)  COMMENT '联系人',
    contact_title VARCHAR(30)  COMMENT '职务',
    contact_addr  VARCHAR(100) COMMENT '联系地址',
    contact_phone VARCHAR(20)  COMMENT '联系电话',
    fax           VARCHAR(20)  COMMENT '传真',
    PRIMARY KEY (hotel_no),
    CONSTRAINT chk_hotel_star  CHECK (star BETWEEN 1 AND 5),
    CONSTRAINT chk_hotel_price CHECK (std_price >= 0),
    CONSTRAINT chk_hotel_phone CHECK (contact_phone IS NULL OR CHAR_LENGTH(contact_phone) >= 7)
) ENGINE=InnoDB COMMENT='宾馆';

-- ---------------------------------------------------------------
-- 5. 游客表
-- ---------------------------------------------------------------
CREATE TABLE tourist (
    tourist_no  CHAR(8)     NOT NULL COMMENT '游客编号，如 T0000001',
    id_card     CHAR(18)    NOT NULL COMMENT '身份证号',
    name        VARCHAR(30) NOT NULL COMMENT '姓名',
    gender      ENUM('男','女') NOT NULL COMMENT '性别',
    birth_date  DATE        COMMENT '出生日期',
    address     VARCHAR(100) COMMENT '住址',
    phone       VARCHAR(20) COMMENT '联系电话',
    PRIMARY KEY (tourist_no),
    CONSTRAINT uk_tourist_idcard UNIQUE (id_card),
    CONSTRAINT chk_tourist_phone CHECK (phone IS NULL OR CHAR_LENGTH(phone) >= 7)
) ENGINE=InnoDB COMMENT='游客';

-- ---------------------------------------------------------------
-- 6. 报名表（游客 m:n 线路，带出发/回程日期）
-- ---------------------------------------------------------------
CREATE TABLE registration (
    reg_no      INT          NOT NULL AUTO_INCREMENT COMMENT '报名编号',
    tourist_no  CHAR(8)      NOT NULL COMMENT '游客编号',
    route_no    CHAR(4)      NOT NULL COMMENT '报名线路',
    depart_date DATE         NOT NULL COMMENT '出发日期',
    return_date DATE         NOT NULL COMMENT '回程日期（按线路天数推算）',
    reg_date    DATE         NOT NULL DEFAULT (CURRENT_DATE) COMMENT '报名日期',
    PRIMARY KEY (reg_no),
    CONSTRAINT fk_reg_tourist FOREIGN KEY (tourist_no) REFERENCES tourist(tourist_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_reg_route FOREIGN KEY (route_no) REFERENCES route(route_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- 同一游客同一线路同一出发日期只能报名一次
    CONSTRAINT uk_reg UNIQUE (tourist_no, route_no, depart_date)
) ENGINE=InnoDB COMMENT='游客报名记录';

-- ---------------------------------------------------------------
-- 7. 旅游团表（一个班次组建多个团，人数 20~50，
--    actual_people 由触发器自动维护，建团时为 0）
-- ---------------------------------------------------------------
CREATE TABLE tour_group (
    group_no      CHAR(8)     NOT NULL COMMENT '团号，如 TG260701',
    batch_no      CHAR(8)     NOT NULL COMMENT '所属班次',
    group_name    VARCHAR(60) NOT NULL COMMENT '团名',
    actual_people INT         NOT NULL DEFAULT 0 COMMENT '实际人数（触发器自动维护）',
    contact_name  VARCHAR(30) COMMENT '联系人',
    contact_addr  VARCHAR(100) COMMENT '联系人住址',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    PRIMARY KEY (group_no),
    CONSTRAINT fk_group_batch FOREIGN KEY (batch_no) REFERENCES batch(batch_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_group_people CHECK (actual_people BETWEEN 0 AND 50),
    CONSTRAINT chk_group_phone CHECK (contact_phone IS NULL OR CHAR_LENGTH(contact_phone) >= 7)
) ENGINE=InnoDB COMMENT='旅游团';

-- ---------------------------------------------------------------
-- 8. 团员表（游客 m:n 旅游团）
-- ---------------------------------------------------------------
CREATE TABLE group_member (
    group_no   CHAR(8) NOT NULL COMMENT '团号',
    tourist_no CHAR(8) NOT NULL COMMENT '游客编号',
    join_date  DATE    NOT NULL DEFAULT (CURRENT_DATE) COMMENT '入团日期',
    PRIMARY KEY (group_no, tourist_no),
    CONSTRAINT fk_member_group FOREIGN KEY (group_no) REFERENCES tour_group(group_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_member_tourist FOREIGN KEY (tourist_no) REFERENCES tourist(tourist_no)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='旅游团团员名单';

-- ---------------------------------------------------------------
-- 9. 随团表（班次 m:n 导游，带随团起止日期）
-- ---------------------------------------------------------------
CREATE TABLE batch_guide (
    batch_no     CHAR(8) NOT NULL COMMENT '班次编号',
    guide_no     CHAR(6) NOT NULL COMMENT '导游编号',
    escort_start DATE    NOT NULL COMMENT '随团开始日期',
    escort_end   DATE    NOT NULL COMMENT '随团结束日期',
    PRIMARY KEY (batch_no, guide_no),
    CONSTRAINT fk_bg_batch FOREIGN KEY (batch_no) REFERENCES batch(batch_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bg_guide FOREIGN KEY (guide_no) REFERENCES guide(guide_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bg_date CHECK (escort_end >= escort_start)
) ENGINE=InnoDB COMMENT='导游随团安排';

-- ---------------------------------------------------------------
-- 10. 住宿表（班次 m:n 宾馆，带入住/退房日期、房间数）
-- ---------------------------------------------------------------
CREATE TABLE batch_hotel (
    batch_no   CHAR(8) NOT NULL COMMENT '班次编号',
    hotel_no   CHAR(6) NOT NULL COMMENT '宾馆编号',
    check_in   DATE    NOT NULL COMMENT '入住日期',
    check_out  DATE    NOT NULL COMMENT '退房日期',
    room_count INT     COMMENT '房间数',
    PRIMARY KEY (batch_no, hotel_no),
    CONSTRAINT fk_bh_batch FOREIGN KEY (batch_no) REFERENCES batch(batch_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bh_hotel FOREIGN KEY (hotel_no) REFERENCES hotel(hotel_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bh_date  CHECK (check_out >= check_in),
    CONSTRAINT chk_bh_rooms CHECK (room_count IS NULL OR room_count > 0)
) ENGINE=InnoDB COMMENT='班次住宿安排';

-- ---------------------------------------------------------------
-- 11. 保险单表（旅游团 1:1 保险，group_no 唯一 => 一团一保）
-- ---------------------------------------------------------------
CREATE TABLE insurance (
    policy_no CHAR(10)     NOT NULL COMMENT '保险单号，如 P260700001',
    group_no  CHAR(8)      NOT NULL COMMENT '投保旅游团团号',
    per_fee   DECIMAL(8,2) NOT NULL COMMENT '人均保险费（元/人）',
    ins_start DATE         NOT NULL COMMENT '保险期限起（默认班次出发日）',
    ins_end   DATE         NOT NULL COMMENT '保险期限止（默认班次回程日）',
    PRIMARY KEY (policy_no),
    CONSTRAINT uk_ins_group UNIQUE (group_no),
    CONSTRAINT fk_ins_group FOREIGN KEY (group_no) REFERENCES tour_group(group_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_ins_fee  CHECK (per_fee >= 0),
    CONSTRAINT chk_ins_date CHECK (ins_end >= ins_start)
) ENGINE=InnoDB COMMENT='旅游团保险单';
