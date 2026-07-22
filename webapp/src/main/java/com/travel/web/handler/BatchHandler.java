package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 班次 CRUD */
public class BatchHandler {
    public String handleGet(HttpServletRequest req) throws SQLException {
        String sql = "SELECT b.batch_no, b.route_no, r.start_place, r.end_place, "
                   + "b.depart_date, b.return_date, b.standard, b.price, b.discount FROM batch b "
                   + "LEFT JOIN route r ON b.route_no = r.route_no ORDER BY b.batch_no";
        return jsonList(DBUtil.query(sql));
    }

    public String handlePost(HttpServletRequest req) throws SQLException {
        String no = req.getParameter("batch_no");
        DBUtil.update(
            "INSERT INTO batch (batch_no, route_no, depart_date, return_date, standard, price, discount) "
            + "VALUES (?,?,CAST(? AS DATE),CAST(? AS DATE),?,CAST(? AS DECIMAL(10,2)),CAST(? AS DECIMAL(3,2))) "
            + "ON CONFLICT (batch_no) DO UPDATE SET route_no=EXCLUDED.route_no, depart_date=EXCLUDED.depart_date, "
            + "return_date=EXCLUDED.return_date, standard=EXCLUDED.standard, price=EXCLUDED.price, "
            + "discount=EXCLUDED.discount",
            no, req.getParameter("route_no"), req.getParameter("depart_date"),
            req.getParameter("return_date"), req.getParameter("standard"),
            req.getParameter("price"), req.getParameter("discount"));
        return jsonOk("班次保存成功");
    }

    public String handleDelete(HttpServletRequest req) throws SQLException {
        DBUtil.update("DELETE FROM batch WHERE batch_no = ?", req.getParameter("batch_no"));
        return jsonOk("班次删除成功");
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
    private static String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }
}
