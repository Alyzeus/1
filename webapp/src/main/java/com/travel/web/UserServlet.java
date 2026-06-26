package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/**
 * 用户管理：增删改查（仅管理员可操作）。
 * 密码以 SHA2-256 摘要存储，编辑时不显示原密码。
 */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 角色鉴权：仅管理员
        if (!"管理员".equals(req.getSession().getAttribute("role"))) {
            resp.sendError(403, "仅管理员可访问用户管理");
            return;
        }
        String action = req.getParameter("action");
        try {
            if ("del".equals(action)) {
                String username = req.getParameter("username");
                // 禁止删除自己
                if (username != null && username.equals(req.getSession().getAttribute("username"))) {
                    req.setAttribute("err", "不能删除当前登录账号");
                } else {
                    int n = DBUtil.update("DELETE FROM sys_user WHERE username = ?", username);
                    req.setAttribute("msg", n > 0 ? "删除成功" : "未找到该用户");
                }
            } else if ("edit".equals(action)) {
                req.setAttribute("edit", DBUtil.query(
                        "SELECT username, real_name, role FROM sys_user WHERE username = ?",
                        req.getParameter("username")).stream().findFirst().orElse(null));
            }
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!"管理员".equals(req.getSession().getAttribute("role"))) {
            resp.sendError(403);
            return;
        }
        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String realName = req.getParameter("real_name");
            String role = req.getParameter("role");

            // 密码为空表示不修改密码（编辑场景）
            if (password == null || password.trim().isEmpty()) {
                DBUtil.update(
                        "INSERT INTO sys_user (username, password, real_name, role) VALUES (?, '', ?, ?) "
                                + "ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), role = VALUES(role)",
                        username, realName, role);
            } else {
                DBUtil.update(
                        "INSERT INTO sys_user (username, password, real_name, role) VALUES (?, SHA2(?, 256), ?, ?) "
                                + "ON DUPLICATE KEY UPDATE password = VALUES(password), "
                                + "real_name = VALUES(real_name), role = VALUES(role)",
                        username, password, realName, role);
            }
            req.setAttribute("msg", "保存成功");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("rows", DBUtil.query(
                    "SELECT username AS 用户名, real_name AS 姓名, role AS 角色 FROM sys_user ORDER BY username"));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/user.jsp").forward(req, resp);
    }
}
