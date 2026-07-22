package com.travel.web;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/** 团体投保：调用存储过程 sp_buy_insurance（成团校验、一团一保由数据库端保证） */
@WebServlet("/insurance")
public class InsuranceServlet extends HttpServlet {

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
        try (Connection c = DBUtil.getConnection();
             CallableStatement cs = c.prepareCall("{call sp_buy_insurance(?,?,?)}")) {
            cs.setString(1, req.getParameter("policy_no"));
            cs.setString(2, req.getParameter("group_no"));
            cs.setBigDecimal(3, new java.math.BigDecimal(req.getParameter("per_fee")));
            cs.execute();
            req.setAttribute("msg", "投保成功，保险期限已自动取班次出发~回程日期");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        } catch (NumberFormatException e) {
            req.setAttribute("err", "人均保险费必须是数字");
        }
        list(req, resp);
    }

    private boolean checkRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return com.travel.util.Roles.requireRole(req, resp, com.travel.util.Roles.ADMIN, com.travel.util.Roles.FINANCE);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("rows", DBUtil.query(
                    "SELECT i.policy_no AS 保险单号, i.group_no AS 团号, g.group_name AS 团名, "
                            + "g.actual_people AS 人数, i.per_fee AS 人均保险费, "
                            + "i.ins_start AS 保险起, i.ins_end AS 保险止, "
                            + "i.per_fee * g.actual_people AS 保费合计 "
                            + "FROM insurance i JOIN tour_group g ON g.group_no = i.group_no ORDER BY i.policy_no"));
            req.setAttribute("groups", DBUtil.query(
                    "SELECT g.group_no, CONCAT(g.group_no, ' ', g.group_name, '（', g.actual_people, '人）') AS label "
                            + "FROM tour_group g ORDER BY g.group_no"));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/insurance.jsp").forward(req, resp);
    }
}
