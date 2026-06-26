-- =====================================================================
-- 题目七：旅行社旅游系统
-- 06_app_user.sql  系统用户表（供 Web 端登录与用户管理）
-- =====================================================================
USE travel_agency;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    username  VARCHAR(30) NOT NULL COMMENT '登录名',
    password  CHAR(64)    NOT NULL COMMENT '口令（SHA2-256 摘要）',
    real_name VARCHAR(30) NOT NULL COMMENT '真实姓名',
    role      ENUM('管理员','业务员','调度员','财务','经理') NOT NULL DEFAULT '业务员' COMMENT '角色',
    PRIMARY KEY (username)
) ENGINE=InnoDB COMMENT='系统用户';

-- 初始账号：admin / admin123，business / bus123，dispatch / dis123
INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin',    SHA2('admin123', 256), '系统管理员', '管理员'),
('business', SHA2('bus123',   256), '李业务',     '业务员'),
('dispatch', SHA2('dis123',   256), '王调度',     '调度员')
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);
