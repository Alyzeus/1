package com.travel.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** 登录校验 + 统一字符编码过滤器（通过 web.xml 注册） */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // 禁用浏览器缓存，确保每次读取最新文件（开发阶段）
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");

        String p = req.getServletPath();
        // 根路径重定向到 index.html，避免 welcome-file 缓存问题
        if ("/".equals(p) || "".equals(p)) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }
        boolean open = p.equals("/login")
                || p.equals("/index.html") || p.startsWith("/css/") || p.equals("/favicon.ico")
                || p.startsWith("/api/");
        if (!open) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
