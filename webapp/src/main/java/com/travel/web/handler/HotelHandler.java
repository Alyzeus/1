package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 宾馆 CRUD */
public class HotelHandler {
    public String handleGet(HttpServletRequest req) throws SQLException {
        String sql = "SELECT hotel_no, hotel_name, city, star, std_price AS price FROM hotel ORDER BY hotel_no";
        return jsonList(DBUtil.query(sql));
    }

    public String handlePost(HttpServletRequest req) throws SQLException {
        String no = req.getParameter("hotel_no");
        DBUtil.update(
            "INSERT INTO hotel (hotel_no, hotel_name, city, star, std_price) VALUES (?,?,?,CAST(? AS SMALLINT),CAST(? AS DECIMAL(8,2))) "
            + "ON CONFLICT (hotel_no) DO UPDATE SET hotel_name=EXCLUDED.hotel_name, city=EXCLUDED.city, "
            + "star=EXCLUDED.star, std_price=EXCLUDED.std_price",
            no, req.getParameter("hotel_name"), req.getParameter("city"),
            req.getParameter("star"), req.getParameter("price"));
        return jsonOk("宾馆保存成功");
    }

    public String handleDelete(HttpServletRequest req) throws SQLException {
        DBUtil.update("DELETE FROM hotel WHERE hotel_no = ?", req.getParameter("hotel_no"));
        return jsonOk("宾馆删除成功");
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
    private static String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }
}
