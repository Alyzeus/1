package com.travel.util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 角色常量与鉴权工具（消除魔法字符串） */
public final class Roles {
    public static final String ADMIN      = "管理员";
    public static final String AGENT      = "业务员";
    public static final String DISPATCHER = "调度员";
    public static final String FINANCE    = "财务";
    public static final String MANAGER    = "经理";

    private Roles() {}

    public static String get(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("role");
    }

    public static boolean isAdmin(HttpServletRequest req) {
        return ADMIN.equals(get(req));
    }

    public static boolean hasRole(HttpServletRequest req, String... roles) {
        String r = get(req);
        if (r == null) return false;
        for (String role : roles) if (role.equals(r)) return true;
        return false;
    }

    /** 仅管理员可访问，否则返回 403 */
    public static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) { resp.sendError(403, "仅管理员可访问"); return false; }
        return true;
    }

    /** 指定角色可访问，否则返回 403 */
    public static boolean requireRole(HttpServletRequest req, HttpServletResponse resp, String... roles) throws IOException {
        if (!hasRole(req, roles)) { resp.sendError(403, "无权限"); return false; }
        return true;
    }
}
