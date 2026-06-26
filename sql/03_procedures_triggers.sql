-- =====================================================================
-- 题目七：旅行社旅游系统
-- 03_procedures_triggers.sql  存储过程（5个）与触发器（5个）
-- =====================================================================
USE travel_agency;
SET NAMES utf8mb4;

DELIMITER $$

-- =====================================================================
-- 触发器部分
-- =====================================================================

-- ---------------------------------------------------------------
-- 触发器1：插入班次时，回程日期为空则按线路天数自动推算，
--           并校验回程日期不早于出发日期
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_batch_bi $$
CREATE TRIGGER trg_batch_bi
BEFORE INSERT ON batch
FOR EACH ROW
BEGIN
    DECLARE v_days TINYINT;
    IF NEW.return_date IS NULL THEN
        SELECT days INTO v_days FROM route WHERE route_no = NEW.route_no;
        SET NEW.return_date = DATE_ADD(NEW.depart_date, INTERVAL v_days - 1 DAY);
    END IF;
    IF NEW.return_date < NEW.depart_date THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '回程日期不能早于出发日期';
    END IF;
END $$

-- ---------------------------------------------------------------
-- 触发器2：入团前校验团人数未达上限 50
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_member_bi $$
CREATE TRIGGER trg_member_bi
BEFORE INSERT ON group_member
FOR EACH ROW
BEGIN
    DECLARE v_people INT;
    SELECT actual_people INTO v_people FROM tour_group WHERE group_no = NEW.group_no;
    IF v_people >= 50 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该旅游团已满（上限50人），禁止入团';
    END IF;
END $$

-- ---------------------------------------------------------------
-- 触发器3：入团后旅游团实际人数自动 +1
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_member_ai $$
CREATE TRIGGER trg_member_ai
AFTER INSERT ON group_member
FOR EACH ROW
BEGIN
    UPDATE tour_group SET actual_people = actual_people + 1
    WHERE group_no = NEW.group_no;
END $$

-- ---------------------------------------------------------------
-- 触发器4：退团后旅游团实际人数自动 -1
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_member_ad $$
CREATE TRIGGER trg_member_ad
AFTER DELETE ON group_member
FOR EACH ROW
BEGIN
    UPDATE tour_group SET actual_people = actual_people - 1
    WHERE group_no = OLD.group_no;
END $$

-- ---------------------------------------------------------------
-- 触发器5：投保前校验"一团一保"（与 UNIQUE 约束双保险，给出友好提示）
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_insurance_bi $$
CREATE TRIGGER trg_insurance_bi
BEFORE INSERT ON insurance
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM insurance WHERE group_no = NEW.group_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '一个旅游团在旅游期间只能参加一次保险';
    END IF;
END $$

-- =====================================================================
-- 存储过程部分
-- =====================================================================

-- ---------------------------------------------------------------
-- 过程1：游客报名
-- 按身份证号查档，新游客自动生成编号建档；
-- 回程日期 = 出发日期 + 线路天数 - 1；重复报名报错
-- ---------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_tourist_register $$
CREATE PROCEDURE sp_tourist_register (
    IN p_id_card     CHAR(18),
    IN p_name        VARCHAR(30),
    IN p_gender      ENUM('男','女'),
    IN p_birth_date  DATE,
    IN p_address     VARCHAR(100),
    IN p_phone       VARCHAR(20),
    IN p_route_no    CHAR(4),
    IN p_depart_date DATE
)
BEGIN
    DECLARE v_tourist_no CHAR(8);
    DECLARE v_days TINYINT;

    -- 校验线路存在
    SELECT days INTO v_days FROM route WHERE route_no = p_route_no;
    IF v_days IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '报名失败：线路不存在';
    END IF;

    -- 游客查档，不存在则建档（编号 T + 7位流水；GET_LOCK 防并发冲突）
    SELECT tourist_no INTO v_tourist_no FROM tourist WHERE id_card = p_id_card;
    IF v_tourist_no IS NULL THEN
        DO GET_LOCK('travel.tourist_no_gen', 10);         -- 获取命名锁，防止并发编号冲突
        SELECT CONCAT('T', LPAD(IFNULL(MAX(CAST(SUBSTRING(tourist_no, 2) AS UNSIGNED)), 0) + 1, 7, '0'))
          INTO v_tourist_no FROM tourist;
        INSERT INTO tourist (tourist_no, id_card, name, gender, birth_date, address, phone)
        VALUES (v_tourist_no, p_id_card, p_name, p_gender, p_birth_date, p_address, p_phone);
        DO RELEASE_LOCK('travel.tourist_no_gen');         -- 释放锁
    END IF;

    -- 重复报名校验
    IF EXISTS (SELECT 1 FROM registration
               WHERE tourist_no = v_tourist_no AND route_no = p_route_no
                 AND depart_date = p_depart_date) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '报名失败：该游客已报名此线路此班期';
    END IF;

    INSERT INTO registration (tourist_no, route_no, depart_date, return_date)
    VALUES (v_tourist_no, p_route_no, p_depart_date,
            DATE_ADD(p_depart_date, INTERVAL v_days - 1 DAY));

    SELECT v_tourist_no AS 游客编号, '报名成功' AS 结果;
END $$

-- ---------------------------------------------------------------
-- 过程2：游客入团（人数上限由触发器把关，这里做存在性与重复校验）
-- ---------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_join_group $$
CREATE PROCEDURE sp_join_group (
    IN p_group_no   CHAR(8),
    IN p_tourist_no CHAR(8)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM tour_group WHERE group_no = p_group_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '入团失败：旅游团不存在';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM tourist WHERE tourist_no = p_tourist_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '入团失败：游客不存在';
    END IF;
    IF EXISTS (SELECT 1 FROM group_member
               WHERE group_no = p_group_no AND tourist_no = p_tourist_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '入团失败：该游客已在此团中';
    END IF;

    INSERT INTO group_member (group_no, tourist_no) VALUES (p_group_no, p_tourist_no);
    SELECT '入团成功' AS 结果,
           (SELECT actual_people FROM tour_group WHERE group_no = p_group_no) AS 当前团人数;
END $$

-- ---------------------------------------------------------------
-- 过程3：成团校验（20 <= 人数 <= 50）
-- ---------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_confirm_group $$
CREATE PROCEDURE sp_confirm_group (
    IN p_group_no CHAR(8)
)
BEGIN
    DECLARE v_people INT;
    SELECT actual_people INTO v_people FROM tour_group WHERE group_no = p_group_no;
    IF v_people IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '成团校验失败：旅游团不存在';
    ELSEIF v_people < 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '成团校验失败：人数不足20人，不能成团';
    ELSEIF v_people > 50 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '成团校验失败：人数超过50人上限';
    END IF;
    SELECT p_group_no AS 团号, v_people AS 实际人数, '成团成功' AS 结果;
END $$

-- ---------------------------------------------------------------
-- 过程4：团体投保
-- 校验已成团（>=20人）且未投保；保险期限自动取班次出发/回程日期
-- ---------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_buy_insurance $$
CREATE PROCEDURE sp_buy_insurance (
    IN p_policy_no CHAR(10),
    IN p_group_no  CHAR(8),
    IN p_per_fee   DECIMAL(8,2)
)
BEGIN
    DECLARE v_people INT;
    DECLARE v_start, v_end DATE;
    DECLARE v_policy_no CHAR(10);

    -- 保单号为空时自动生成（P + 9位流水）
    IF p_policy_no IS NULL OR p_policy_no = '' THEN
        SELECT CONCAT('P', LPAD(IFNULL(MAX(CAST(SUBSTRING(policy_no, 2) AS UNSIGNED)), 0) + 1, 9, '0'))
          INTO v_policy_no FROM insurance;
    ELSE
        SET v_policy_no = p_policy_no;
    END IF;

    SELECT g.actual_people, b.depart_date, b.return_date
      INTO v_people, v_start, v_end
    FROM tour_group g JOIN batch b ON b.batch_no = g.batch_no
    WHERE g.group_no = p_group_no;

    IF v_people IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '投保失败：旅游团不存在';
    END IF;
    IF v_people < 20 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '投保失败：该团尚未成团（不足20人）';
    END IF;
    -- "一团一保"由触发器 trg_insurance_bi 与 UNIQUE 约束共同把关

    INSERT INTO insurance (policy_no, group_no, per_fee, ins_start, ins_end)
    VALUES (v_policy_no, p_group_no, p_per_fee, v_start, v_end);

    SELECT v_policy_no AS 保险单号, p_group_no AS 团号,
           v_start AS 保险起, v_end AS 保险止, '投保成功' AS 结果;
END $$

-- ---------------------------------------------------------------
-- 过程5：团费结算
-- 应收团费 = 报价 × 折扣率 × 实际人数 + 人均保险费 × 实际人数
-- ---------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_calc_group_fee $$
CREATE PROCEDURE sp_calc_group_fee (
    IN  p_group_no CHAR(8),
    OUT p_total    DECIMAL(12,2)
)
BEGIN
    SELECT ROUND(b.price * b.discount * g.actual_people
                 + IFNULL(i.per_fee, 0) * g.actual_people, 2)
      INTO p_total
    FROM tour_group g
    JOIN batch b ON b.batch_no = g.batch_no
    LEFT JOIN insurance i ON i.group_no = g.group_no
    WHERE g.group_no = p_group_no;

    IF p_total IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '结算失败：旅游团不存在';
    END IF;
END $$

-- =====================================================================
-- 数据校验触发器：手机号 / 身份证号格式约束
-- =====================================================================

-- ---------------------------------------------------------------
-- 触发器6：游客表 INSERT/UPDATE 校验 —— 手机号 + 身份证号格式
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_tourist_biu $$
CREATE TRIGGER trg_tourist_biu
BEFORE INSERT ON tourist
FOR EACH ROW
BEGIN
    -- 手机号校验：必须为11位数字（1开头），或区号-号码格式
    IF NEW.phone IS NOT NULL AND NEW.phone != '' THEN
        IF NOT (NEW.phone REGEXP '^1[3-9][0-9]{9}$'
                OR NEW.phone REGEXP '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '游客手机号格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    -- 身份证号校验：18位，前17位数字 + 末位数字或X
    IF NEW.id_card IS NOT NULL AND NEW.id_card != '' THEN
        IF NOT (NEW.id_card REGEXP '^[0-9]{17}[0-9Xx]$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '游客身份证号格式不正确（应为18位，末位可为数字或X）';
        END IF;
    END IF;
END $$

-- ---------------------------------------------------------------
-- 触发器7：导游表 INSERT/UPDATE 校验 —— 手机号 + 身份证号格式
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_guide_biu $$
CREATE TRIGGER trg_guide_biu
BEFORE INSERT ON guide
FOR EACH ROW
BEGIN
    IF NEW.phone IS NOT NULL AND NEW.phone != '' THEN
        IF NOT (NEW.phone REGEXP '^1[3-9][0-9]{9}$'
                OR NEW.phone REGEXP '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '导游手机号格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    IF NEW.id_card IS NOT NULL AND NEW.id_card != '' THEN
        IF NOT (NEW.id_card REGEXP '^[0-9]{17}[0-9Xx]$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '导游身份证号格式不正确（应为18位，末位可为数字或X）';
        END IF;
    END IF;
END $$

-- ---------------------------------------------------------------
-- 触发器8：旅游团联系人电话校验
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_group_biu $$
CREATE TRIGGER trg_group_biu
BEFORE INSERT ON tour_group
FOR EACH ROW
BEGIN
    IF NEW.contact_phone IS NOT NULL AND NEW.contact_phone != '' THEN
        IF NOT (NEW.contact_phone REGEXP '^1[3-9][0-9]{9}$'
                OR NEW.contact_phone REGEXP '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '联系人电话格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
END $$

-- ---------------------------------------------------------------
-- 触发器9：宾馆联系人电话 / 传真校验
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_hotel_biu $$
CREATE TRIGGER trg_hotel_biu
BEFORE INSERT ON hotel
FOR EACH ROW
BEGIN
    IF NEW.contact_phone IS NOT NULL AND NEW.contact_phone != '' THEN
        IF NOT (NEW.contact_phone REGEXP '^1[3-9][0-9]{9}$'
                OR NEW.contact_phone REGEXP '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '宾馆联系电话格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    IF NEW.fax IS NOT NULL AND NEW.fax != '' THEN
        IF NOT (NEW.fax REGEXP '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '宾馆传真格式不正确（应为 区号-号码格式）';
        END IF;
    END IF;
END $$

DELIMITER ;
