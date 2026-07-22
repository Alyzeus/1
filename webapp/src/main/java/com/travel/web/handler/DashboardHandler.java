package com.travel.web.handler;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 仪表盘统计 */
public class DashboardHandler {

    public String handleGet(HttpServletRequest req) throws SQLException {
        int signups = DBUtil.query("SELECT COUNT(*) AS cnt FROM registration "
            + "WHERE EXTRACT(YEAR FROM reg_date) = EXTRACT(YEAR FROM CURRENT_DATE) "
            + "AND EXTRACT(MONTH FROM reg_date) = EXTRACT(MONTH FROM CURRENT_DATE)")
            .stream().findFirst().map(m -> ((Number)m.get("cnt")).intValue()).orElse(0);
        int inGroup = DBUtil.query("SELECT COALESCE(SUM(actual_people),0) AS cnt FROM tour_group")
            .stream().findFirst().map(m -> ((Number)m.get("cnt")).intValue()).orElse(0);
        int activeGroups = DBUtil.query("SELECT COUNT(*) AS cnt FROM tour_group WHERE actual_people > 0")
            .stream().findFirst().map(m -> ((Number)m.get("cnt")).intValue()).orElse(0);
        double income = DBUtil.query(
            "SELECT COALESCE(SUM(b.price * b.discount * tg.actual_people), 0) AS total "
            + "FROM tour_group tg JOIN batch b ON tg.batch_no = b.batch_no "
            + "WHERE EXTRACT(YEAR FROM b.depart_date) = EXTRACT(YEAR FROM CURRENT_DATE) "
            + "AND EXTRACT(MONTH FROM b.depart_date) = EXTRACT(MONTH FROM CURRENT_DATE)")
            .stream().findFirst().map(m -> ((Number)m.get("total")).doubleValue()).orElse(0.0);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("signups", signups);
        stats.put("inGroup", inGroup);
        stats.put("activeGroups", activeGroups);
        stats.put("income", Math.round(income * 100.0) / 100.0);
        return "{\"ok\":true," + DBUtil.toJsonOne(stats).substring(1);
    }
}
