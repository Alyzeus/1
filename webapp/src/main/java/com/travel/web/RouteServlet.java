package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/** 线路管理：增、删、改、查（阶段6 CRUD 示范模块） */
@WebServlet("/route")
public class RouteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!checkRole(req, resp)) return;
        String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
        try {
            if ("del".equals(action)) {
                int n = DBUtil.update("DELETE FROM route WHERE route_no = ?", req.getParameter("no"));
                req.setAttribute("msg", n > 0 ? "删除成功" : "未找到该线路");
            } else if ("edit".equals(action)) {
                // 取一条记录回填到表单
                req.setAttribute("edit", DBUtil.query(
                        "SELECT route_no, start_place, end_place, days, main_spots FROM route WHERE route_no = ?",
                        req.getParameter("no")).stream().findFirst().orElse(null));
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
        try {
            // 新增与修改共用：主键已存在则更新
            DBUtil.update(
                    "INSERT INTO route (route_no, start_place, end_place, days, main_spots) VALUES (?,?,?,?,?) "
                            + "ON DUPLICATE KEY UPDATE start_place=VALUES(start_place), end_place=VALUES(end_place), "
                            + "days=VALUES(days), main_spots=VALUES(main_spots)",
                    req.getParameter("route_no"), req.getParameter("start_place"),
                    req.getParameter("end_place"), req.getParameter("days"), req.getParameter("main_spots"));
            req.setAttribute("msg", "保存成功");
        } catch (SQLException e) {
            req.setAttribute("err", DBUtil.friendly(e));
        }
        list(req, resp);
    }

    /** 角色鉴权：管理员、业务员 */
    private boolean checkRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = (String) req.getSession().getAttribute("role");
        if (!"管理员".equals(role) && !"业务员".equals(role)) {
            resp.sendError(403, "无权限访问");
            return false;
        }
        return true;
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        boolean json = "json".equals(req.getParameter("fmt"));
        try {
            String kw = req.getParameter("kw");
            String sql = "SELECT route_no, start_place, end_place, days, main_spots, "
                       + "route_no AS code, route_no AS route_no, start_place AS start_place, "
                       + "end_place AS end_place, days AS days, main_spots AS spots, "
                       + "days AS days FROM route";
            // 精简查询：返回前端需要的字段
            sql = "SELECT route_no, start_place, end_place, days, main_spots FROM route";
            if (kw != null && !kw.trim().isEmpty()) {
                sql += " WHERE start_place LIKE ? OR end_place LIKE ? OR main_spots LIKE ?";
                String like = "%" + kw.trim() + "%";
                req.setAttribute("rows", DBUtil.query(sql + " ORDER BY route_no", like, like, like));
            } else {
                req.setAttribute("rows", DBUtil.query(sql + " ORDER BY route_no"));
            }
        } catch (SQLException e) {
            if (json) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"ok\":false,\"err\":\"" + DBUtil.friendly(e).replace("\"","\\\"") + "\"}");
                return;
            }
            req.setAttribute("err", DBUtil.friendly(e));
        }
        if (json) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) req.getAttribute("rows");
            if (rows == null) rows = new ArrayList<>();
            String msg = (String) req.getAttribute("msg");
            String err = (String) req.getAttribute("err");
            resp.setContentType("application/json;charset=UTF-8");
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"ok\":").append(err == null);
            if (msg != null) sb.append(",\"msg\":\"").append(msg.replace("\"","\\\"")).append("\"");
            if (err != null) sb.append(",\"err\":\"").append(err.replace("\"","\\\"")).append("\"");
            sb.append(",\"data\":").append(DBUtil.toJson(rows));
            sb.append("}");
            resp.getWriter().write(sb.toString());
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/route.jsp").forward(req, resp);
    }
}
