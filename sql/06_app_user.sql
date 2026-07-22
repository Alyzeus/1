-- =====================================================================
-- 题目七：旅行社旅游系统 — PostgreSQL 系统用户表 (bcrypt 密码哈希)
-- =====================================================================

CREATE TABLE IF NOT EXISTS sys_user (
    username  VARCHAR(30) NOT NULL,
    password  VARCHAR(60) NOT NULL,
    realname  VARCHAR(30) NOT NULL,
    role      VARCHAR(10) NOT NULL DEFAULT '业务员' CHECK (role IN ('管理员','业务员','调度员','财务','经理')),
    phone     VARCHAR(20),
    status    VARCHAR(10) NOT NULL DEFAULT '启用' CHECK (status IN ('启用','停用')),
    created   DATE        NOT NULL DEFAULT CURRENT_DATE,
    PRIMARY KEY (username)
);
COMMENT ON TABLE  sys_user IS '系统用户';
COMMENT ON COLUMN sys_user.username  IS '登录名';
COMMENT ON COLUMN sys_user.password  IS '口令（bcrypt 摘要）';
COMMENT ON COLUMN sys_user.realname  IS '真实姓名';
COMMENT ON COLUMN sys_user.role      IS '角色';
COMMENT ON COLUMN sys_user.phone     IS '联系电话';
COMMENT ON COLUMN sys_user.status    IS '状态：启用/停用';
COMMENT ON COLUMN sys_user.created   IS '创建日期';

-- 初始账号（bcrypt 哈希，cost=10）：admin/admin123, business/bus123, dispatch/dis123
INSERT INTO sys_user (username, password, realname, role) VALUES
('admin',    '$2a$10$XtW9UFe0szM9TWnnfNFJ7uNkmzzq9FqzLBsLx8OBVDl33HOQpmNfe', '系统管理员', '管理员'),
('business', '$2a$10$TCpexu27WZmKygFU8oYqLu1ADbpKhtoptj69gM4jnk69mgFPz6U2C', '李业务',     '业务员'),
('dispatch', '$2a$10$72gyKULV5/L/.4iONF2u/.uyJRN6JAciLSdL9QF.zrsbwolp2X5.C', '王调度',     '调度员')
ON CONFLICT (username) DO UPDATE SET realname = EXCLUDED.realname;
