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

import org.mindrot.jbcrypt.BCrypt;

import com.travel.util.DBUtil;

/** 登录 / 注销（bcrypt 密码验证 + session 固定防护） */
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
            // bcrypt 验证：查询密码哈希，在应用层比对
            List<Map<String, Object>> rows = DBUtil.query(
                    "SELECT username, realname, role, password FROM sys_user WHERE username = ?",
                    username);
            if (rows.isEmpty() || !BCrypt.checkpw(password, (String) rows.get(0).get("password"))) {
                if ("json".equals(req.getParameter("fmt"))) {
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"ok\":false,\"err\":\"用户名或密码错误\"}");
                    return;
                }
                req.setAttribute("err", "用户名或密码错误");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }
            // 防止 session 固定攻击：先创建 session，再更换 ID
            req.getSession(true);
            req.changeSessionId();
            req.getSession().setAttribute("username", rows.get(0).get("username"));
            req.getSession().setAttribute("user", rows.get(0).get("realname"));
            req.getSession().setAttribute("role", rows.get(0).get("role"));
            // 生成 CSRF token
            String csrfToken = java.util.UUID.randomUUID().toString();
            req.getSession().setAttribute("csrf_token", csrfToken);
            // SPA 登录返回 JSON；JSP 登录重定向
            if ("json".equals(req.getParameter("fmt"))) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"ok\":true,\"user\":\""
                        + DBUtil.escJson((String) rows.get(0).get("realname")) + "\",\"role\":\""
                        + DBUtil.escJson((String) rows.get(0).get("role")) + "\",\"username\":\""
                        + DBUtil.escJson((String) rows.get(0).get("username")) + "\",\"csrf\":\""
                        + csrfToken + "\"}");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/menu.jsp");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
