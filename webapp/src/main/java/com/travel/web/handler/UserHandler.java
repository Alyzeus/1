package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;
import com.travel.util.Roles;

/** 用户管理（仅管理员） */
public class UserHandler {

    public String handleGet(HttpServletRequest req) throws SQLException {
        if (!Roles.isAdmin(req)) return jsonError("无权限");
        String sql = "SELECT username, realname, role, phone, status, created FROM sys_user ORDER BY username";
        return jsonList(DBUtil.query(sql));
    }

    public String handlePost(HttpServletRequest req) throws SQLException {
        if (!Roles.isAdmin(req)) return jsonError("无权限");
        String username = req.getParameter("username");
        DBUtil.update(
            "INSERT INTO sys_user (username, realname, role, phone, status) VALUES (?,?,?,?,?) "
            + "ON CONFLICT (username) DO UPDATE SET realname=EXCLUDED.realname, role=EXCLUDED.role, "
            + "phone=EXCLUDED.phone, status=EXCLUDED.status",
            username, req.getParameter("realname"), req.getParameter("role"),
            req.getParameter("phone"), req.getParameter("status"));
        return jsonOk("用户保存成功");
    }

    public String handleDelete(HttpServletRequest req) throws SQLException {
        if (!Roles.isAdmin(req)) return jsonError("无权限");
        DBUtil.update("DELETE FROM sys_user WHERE username = ?", req.getParameter("username"));
        return jsonOk("用户删除成功");
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
    private static String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }
    private static String jsonError(String err) {
        return "{\"ok\":false,\"err\":\"" + DBUtil.escJson(err) + "\"}";
    }
}
