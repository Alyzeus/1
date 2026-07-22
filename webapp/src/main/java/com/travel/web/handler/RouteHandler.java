package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 线路 CRUD */
public class RouteHandler {
    public String handleGet(HttpServletRequest req) throws SQLException {
        String kw = req.getParameter("kw");
        String sql = "SELECT route_no, start_place, end_place, days, main_spots FROM route";
        if (kw != null && !kw.trim().isEmpty()) {
            String like = "%" + kw.trim() + "%";
            sql += " WHERE start_place LIKE ? OR end_place LIKE ? OR main_spots LIKE ?";
            return jsonList(DBUtil.query(sql + " ORDER BY route_no", like, like, like));
        }
        return jsonList(DBUtil.query(sql + " ORDER BY route_no"));
    }

    public String handlePost(HttpServletRequest req) throws SQLException {
        String no = req.getParameter("route_no");
        DBUtil.update(
            "INSERT INTO route (route_no, start_place, end_place, days, main_spots) VALUES (?,?,?,CAST(? AS SMALLINT),?) "
            + "ON CONFLICT (route_no) DO UPDATE SET start_place=EXCLUDED.start_place, end_place=EXCLUDED.end_place, "
            + "days=EXCLUDED.days, main_spots=EXCLUDED.main_spots",
            no, req.getParameter("start_place"), req.getParameter("end_place"),
            req.getParameter("days"), req.getParameter("main_spots"));
        return jsonOk("线路保存成功");
    }

    public String handleDelete(HttpServletRequest req) throws SQLException {
        DBUtil.update("DELETE FROM route WHERE route_no = ?", req.getParameter("route_no"));
        return jsonOk("线路删除成功");
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
    private static String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }
}
