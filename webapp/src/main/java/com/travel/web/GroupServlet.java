package com.travel.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/**
 * 旅游团管理：建团、入团/退团、成团校验、团费结算。
 * 入团人数上限、人数自动维护由数据库触发器保证；
 * 成团校验、团费结算通过 CallableStatement 调用存储过程实现。
 */
@WebServlet("/group")
public class GroupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
        String no = req.getParameter("no");
        try {
            switch (action) {
                case "detail":
                    detail(req, resp, no);
                    return;
                case "quit":
                    DBUtil.update("DELETE FROM group_member WHERE group_no=? AND tourist_no=?",
                            no, req.getParameter("tno"));
                    req.setAttribute("msg", "退团成功（团人数已由触发器自动减 1）");
                    detail(req, resp, no);
                    return;
                case "confirm":
                    call(req, "{call sp_confirm_group(?)}", no);
                    if (req.getAttribute("err") == null) {
                        req.setAttribute("msg", "团 " + no + " 成团校验通过（20~50人）");
                        detail(req, resp, no);
                        return;
                    }
                    break;
                case "fee":
                    fee(req, no);
                    if (req.getAttribute("err") == null) {
                        detail(req, resp, no);
                        return;
                    }
                    break;
                default:
            }
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                // 联系人电话格式校验
                String cphone = req.getParameter("contact_phone");
                if (cphone != null && !cphone.trim().isEmpty()) {
                    String p = cphone.trim();
                    if (!p.matches("1[3-9]\\d{9}") && !p.matches("0\\d{2,3}-\\d{7,8}")) {
                        req.setAttribute("err", "联系人电话格式不正确（应为11位手机号或区号-号码格式）");
                        list(req, resp);
                        return;
                    }
                }
                DBUtil.update(
                        "INSERT INTO tour_group (group_no, batch_no, group_name, contact_name, contact_addr, contact_phone) "
                                + "VALUES (?,?,?,?,?,?)",
                        req.getParameter("group_no"), req.getParameter("batch_no"),
                        req.getParameter("group_name"), req.getParameter("contact_name"),
                        req.getParameter("contact_addr"), req.getParameter("contact_phone"));
                req.setAttribute("msg", "建团成功");
                list(req, resp);
                return;
            }
            if ("join".equals(action)) {
                String no = req.getParameter("no");
                call(req, "{call sp_join_group(?,?)}", no, req.getParameter("tno"));
                if (req.getAttribute("err") == null) {
                    req.setAttribute("msg", "入团成功（团人数已由触发器自动加 1）");
                }
                detail(req, resp, no);
                return;
            }
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    private boolean checkRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = (String) req.getSession().getAttribute("role");
        if (!"管理员".equals(role) && !"业务员".equals(role)) {
            resp.sendError(403, "无权限访问");
            return false;
        }
        return true;
    }

    /** 调用无 OUT 参数的存储过程，业务规则错误转为友好提示 */
    private void call(HttpServletRequest req, String sql, String... args) {
        try (Connection c = DBUtil.getConnection(); CallableStatement cs = c.prepareCall(sql)) {
            for (int i = 0; i < args.length; i++) {
                cs.setString(i + 1, args[i]);
            }
            cs.execute();
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
    }

    /** 团费结算：调用带 OUT 参数的存储过程 sp_calc_group_fee */
    private void fee(HttpServletRequest req, String no) {
        try (Connection c = DBUtil.getConnection();
             CallableStatement cs = c.prepareCall("{call sp_calc_group_fee(?,?)}")) {
            cs.setString(1, no);
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.execute();
            BigDecimal total = cs.getBigDecimal(2);
            req.setAttribute("msg", "团 " + no + " 应收团费合计：" + total + " 元"
                    + "（报价×折扣率×人数 + 人均保险费×人数）");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("rows", DBUtil.query("SELECT * FROM v_group_overview ORDER BY 团号"));
            req.setAttribute("batches", DBUtil.query(
                    "SELECT batch_no, CONCAT(batch_no, ' ', depart_date) AS label FROM batch ORDER BY depart_date"));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/group.jsp").forward(req, resp);
    }

    private void detail(HttpServletRequest req, HttpServletResponse resp, String no)
            throws ServletException, IOException {
        try {
            req.setAttribute("groupNo", no);
            req.setAttribute("roster", DBUtil.query(
                    "SELECT 游客编号, 姓名, 性别, 联系电话, 入团日期 FROM v_group_roster "
                            + "WHERE 团号 = ? ORDER BY 游客编号", no));
            req.setAttribute("candidates", DBUtil.query(
                    "SELECT t.tourist_no, CONCAT(t.tourist_no, ' ', t.name) AS label FROM tourist t "
                            + "WHERE t.tourist_no NOT IN (SELECT tourist_no FROM group_member WHERE group_no = ?) "
                            + "ORDER BY t.tourist_no", no));
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        req.getRequestDispatcher("/WEB-INF/jsp/group_detail.jsp").forward(req, resp);
    }
}
