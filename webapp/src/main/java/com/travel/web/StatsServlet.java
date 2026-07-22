package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/** 查询统计：导游随团安排、宾馆接待情况、班次收入汇总（基于视图） */
@WebServlet("/stats")
public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("guides", DBUtil.query("SELECT * FROM v_guide_schedule ORDER BY 随团开始"));
            req.setAttribute("hotels", DBUtil.query("SELECT * FROM v_hotel_reception ORDER BY 入住日期"));
            req.setAttribute("income", DBUtil.query(
                    "SELECT b.batch_no AS 班次编号, CONCAT(r.start_place, ' → ', r.end_place) AS 线路, "
                            + "b.depart_date AS 出发日期, ROUND(b.price * b.discount, 2) AS 折后价, "
                            + "COALESCE(SUM(g.actual_people), 0) AS 总人数, "
                            + "ROUND(b.price * b.discount * COALESCE(SUM(g.actual_people), 0), 2) AS 团费收入 "
                            + "FROM batch b JOIN route r ON r.route_no = b.route_no "
                            + "LEFT JOIN tour_group g ON g.batch_no = b.batch_no "
                            + "GROUP BY b.batch_no, r.start_place, r.end_place, b.depart_date, b.price, b.discount "
                            + "ORDER BY b.depart_date"));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/stats.jsp").forward(req, resp);
    }
}
