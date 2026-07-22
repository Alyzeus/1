package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/** 班次管理：开设班次（回程日期可留空，由触发器按线路天数自动推算）、查询 */
@WebServlet("/batch")
public class BatchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        list(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        try {
            String returnDate = req.getParameter("return_date");
            if (returnDate != null && returnDate.trim().isEmpty()) {
                returnDate = null; // 留空 -> 触发器 trg_batch_bi 自动推算
            }
            DBUtil.update(
                    "INSERT INTO batch (batch_no, route_no, depart_date, return_date, standard, price, discount) "
                            + "VALUES (?,?,?,?,?,?,?)",
                    req.getParameter("batch_no"), req.getParameter("route_no"),
                    req.getParameter("depart_date"), returnDate,
                    req.getParameter("standard"), req.getParameter("price"), req.getParameter("discount"));
            req.setAttribute("msg", "班次开设成功");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    private boolean checkRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return com.travel.util.Roles.requireRole(req, resp, com.travel.util.Roles.ADMIN, com.travel.util.Roles.AGENT, com.travel.util.Roles.DISPATCHER);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("rows", DBUtil.query("SELECT * FROM v_batch_full ORDER BY 出发日期"));
            req.setAttribute("routes", DBUtil.query(
                    "SELECT route_no, CONCAT(route_no, ' ', start_place, '→', end_place, ' ', days, '天') AS label "
                            + "FROM route ORDER BY route_no"));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/batch.jsp").forward(req, resp);
    }
}
