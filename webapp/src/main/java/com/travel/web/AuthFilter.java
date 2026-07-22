package com.travel.web;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** 登录校验 + CSRF 防护 + 安全响应头 + 统一字符编码过滤器 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // 安全响应头
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        String p = req.getServletPath();
        // 根路径重定向到 index.html
        if ("/".equals(p) || "".equals(p)) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }
        // CSRF token：首次访问时初始化
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("csrf_token") == null) {
            session.setAttribute("csrf_token", UUID.randomUUID().toString());
        }
        boolean open = p.equals("/login")
                || p.equals("/index.html") || p.startsWith("/css/") || p.equals("/favicon.ico")
                || p.startsWith("/api/");
        if (!open) {
            if (session == null || session.getAttribute("user") == null) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
