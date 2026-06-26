package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/** 登录 / 注销 */
@WebServlet({"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getServletPath().equals("/logout")) {
            req.getSession().invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            // 口令以 SHA2-256 摘要存储，比对在数据库端完成
            List<Map<String, Object>> rows = DBUtil.query(
                    "SELECT username, real_name, role FROM sys_user WHERE username = ? AND password = SHA2(?, 256)",
                    username, password);
            if (rows.isEmpty()) {
                if ("json".equals(req.getParameter("fmt"))) {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"ok\":false,\"err\":\"用户名或密码错误\"}");
                    return;
                }
                req.setAttribute("err", "用户名或密码错误");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }
            req.getSession().setAttribute("username", rows.get(0).get("username"));
            req.getSession().setAttribute("user", rows.get(0).get("real_name"));
            req.getSession().setAttribute("role", rows.get(0).get("role"));
            // SPA 登录返回 JSON；JSP 登录重定向
            if ("json".equals(req.getParameter("fmt"))) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"ok\":true,\"user\":\""
                        + rows.get(0).get("real_name") + "\",\"role\":\""
                        + rows.get(0).get("role") + "\",\"username\":\""
                        + rows.get(0).get("username") + "\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/menu.jsp");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
