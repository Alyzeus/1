-- =====================================================================
-- 题目七：旅行社旅游系统
-- 02_indexes_views.sql  二级索引与视图
-- =====================================================================
USE travel_agency;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------
-- 二级索引（主键、UNIQUE、外键索引已自动创建，以下针对高频查询补充）
-- 用存储过程检查索引是否存在，避免重复创建报错
-- ---------------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS create_index_if_not_exists$$
CREATE PROCEDURE create_index_if_not_exists(
    IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_cols VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index LIMIT 1
    ) THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_cols, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL create_index_if_not_exists('batch',        'idx_batch_route_date', 'route_no, depart_date');
CALL create_index_if_not_exists('tourist',      'idx_tourist_name',     'name');
CALL create_index_if_not_exists('guide',        'idx_guide_name',       'name');
CALL create_index_if_not_exists('hotel',        'idx_hotel_city_star',  'city, star');
CALL create_index_if_not_exists('tour_group',   'idx_group_batch',      'batch_no');
CALL create_index_if_not_exists('registration', 'idx_reg_route_date',   'route_no, depart_date');

DROP PROCEDURE IF EXISTS create_index_if_not_exists;

-- ---------------------------------------------------------------
-- 视图 1：班次完整信息（班次 + 线路 + 折后价）
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_batch_full AS
SELECT b.batch_no            AS 班次编号,
       r.route_no            AS 线路编号,
       CONCAT(r.start_place, ' → ', r.end_place) AS 线路,
       r.days                AS 旅游天数,
       r.main_spots          AS 主要景点,
       b.depart_date         AS 出发日期,
       b.return_date         AS 回程日期,
       b.standard            AS 旅游标准,
       b.price               AS 报价,
       b.discount            AS 折扣率,
       ROUND(b.price * b.discount, 2) AS 折后价
FROM batch b
JOIN route r ON r.route_no = b.route_no;

-- ---------------------------------------------------------------
-- 视图 2：旅游团总览（团 + 班次 + 线路 + 保险 + 应收团费）
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_group_overview AS
SELECT g.group_no            AS 团号,
       g.group_name          AS 团名,
       g.actual_people       AS 实际人数,
       g.contact_name        AS 联系人,
       g.contact_phone       AS 联系电话,
       b.batch_no            AS 班次编号,
       CONCAT(r.start_place, ' → ', r.end_place) AS 线路,
       b.depart_date         AS 出发日期,
       b.return_date         AS 回程日期,
       i.policy_no           AS 保险单号,
       i.per_fee             AS 人均保险费,
       ROUND(b.price * b.discount * g.actual_people
             + IFNULL(i.per_fee, 0) * g.actual_people, 2) AS 应收团费
FROM tour_group g
JOIN batch b ON b.batch_no = g.batch_no
JOIN route r ON r.route_no = b.route_no
LEFT JOIN insurance i ON i.group_no = g.group_no;

-- ---------------------------------------------------------------
-- 视图 3：团员名单（不含身份证号，供业务员打印出团名单，起脱敏作用）
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_group_roster AS
SELECT gm.group_no  AS 团号,
       g.group_name AS 团名,
       t.tourist_no AS 游客编号,
       t.name       AS 姓名,
       t.gender     AS 性别,
       t.phone      AS 联系电话,
       gm.join_date AS 入团日期
FROM group_member gm
JOIN tour_group g ON g.group_no = gm.group_no
JOIN tourist t    ON t.tourist_no = gm.tourist_no;

-- ---------------------------------------------------------------
-- 视图 4：导游随团安排
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_guide_schedule AS
SELECT gd.guide_no   AS 导游编号,
       gd.name       AS 导游姓名,
       gd.glevel     AS 等级,
       gd.languages  AS 语种,
       bg.batch_no   AS 班次编号,
       CONCAT(r.start_place, ' → ', r.end_place) AS 线路,
       bg.escort_start AS 随团开始,
       bg.escort_end   AS 随团结束
FROM batch_guide bg
JOIN guide gd ON gd.guide_no = bg.guide_no
JOIN batch b  ON b.batch_no = bg.batch_no
JOIN route r  ON r.route_no = b.route_no;

-- ---------------------------------------------------------------
-- 视图 5：宾馆接待班次情况
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_hotel_reception AS
SELECT h.hotel_no    AS 宾馆编号,
       h.hotel_name  AS 宾馆名称,
       h.city        AS 城市,
       h.star        AS 星级,
       bh.batch_no   AS 接待班次,
       bh.check_in   AS 入住日期,
       bh.check_out  AS 退房日期,
       bh.room_count AS 房间数
FROM batch_hotel bh
JOIN hotel h ON h.hotel_no = bh.hotel_no;
