package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 游客 CRUD */
public class TouristHandler {
    public String handleGet(HttpServletRequest req) throws SQLException {
        String sql = "SELECT tourist_no, name, id_card, gender, birth_date, address, phone FROM tourist ORDER BY tourist_no";
        return jsonList(DBUtil.query(sql));
    }

    public String handlePost(HttpServletRequest req) throws SQLException {
        String no = req.getParameter("tourist_no");
        DBUtil.update(
            "INSERT INTO tourist (tourist_no, name, id_card, gender, birth_date, address, phone) "
            + "VALUES (?,?,?,?,CAST(? AS DATE),?,?) "
            + "ON CONFLICT (tourist_no) DO UPDATE SET name=EXCLUDED.name, id_card=EXCLUDED.id_card, "
            + "gender=EXCLUDED.gender, birth_date=EXCLUDED.birth_date, "
            + "address=EXCLUDED.address, phone=EXCLUDED.phone",
            no, req.getParameter("name"), req.getParameter("id_card"),
            req.getParameter("gender"), req.getParameter("birth_date"),
            req.getParameter("address"), req.getParameter("phone"));
        return jsonOk("游客保存成功");
    }

    public String handleDelete(HttpServletRequest req) throws SQLException {
        DBUtil.update("DELETE FROM tourist WHERE tourist_no = ?", req.getParameter("tourist_no"));
        return jsonOk("游客删除成功");
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
    private static String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }
}
