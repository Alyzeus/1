package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 导游 CRUD */
public class GuideHandler {
    public String handleGet(HttpServletRequest req) throws SQLException {
        String sql = "SELECT guide_no, name, languages, glevel FROM guide ORDER BY guide_no";
        return jsonList(DBUtil.query(sql));
    }

    public String handlePost(HttpServletRequest req) throws SQLException {
        String no = req.getParameter("guide_no");
        String gIdCard = req.getParameter("id_card");
        DBUtil.update(
            "INSERT INTO guide (guide_no, id_card, name, gender, languages, glevel) "
            + "VALUES (?,COALESCE(NULLIF(?,''),'000000000000000000'),?,COALESCE(NULLIF(?,''),'男'),?,?) "
            + "ON CONFLICT (guide_no) DO UPDATE SET name=EXCLUDED.name, "
            + "languages=EXCLUDED.languages, glevel=EXCLUDED.glevel",
            no, gIdCard, req.getParameter("name"), req.getParameter("gender"),
            req.getParameter("languages"), req.getParameter("glevel"));
        return jsonOk("导游保存成功");
    }

    public String handleDelete(HttpServletRequest req) throws SQLException {
        DBUtil.update("DELETE FROM guide WHERE guide_no = ?", req.getParameter("guide_no"));
        return jsonOk("导游删除成功");
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
    private static String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }
}
