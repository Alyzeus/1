-- =====================================================================
-- 题目七：旅行社旅游系统 — PostgreSQL 建表DDL
-- =====================================================================

-- 启用 pgcrypto 扩展（密码哈希 / 认证必需）
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. 旅游线路表
-- ============================================================
DROP TABLE IF EXISTS batch_hotel CASCADE;
DROP TABLE IF EXISTS batch_guide CASCADE;
DROP TABLE IF EXISTS group_member CASCADE;
DROP TABLE IF EXISTS tour_group CASCADE;
DROP TABLE IF EXISTS insurance CASCADE;
DROP TABLE IF EXISTS registration CASCADE;
DROP TABLE IF EXISTS batch CASCADE;
DROP TABLE IF EXISTS route CASCADE;
DROP TABLE IF EXISTS tourist CASCADE;
DROP TABLE IF EXISTS guide CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;

CREATE TABLE route (
    route_no    CHAR(4)      NOT NULL,
    start_place VARCHAR(50)  NOT NULL,
    end_place   VARCHAR(50)  NOT NULL,
    days        SMALLINT     NOT NULL CHECK (days >= 1),
    main_spots  VARCHAR(500),
    PRIMARY KEY (route_no)
);
COMMENT ON TABLE  route        IS '旅游线路';
COMMENT ON COLUMN route.route_no    IS '线路编号，如 L001';
COMMENT ON COLUMN route.start_place IS '起点';
COMMENT ON COLUMN route.end_place   IS '终点';
COMMENT ON COLUMN route.days        IS '旅游天数';
COMMENT ON COLUMN route.main_spots  IS '主要景点';

-- ============================================================
-- 2. 旅游班次表
-- ============================================================
CREATE TABLE batch (
    batch_no    CHAR(8)      NOT NULL,
    route_no    CHAR(4)      NOT NULL,
    depart_date DATE         NOT NULL,
    return_date DATE,
    standard    VARCHAR(200),
    price       DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    discount    DECIMAL(3,2)  NOT NULL DEFAULT 1.00 CHECK (discount BETWEEN 0 AND 1),
    PRIMARY KEY (batch_no),
    CONSTRAINT fk_batch_route FOREIGN KEY (route_no) REFERENCES route(route_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_batch_dates CHECK (return_date IS NULL OR return_date >= depart_date)
);
COMMENT ON TABLE  batch       IS '旅游班次';
COMMENT ON COLUMN batch.batch_no    IS '班次编号，如 B2607001';
COMMENT ON COLUMN batch.route_no    IS '所属线路编号';
COMMENT ON COLUMN batch.depart_date IS '出发日期';
COMMENT ON COLUMN batch.return_date IS '回程日期';
COMMENT ON COLUMN batch.standard    IS '旅游标准';
COMMENT ON COLUMN batch.price       IS '报价（元/人）';
COMMENT ON COLUMN batch.discount    IS '折扣率 0~1，1为无折扣';

-- ============================================================
-- 3. 导游表
-- ============================================================
CREATE TABLE guide (
    guide_no    CHAR(6)     NOT NULL,
    id_card     CHAR(18)    NOT NULL,
    name        VARCHAR(30) NOT NULL,
    gender      VARCHAR(2)  NOT NULL CHECK (gender IN ('男', '女')),
    birth_date  DATE,
    address     VARCHAR(100),
    phone       VARCHAR(20),
    languages   VARCHAR(50),
    glevel      VARCHAR(20),
    PRIMARY KEY (guide_no),
    CONSTRAINT uk_guide_idcard UNIQUE (id_card),
    CONSTRAINT chk_guide_phone CHECK (phone IS NULL OR LENGTH(phone) >= 7)
);
COMMENT ON TABLE  guide       IS '导游';
COMMENT ON COLUMN guide.guide_no   IS '导游编号，如 G00001';
COMMENT ON COLUMN guide.id_card    IS '身份证号';
COMMENT ON COLUMN guide.name       IS '姓名';
COMMENT ON COLUMN guide.gender     IS '性别';
COMMENT ON COLUMN guide.birth_date IS '出生日期';
COMMENT ON COLUMN guide.address    IS '住址';
COMMENT ON COLUMN guide.phone      IS '联系电话';
COMMENT ON COLUMN guide.languages  IS '语种';
COMMENT ON COLUMN guide.glevel     IS '等级';

-- ============================================================
-- 4. 宾馆表
-- ============================================================
CREATE TABLE hotel (
    hotel_no      CHAR(6)      NOT NULL,
    hotel_name    VARCHAR(60)  NOT NULL,
    city          VARCHAR(50)  NOT NULL,
    star          SMALLINT     CHECK (star BETWEEN 1 AND 5),
    std_price     DECIMAL(8,2) CHECK (std_price >= 0),
    contact_name  VARCHAR(30),
    contact_title VARCHAR(30),
    contact_addr  VARCHAR(100),
    contact_phone VARCHAR(20),
    fax           VARCHAR(20),
    PRIMARY KEY (hotel_no),
    CONSTRAINT chk_hotel_phone CHECK (contact_phone IS NULL OR LENGTH(contact_phone) >= 7)
);
COMMENT ON TABLE  hotel       IS '宾馆';
COMMENT ON COLUMN hotel.hotel_no      IS '宾馆编号，如 H00001';
COMMENT ON COLUMN hotel.hotel_name    IS '宾馆名称';
COMMENT ON COLUMN hotel.city          IS '所在城市';
COMMENT ON COLUMN hotel.star          IS '星级 1~5';
COMMENT ON COLUMN hotel.std_price     IS '标准房价（元/间·夜）';
COMMENT ON COLUMN hotel.contact_name  IS '联系人';
COMMENT ON COLUMN hotel.contact_title IS '职务';
COMMENT ON COLUMN hotel.contact_addr  IS '联系地址';
COMMENT ON COLUMN hotel.contact_phone IS '联系电话';
COMMENT ON COLUMN hotel.fax           IS '传真';

-- ============================================================
-- 5. 游客表
-- ============================================================
CREATE TABLE tourist (
    tourist_no  CHAR(8)     NOT NULL,
    id_card     CHAR(18)    NOT NULL,
    name        VARCHAR(30) NOT NULL,
    gender      VARCHAR(2)  NOT NULL CHECK (gender IN ('男', '女')),
    birth_date  DATE,
    address     VARCHAR(100),
    phone       VARCHAR(20),
    PRIMARY KEY (tourist_no),
    CONSTRAINT uk_tourist_idcard UNIQUE (id_card),
    CONSTRAINT chk_tourist_phone CHECK (phone IS NULL OR LENGTH(phone) >= 7)
);
COMMENT ON TABLE  tourist IS '游客';
COMMENT ON COLUMN tourist.tourist_no IS '游客编号，如 T0000001';
COMMENT ON COLUMN tourist.id_card    IS '身份证号';
COMMENT ON COLUMN tourist.name       IS '姓名';
COMMENT ON COLUMN tourist.gender     IS '性别';
COMMENT ON COLUMN tourist.birth_date IS '出生日期';
COMMENT ON COLUMN tourist.address    IS '住址';
COMMENT ON COLUMN tourist.phone      IS '联系电话';

-- ============================================================
-- 6. 报名表
-- ============================================================
CREATE TABLE registration (
    reg_no      SERIAL   PRIMARY KEY,
    tourist_no  CHAR(8)  NOT NULL,
    route_no    CHAR(4)  NOT NULL,
    depart_date DATE     NOT NULL,
    return_date DATE     NOT NULL,
    reg_date    DATE     NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT fk_reg_tourist FOREIGN KEY (tourist_no) REFERENCES tourist(tourist_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_reg_route FOREIGN KEY (route_no) REFERENCES route(route_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uk_reg UNIQUE (tourist_no, route_no, depart_date)
);
COMMENT ON TABLE  registration       IS '游客报名记录';
COMMENT ON COLUMN registration.reg_no       IS '报名编号';
COMMENT ON COLUMN registration.tourist_no   IS '游客编号';
COMMENT ON COLUMN registration.route_no     IS '报名线路';
COMMENT ON COLUMN registration.depart_date  IS '出发日期';
COMMENT ON COLUMN registration.return_date  IS '回程日期';
COMMENT ON COLUMN registration.reg_date     IS '报名日期';

-- ============================================================
-- 7. 旅游团表
-- ============================================================
CREATE TABLE tour_group (
    group_no      CHAR(8)     NOT NULL,
    batch_no      CHAR(8)     NOT NULL,
    group_name    VARCHAR(60) NOT NULL,
    actual_people INT         NOT NULL DEFAULT 0 CHECK (actual_people BETWEEN 0 AND 50),
    contact_name  VARCHAR(30),
    contact_addr  VARCHAR(100),
    contact_phone VARCHAR(20),
    PRIMARY KEY (group_no),
    CONSTRAINT fk_group_batch FOREIGN KEY (batch_no) REFERENCES batch(batch_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_group_phone CHECK (contact_phone IS NULL OR LENGTH(contact_phone) >= 7)
);
COMMENT ON TABLE  tour_group IS '旅游团';
COMMENT ON COLUMN tour_group.group_no       IS '团号，如 TG260701';
COMMENT ON COLUMN tour_group.batch_no       IS '所属班次';
COMMENT ON COLUMN tour_group.group_name     IS '团名';
COMMENT ON COLUMN tour_group.actual_people  IS '实际人数';
COMMENT ON COLUMN tour_group.contact_name   IS '联系人';
COMMENT ON COLUMN tour_group.contact_addr   IS '联系人住址';
COMMENT ON COLUMN tour_group.contact_phone  IS '联系电话';

-- ============================================================
-- 8. 团员表
-- ============================================================
CREATE TABLE group_member (
    group_no   CHAR(8) NOT NULL,
    tourist_no CHAR(8) NOT NULL,
    join_date  DATE    NOT NULL DEFAULT CURRENT_DATE,
    PRIMARY KEY (group_no, tourist_no),
    CONSTRAINT fk_member_group   FOREIGN KEY (group_no) REFERENCES tour_group(group_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_member_tourist FOREIGN KEY (tourist_no) REFERENCES tourist(tourist_no)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
COMMENT ON TABLE  group_member IS '旅游团团员名单';
COMMENT ON COLUMN group_member.group_no    IS '团号';
COMMENT ON COLUMN group_member.tourist_no  IS '游客编号';
COMMENT ON COLUMN group_member.join_date   IS '入团日期';

-- ============================================================
-- 9. 随团表
-- ============================================================
CREATE TABLE batch_guide (
    batch_no     CHAR(8) NOT NULL,
    guide_no     CHAR(6) NOT NULL,
    escort_start DATE    NOT NULL,
    escort_end   DATE    NOT NULL,
    PRIMARY KEY (batch_no, guide_no),
    CONSTRAINT fk_bg_batch FOREIGN KEY (batch_no) REFERENCES batch(batch_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bg_guide FOREIGN KEY (guide_no) REFERENCES guide(guide_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bg_date CHECK (escort_end >= escort_start)
);
COMMENT ON TABLE  batch_guide IS '导游随团安排';

-- ============================================================
-- 10. 住宿表
-- ============================================================
CREATE TABLE batch_hotel (
    batch_no   CHAR(8) NOT NULL,
    hotel_no   CHAR(6) NOT NULL,
    check_in   DATE    NOT NULL,
    check_out  DATE    NOT NULL,
    room_count INT     CHECK (room_count IS NULL OR room_count > 0),
    PRIMARY KEY (batch_no, hotel_no),
    CONSTRAINT fk_bh_batch FOREIGN KEY (batch_no) REFERENCES batch(batch_no)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bh_hotel FOREIGN KEY (hotel_no) REFERENCES hotel(hotel_no)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bh_date CHECK (check_out >= check_in)
);
COMMENT ON TABLE  batch_hotel IS '班次住宿安排';

-- ============================================================
-- 11. 保险单表
-- ============================================================
CREATE TABLE insurance (
    policy_no CHAR(10)     NOT NULL,
    group_no  CHAR(8)      NOT NULL,
    per_fee   DECIMAL(8,2) NOT NULL CHECK (per_fee >= 0),
    ins_start DATE         NOT NULL,
    ins_end   DATE         NOT NULL CHECK (ins_end >= ins_start),
    PRIMARY KEY (policy_no),
    CONSTRAINT uk_ins_group UNIQUE (group_no),
    CONSTRAINT fk_ins_group FOREIGN KEY (group_no) REFERENCES tour_group(group_no)
        ON UPDATE CASCADE ON DELETE CASCADE
);
COMMENT ON TABLE  insurance IS '旅游团保险单';
COMMENT ON COLUMN insurance.policy_no IS '保险单号';
COMMENT ON COLUMN insurance.group_no  IS '投保旅游团团号';
COMMENT ON COLUMN insurance.per_fee   IS '人均保险费（元/人）';
COMMENT ON COLUMN insurance.ins_start IS '保险期限起';
COMMENT ON COLUMN insurance.ins_end   IS '保险期限止';
