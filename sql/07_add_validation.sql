-- =====================================================================
-- 题目七：旅行社旅游系统
-- 07_add_validation.sql  增量升级：为已有数据库添加数据校验触发器
-- 适用场景：已经按 01~06 建库，执行本脚本追加手机号/身份证号格式约束
-- =====================================================================
USE travel_agency;
SET NAMES utf8mb4;

DELIMITER $$

-- ---------------------------------------------------------------
-- 游客表校验：手机号 + 身份证号（BEFORE INSERT）
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_tourist_biu $$
CREATE TRIGGER trg_tourist_biu
BEFORE INSERT ON tourist
FOR EACH ROW
BEGIN
    IF NEW.phone IS NOT NULL AND NEW.phone != '' THEN
        IF NOT (NEW.phone REGEXP '^1[3-9][0-9]{9}$'
                OR NEW.phone REGEXP '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '游客手机号格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    IF NEW.id_card IS NOT NULL AND NEW.id_card != '' THEN
        IF NOT (NEW.id_card REGEXP '^[0-9]{17}[0-9Xx]$') THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '游客身份证号格式不正确（应为18位，末位可为数字或X）';
        END IF;
    END IF;
END $$

-- ---------------------------------------------------------------
-- 导游表校验
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
-- 旅游团联系人电话校验
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
-- 宾馆联系电话 / 传真校验
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

-- 验证触发器已创建
SELECT TRIGGER_NAME, EVENT_MANIPULATION, EVENT_OBJECT_TABLE
FROM information_schema.triggers
WHERE trigger_schema = 'travel_agency'
ORDER BY EVENT_OBJECT_TABLE, EVENT_MANIPULATION;
