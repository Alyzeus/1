package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 报名记录 / 旅游团 / 团名单 / 保险 查询 */
public class RegGroupInsHandler {

    public String handleRegistrations(HttpServletRequest req) throws SQLException {
        String sql = "SELECT reg.reg_no, reg.tourist_no, t.name AS tourist_name, "
                   + "reg.route_no, r.start_place, r.end_place, reg.reg_date "
                   + "FROM registration reg "
                   + "LEFT JOIN tourist t ON reg.tourist_no = t.tourist_no "
                   + "LEFT JOIN route r ON reg.route_no = r.route_no ORDER BY reg.reg_no DESC";
        return jsonList(DBUtil.query(sql));
    }

    public String handleGroups(HttpServletRequest req) throws SQLException {
        String sql = "SELECT tg.group_no, tg.group_name, tg.batch_no, "
                   + "b.depart_date, tg.contact_name, tg.contact_phone, "
                   + "tg.contact_addr, tg.actual_people "
                   + "FROM tour_group tg LEFT JOIN batch b ON tg.batch_no = b.batch_no "
                   + "ORDER BY tg.group_no";
        return jsonList(DBUtil.query(sql));
    }

    public String handleGroupMembers(HttpServletRequest req) throws SQLException {
        String gno = req.getParameter("group_no");
        String sql = "SELECT gm.group_no, gm.tourist_no, t.name AS tourist_name, "
                   + "t.id_card, t.phone FROM group_member gm "
                   + "JOIN tourist t ON gm.tourist_no = t.tourist_no";
        if (gno != null && !gno.isEmpty()) {
            sql += " WHERE gm.group_no = ?";
            return jsonList(DBUtil.query(sql + " ORDER BY t.name", gno));
        }
        return jsonList(DBUtil.query(sql + " ORDER BY gm.group_no, t.name"));
    }

    public String handleInsurances(HttpServletRequest req) throws SQLException {
        String sql = "SELECT i.policy_no, i.group_no, tg.group_name, "
                   + "i.per_fee, i.ins_start, i.ins_end "
                   + "FROM insurance i "
                   + "JOIN tour_group tg ON i.group_no = tg.group_no ORDER BY i.policy_no";
        return jsonList(DBUtil.query(sql));
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
}
