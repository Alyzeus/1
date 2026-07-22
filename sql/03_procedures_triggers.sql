-- =====================================================================
-- 题目七：旅行社旅游系统 — PostgreSQL 函数与触发器
-- 注意：执行前需先启用 pgcrypto 扩展：
--   CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- =====================================================================

-- =====================================================================
-- 触发器部分
-- =====================================================================

-- ---------------------------------------------------------------
-- 触发器1：插入班次时，回程日期为空则按线路天数自动推算
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_batch_bi ON batch;
DROP FUNCTION IF EXISTS trg_batch_bi_func;

CREATE OR REPLACE FUNCTION trg_batch_bi_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    v_days SMALLINT;
BEGIN
    IF NEW.return_date IS NULL THEN
        SELECT days INTO v_days FROM route WHERE route_no = NEW.route_no;
        NEW.return_date := NEW.depart_date + (v_days - 1);
    END IF;
    IF NEW.return_date < NEW.depart_date THEN
        RAISE EXCEPTION '回程日期不能早于出发日期';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_batch_bi
BEFORE INSERT ON batch
FOR EACH ROW EXECUTE FUNCTION trg_batch_bi_func();

-- ---------------------------------------------------------------
-- 触发器2：入团前校验团人数未达上限 50
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_member_bi ON group_member;
DROP FUNCTION IF EXISTS trg_member_bi_func;

CREATE OR REPLACE FUNCTION trg_member_bi_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    v_people INT;
BEGIN
    SELECT actual_people INTO v_people FROM tour_group WHERE group_no = NEW.group_no;
    IF v_people >= 50 THEN
        RAISE EXCEPTION '该旅游团已满（上限50人），禁止入团';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_member_bi
BEFORE INSERT ON group_member
FOR EACH ROW EXECUTE FUNCTION trg_member_bi_func();

-- ---------------------------------------------------------------
-- 触发器3：入团后旅游团实际人数自动 +1
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_member_ai ON group_member;
DROP FUNCTION IF EXISTS trg_member_ai_func;

CREATE OR REPLACE FUNCTION trg_member_ai_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE tour_group SET actual_people = actual_people + 1
    WHERE group_no = NEW.group_no;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_member_ai
AFTER INSERT ON group_member
FOR EACH ROW EXECUTE FUNCTION trg_member_ai_func();

-- ---------------------------------------------------------------
-- 触发器4：退团后旅游团实际人数自动 -1
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_member_ad ON group_member;
DROP FUNCTION IF EXISTS trg_member_ad_func;

CREATE OR REPLACE FUNCTION trg_member_ad_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    v_people INT;
BEGIN
    SELECT actual_people INTO v_people FROM tour_group WHERE group_no = OLD.group_no;
    IF v_people <= 0 THEN
        RAISE EXCEPTION '退团失败：当前团人数为0，无法继续退团';
    END IF;
    UPDATE tour_group SET actual_people = actual_people - 1
    WHERE group_no = OLD.group_no;
    RETURN OLD;
END;
$$;

CREATE TRIGGER trg_member_ad
AFTER DELETE ON group_member
FOR EACH ROW EXECUTE FUNCTION trg_member_ad_func();

-- ---------------------------------------------------------------
-- 触发器5：投保前校验"一团一保"
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_insurance_bi ON insurance;
DROP FUNCTION IF EXISTS trg_insurance_bi_func;

CREATE OR REPLACE FUNCTION trg_insurance_bi_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM insurance WHERE group_no = NEW.group_no) THEN
        RAISE EXCEPTION '一个旅游团在旅游期间只能参加一次保险';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_insurance_bi
BEFORE INSERT ON insurance
FOR EACH ROW EXECUTE FUNCTION trg_insurance_bi_func();

-- =====================================================================
-- 函数部分
-- =====================================================================

-- ---------------------------------------------------------------
-- 函数1：游客报名
-- ---------------------------------------------------------------
DROP FUNCTION IF EXISTS sp_tourist_register;

CREATE OR REPLACE FUNCTION sp_tourist_register(
    p_id_card     CHAR(18),
    p_name        VARCHAR(30),
    p_gender      VARCHAR(2),
    p_birth_date  DATE,
    p_address     VARCHAR(100),
    p_phone       VARCHAR(20),
    p_route_no    CHAR(4),
    p_depart_date DATE
)
RETURNS TABLE(游客编号 CHAR(8), 结果 VARCHAR(20))
LANGUAGE plpgsql AS $$
DECLARE
    v_tourist_no CHAR(8);
    v_days SMALLINT;
BEGIN
    -- 校验线路存在
    SELECT days INTO v_days FROM route WHERE route_no = p_route_no;
    IF v_days IS NULL THEN
        RAISE EXCEPTION '报名失败：线路不存在';
    END IF;

    -- 游客查档，不存在则建档
    SELECT tourist_no INTO v_tourist_no FROM tourist WHERE id_card = p_id_card;
    IF v_tourist_no IS NULL THEN
        SELECT CONCAT('T', LPAD(COALESCE(MAX(CAST(SUBSTRING(tourist_no, 2) AS INTEGER)), 0) + 1, 7, '0'))
        INTO v_tourist_no FROM tourist;
        INSERT INTO tourist (tourist_no, id_card, name, gender, birth_date, address, phone)
        VALUES (v_tourist_no, p_id_card, p_name, p_gender, p_birth_date, p_address, p_phone);
    END IF;

    -- 重复报名校验
    IF EXISTS (SELECT 1 FROM registration
               WHERE tourist_no = v_tourist_no AND route_no = p_route_no
                 AND depart_date = p_depart_date) THEN
        RAISE EXCEPTION '报名失败：该游客已报名此线路此班期';
    END IF;

    INSERT INTO registration (tourist_no, route_no, depart_date, return_date)
    VALUES (v_tourist_no, p_route_no, p_depart_date, p_depart_date + v_days - 1);

    RETURN QUERY SELECT v_tourist_no, '报名成功'::VARCHAR(20);
END;
$$;

-- ---------------------------------------------------------------
-- 函数2：游客入团
-- ---------------------------------------------------------------
DROP FUNCTION IF EXISTS sp_join_group;

CREATE OR REPLACE FUNCTION sp_join_group(
    p_group_no   CHAR(8),
    p_tourist_no CHAR(8)
)
RETURNS TABLE(结果 VARCHAR(20), 当前团人数 INT)
LANGUAGE plpgsql AS $$
DECLARE
    v_people INT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM tour_group WHERE group_no = p_group_no) THEN
        RAISE EXCEPTION '入团失败：旅游团不存在';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM tourist WHERE tourist_no = p_tourist_no) THEN
        RAISE EXCEPTION '入团失败：游客不存在';
    END IF;
    IF EXISTS (SELECT 1 FROM group_member
               WHERE group_no = p_group_no AND tourist_no = p_tourist_no) THEN
        RAISE EXCEPTION '入团失败：该游客已在此团中';
    END IF;

    INSERT INTO group_member (group_no, tourist_no) VALUES (p_group_no, p_tourist_no);

    SELECT actual_people INTO v_people FROM tour_group WHERE group_no = p_group_no;
    RETURN QUERY SELECT '入团成功'::VARCHAR(20), v_people;
END;
$$;

-- ---------------------------------------------------------------
-- 函数3：成团校验
-- ---------------------------------------------------------------
DROP FUNCTION IF EXISTS sp_confirm_group;

CREATE OR REPLACE FUNCTION sp_confirm_group(
    p_group_no CHAR(8)
)
RETURNS TABLE(团号 CHAR(8), 实际人数 INT, 结果 VARCHAR(20))
LANGUAGE plpgsql AS $$
DECLARE
    v_people INT;
BEGIN
    SELECT actual_people INTO v_people FROM tour_group WHERE group_no = p_group_no;
    IF v_people IS NULL THEN
        RAISE EXCEPTION '成团校验失败：旅游团不存在';
    ELSIF v_people < 20 THEN
        RAISE EXCEPTION '成团校验失败：人数不足20人，不能成团';
    ELSIF v_people > 50 THEN
        RAISE EXCEPTION '成团校验失败：人数超过50人上限';
    END IF;
    RETURN QUERY SELECT p_group_no, v_people, '成团成功'::VARCHAR(20);
END;
$$;

-- ---------------------------------------------------------------
-- 函数4：团体投保
-- ---------------------------------------------------------------
DROP FUNCTION IF EXISTS sp_buy_insurance;

CREATE OR REPLACE FUNCTION sp_buy_insurance(
    p_policy_no CHAR(10),
    p_group_no  CHAR(8),
    p_per_fee   DECIMAL(8,2)
)
RETURNS TABLE(保险单号 CHAR(10), 团号 CHAR(8), 保险起 DATE, 保险止 DATE, 结果 VARCHAR(20))
LANGUAGE plpgsql AS $$
DECLARE
    v_people INT;
    v_start DATE;
    v_end DATE;
    v_policy_no CHAR(10);
BEGIN
    -- 保单号为空时自动生成
    IF p_policy_no IS NULL OR p_policy_no = '' THEN
        SELECT CONCAT('P', LPAD(COALESCE(MAX(CAST(SUBSTRING(policy_no, 2) AS INTEGER)), 0) + 1, 9, '0'))
        INTO v_policy_no FROM insurance;
    ELSE
        v_policy_no := p_policy_no;
    END IF;

    SELECT g.actual_people, b.depart_date, b.return_date
    INTO v_people, v_start, v_end
    FROM tour_group g JOIN batch b ON b.batch_no = g.batch_no
    WHERE g.group_no = p_group_no;

    IF v_people IS NULL THEN
        RAISE EXCEPTION '投保失败：旅游团不存在';
    END IF;
    IF v_people < 20 THEN
        RAISE EXCEPTION '投保失败：该团尚未成团（不足20人）';
    END IF;

    INSERT INTO insurance (policy_no, group_no, per_fee, ins_start, ins_end)
    VALUES (v_policy_no, p_group_no, p_per_fee, v_start, v_end);

    RETURN QUERY SELECT v_policy_no, p_group_no, v_start, v_end, '投保成功'::VARCHAR(20);
END;
$$;

-- ---------------------------------------------------------------
-- 函数5：团费结算
-- ---------------------------------------------------------------
DROP FUNCTION IF EXISTS sp_calc_group_fee;

CREATE OR REPLACE FUNCTION sp_calc_group_fee(
    p_group_no CHAR(8)
)
RETURNS NUMERIC(12,2)
LANGUAGE plpgsql AS $$
DECLARE
    v_total NUMERIC(12,2);
BEGIN
    SELECT ROUND(b.price * b.discount * g.actual_people
                 + COALESCE(i.per_fee, 0) * g.actual_people, 2)
    INTO v_total
    FROM tour_group g
    JOIN batch b ON b.batch_no = g.batch_no
    LEFT JOIN insurance i ON i.group_no = g.group_no
    WHERE g.group_no = p_group_no;

    IF v_total IS NULL THEN
        RAISE EXCEPTION '结算失败：旅游团不存在';
    END IF;

    RETURN v_total;
END;
$$;

-- =====================================================================
-- 数据校验触发器：手机号 / 身份证号格式约束
-- =====================================================================

-- ---------------------------------------------------------------
-- 触发器6：游客表 INSERT/UPDATE 校验
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_tourist_biu ON tourist;
DROP FUNCTION IF EXISTS trg_tourist_biu_func;

CREATE OR REPLACE FUNCTION trg_tourist_biu_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.phone IS NOT NULL AND NEW.phone != '' THEN
        IF NOT (NEW.phone ~ '^1[3-9][0-9]{9}$'
                OR NEW.phone ~ '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            RAISE EXCEPTION '游客手机号格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    IF NEW.id_card IS NOT NULL AND NEW.id_card != '' THEN
        IF NOT (NEW.id_card ~ '^[0-9]{17}[0-9Xx]$') THEN
            RAISE EXCEPTION '游客身份证号格式不正确（应为18位，末位可为数字或X）';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_tourist_biu
BEFORE INSERT ON tourist
FOR EACH ROW EXECUTE FUNCTION trg_tourist_biu_func();

-- ---------------------------------------------------------------
-- 触发器7：导游表 INSERT/UPDATE 校验
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_guide_biu ON guide;
DROP FUNCTION IF EXISTS trg_guide_biu_func;

CREATE OR REPLACE FUNCTION trg_guide_biu_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.phone IS NOT NULL AND NEW.phone != '' THEN
        IF NOT (NEW.phone ~ '^1[3-9][0-9]{9}$'
                OR NEW.phone ~ '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            RAISE EXCEPTION '导游手机号格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    IF NEW.id_card IS NOT NULL AND NEW.id_card != '' THEN
        IF NOT (NEW.id_card ~ '^[0-9]{17}[0-9Xx]$') THEN
            RAISE EXCEPTION '导游身份证号格式不正确（应为18位，末位可为数字或X）';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_guide_biu
BEFORE INSERT ON guide
FOR EACH ROW EXECUTE FUNCTION trg_guide_biu_func();

-- ---------------------------------------------------------------
-- 触发器8：旅游团联系人电话校验
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_group_biu ON tour_group;
DROP FUNCTION IF EXISTS trg_group_biu_func;

CREATE OR REPLACE FUNCTION trg_group_biu_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.contact_phone IS NOT NULL AND NEW.contact_phone != '' THEN
        IF NOT (NEW.contact_phone ~ '^1[3-9][0-9]{9}$'
                OR NEW.contact_phone ~ '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            RAISE EXCEPTION '联系人电话格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_group_biu
BEFORE INSERT ON tour_group
FOR EACH ROW EXECUTE FUNCTION trg_group_biu_func();

-- ---------------------------------------------------------------
-- 触发器9：宾馆联系人电话 / 传真校验
-- ---------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_hotel_biu ON hotel;
DROP FUNCTION IF EXISTS trg_hotel_biu_func;

CREATE OR REPLACE FUNCTION trg_hotel_biu_func()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.contact_phone IS NOT NULL AND NEW.contact_phone != '' THEN
        IF NOT (NEW.contact_phone ~ '^1[3-9][0-9]{9}$'
                OR NEW.contact_phone ~ '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            RAISE EXCEPTION '宾馆联系电话格式不正确（应为11位手机号 或 区号-号码格式）';
        END IF;
    END IF;
    IF NEW.fax IS NOT NULL AND NEW.fax != '' THEN
        IF NOT (NEW.fax ~ '^0[0-9]{2,3}-[0-9]{7,8}$') THEN
            RAISE EXCEPTION '宾馆传真格式不正确（应为 区号-号码格式）';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_hotel_biu
BEFORE INSERT ON hotel
FOR EACH ROW EXECUTE FUNCTION trg_hotel_biu_func();
