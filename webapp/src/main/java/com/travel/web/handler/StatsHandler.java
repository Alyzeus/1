package com.travel.web.handler;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.travel.util.DBUtil;

/** 统计查询 */
public class StatsHandler {

    public String handleIncome(HttpServletRequest req) throws SQLException {
        String sql = "SELECT b.batch_no, r.start_place, r.end_place, "
                   + "b.price, b.discount, "
                   + "COALESCE(SUM(tg.actual_people), 0) AS total_people, "
                   + "CAST(b.price * b.discount * COALESCE(SUM(tg.actual_people), 0) AS DECIMAL(12,2)) AS income "
                   + "FROM batch b LEFT JOIN route r ON b.route_no = r.route_no "
                   + "LEFT JOIN tour_group tg ON b.batch_no = tg.batch_no "
                   + "GROUP BY b.batch_no, r.start_place, r.end_place, b.price, b.discount "
                   + "ORDER BY b.batch_no";
        return jsonList(DBUtil.query(sql));
    }

    public String handleGuideSchedule(HttpServletRequest req) throws SQLException {
        String sql = "SELECT gs.guide_no, g.name AS guide_name, gs.batch_no, "
                   + "b.depart_date, b.return_date FROM batch_guide gs "
                   + "JOIN guide g ON gs.guide_no = g.guide_no "
                   + "JOIN batch b ON gs.batch_no = b.batch_no ORDER BY b.depart_date";
        return jsonList(DBUtil.query(sql));
    }

    public String handleHotelReception(HttpServletRequest req) throws SQLException {
        String sql = "SELECT bh.hotel_no, h.hotel_name, bh.batch_no, "
                   + "b.depart_date, b.return_date FROM batch_hotel bh "
                   + "JOIN hotel h ON bh.hotel_no = h.hotel_no "
                   + "JOIN batch b ON bh.batch_no = b.batch_no ORDER BY b.depart_date";
        return jsonList(DBUtil.query(sql));
    }

    private static String jsonList(java.util.List<java.util.Map<String,Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }
}
