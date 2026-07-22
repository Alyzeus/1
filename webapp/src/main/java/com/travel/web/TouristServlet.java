package com.travel.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/**
 * 游客报名模块。
 * 阶段6 事务示范：报名涉及 tourist、registration 两张表的写入，
 * 使用 JDBC 事务（setAutoCommit(false) / commit / rollback）保证原子性。
 */
@WebServlet("/tourist")
public class TouristServlet extends HttpServlet {

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
        String idCard = req.getParameter("id_card");
        String routeNo = req.getParameter("route_no");
        String departDate = req.getParameter("depart_date");
        String phone = req.getParameter("phone");

        // --- 服务端格式校验（双重保险：前端 HTML5 + 此处）---
        if (phone != null && !phone.trim().isEmpty()) {
            String p = phone.trim();
            if (!p.matches("1[3-9]\\d{9}") && !p.matches("0\\d{2,3}-\\d{7,8}")) {
                req.setAttribute("err", "手机号格式不正确（应为11位手机号或区号-号码格式）");
                list(req, resp);
                return;
            }
        }
        if (idCard != null && !idCard.trim().isEmpty()) {
            if (!idCard.trim().matches("\\d{17}[0-9Xx]")) {
                req.setAttribute("err", "身份证号格式不正确（应为18位，末位可为数字或X）");
                list(req, resp);
                return;
            }
        }

        try (Connection c = DBUtil.getConnection()) {
            try {
                c.setAutoCommit(false);   // ===== 开启事务（START TRANSACTION）=====

                // 1. 查线路天数（用于推算回程日期）
                Integer days = null;
                try (PreparedStatement ps = DBUtil.prepare(c,
                        "SELECT days FROM route WHERE route_no = ?", routeNo);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        days = rs.getInt(1);
                    }
                }
                if (days == null) {
                    throw new SQLException("报名失败：线路不存在", "P0001");
                }

                // 2. 按身份证号查档，新游客自动生成编号并建档
                String touristNo = null;
                try (PreparedStatement ps = DBUtil.prepare(c,
                        "SELECT tourist_no FROM tourist WHERE id_card = ?", idCard);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        touristNo = rs.getString(1);
                    }
                }
                if (touristNo == null) {
                    // 生成新游客编号（带重试机制防止并发下的主键冲突）
                    int retries = 3;
                    while (true) {
                        try (PreparedStatement ps = DBUtil.prepare(c,
                                "SELECT CONCAT('T', LPAD(COALESCE(MAX(CAST(SUBSTRING(tourist_no,2) AS INTEGER)),0)+1,7,'0')) "
                                        + "FROM tourist");
                             ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            touristNo = rs.getString(1);
                        }
                        try (PreparedStatement ps = DBUtil.prepare(c,
                                "INSERT INTO tourist (tourist_no, id_card, name, gender, birth_date, address, phone) "
                                        + "VALUES (?,?,?,?,?,?,?)",
                                touristNo, idCard, req.getParameter("name"), req.getParameter("gender"),
                                req.getParameter("birth_date"), req.getParameter("address"),
                                req.getParameter("phone"))) {
                            ps.executeUpdate();
                            break;
                        } catch (SQLException e) {
                            if ("23505".equals(e.getSQLState()) && --retries > 0) {
                                continue; // 主键冲突，重新生成编号
                            }
                            throw e;
                        }
                    }
                }

                // 3. 重复报名校验
                try (PreparedStatement ps = DBUtil.prepare(c,
                        "SELECT 1 FROM registration WHERE tourist_no=? AND route_no=? AND depart_date=?",
                        touristNo, routeNo, departDate);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("报名失败：该游客已报名此线路此班期", "P0001");
                    }
                }

                // 4. 写入报名记录，回程日期 = 出发日期 + 天数 - 1
                try (PreparedStatement ps = DBUtil.prepare(c,
                        "INSERT INTO registration (tourist_no, route_no, depart_date, return_date) "
                                + "VALUES (?,?,?, CAST(? AS DATE) + CAST(? AS INTEGER))",
                        touristNo, routeNo, departDate, departDate, days - 1)) {
                    ps.executeUpdate();
                }

                c.commit();               // ===== 全部成功，提交（COMMIT）=====
                req.setAttribute("msg", "报名成功，游客编号：" + touristNo);
            } catch (SQLException ex) {
                c.rollback();             // ===== 任一步失败，回滚（ROLLBACK）=====
                req.setAttribute("err", DBUtil.friendly(ex));
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    private boolean checkRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return com.travel.util.Roles.requireRole(req, resp, com.travel.util.Roles.ADMIN, com.travel.util.Roles.AGENT);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("rows", DBUtil.query(
                    "SELECT tourist_no AS 游客编号, name AS 姓名, gender AS 性别, birth_date AS 出生日期, "
                            + "address AS 住址, phone AS 联系电话 FROM tourist ORDER BY tourist_no"));
            req.setAttribute("regs", DBUtil.query(
                    "SELECT r.reg_no AS 报名编号, t.name AS 姓名, r.route_no AS 线路, "
                            + "r.depart_date AS 出发日期, r.return_date AS 回程日期, r.reg_date AS 报名日期 "
                            + "FROM registration r JOIN tourist t ON t.tourist_no = r.tourist_no "
                            + "ORDER BY r.reg_no DESC"));
            req.setAttribute("routes", DBUtil.query(
                    "SELECT route_no, CONCAT(route_no, ' ', start_place, '→', end_place, ' ', days, '天') AS label "
                            + "FROM route ORDER BY route_no"));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/tourist.jsp").forward(req, resp);
    }
}
