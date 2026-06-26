package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;

/** 统一 JSON API：为前端 SPA 提供所有数据的 CRUD 接口 */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 登录检查（与 AuthFilter 保持一致）
        String path = req.getPathInfo();
        if (!"/login".equals(path)) {
            String role = (String) req.getSession().getAttribute("role");
            if (role == null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"ok\":false,\"err\":\"未登录\"}");
                return;
            }
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json;charset=UTF-8");
        try {
            String result = handleGet(path, req);
            resp.getWriter().write(result);
        } catch (SQLException e) {
            resp.getWriter().write(jsonError(DBUtil.friendly(e)));
        } catch (Exception e) {
            resp.getWriter().write(jsonError(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json;charset=UTF-8");
        try {
            String result = handlePost(path, req);
            resp.getWriter().write(result);
        } catch (SQLException e) {
            resp.getWriter().write(jsonError(DBUtil.friendly(e)));
        } catch (Exception e) {
            resp.getWriter().write(jsonError(e.getMessage()));
        }
    }

    private String handleGet(String path, HttpServletRequest req) throws SQLException {
        if (path == null) path = "/";

        switch (path) {
            // === 线路 ===
            case "/routes": {
                String kw = req.getParameter("kw");
                String sql = "SELECT route_no, start_place, end_place, days, main_spots FROM route";
                if (kw != null && !kw.trim().isEmpty()) {
                    String like = "%" + kw.trim() + "%";
                    sql += " WHERE start_place LIKE ? OR end_place LIKE ? OR main_spots LIKE ?";
                    return jsonList(DBUtil.query(sql + " ORDER BY route_no", like, like, like));
                }
                return jsonList(DBUtil.query(sql + " ORDER BY route_no"));
            }

            // === 班次 ===
            case "/batches": {
                String sql = "SELECT b.batch_no, b.route_no, r.start_place, r.end_place, "
                           + "b.depart_date, b.return_date, b.standard, b.price, b.discount FROM batch b "
                           + "LEFT JOIN route r ON b.route_no = r.route_no ORDER BY b.batch_no";
                return jsonList(DBUtil.query(sql));
            }

            // === 导游 ===
            case "/guides": {
                String sql = "SELECT guide_no, name, languages, glevel FROM guide ORDER BY guide_no";
                return jsonList(DBUtil.query(sql));
            }

            // === 宾馆 ===
            case "/hotels": {
                String sql = "SELECT hotel_no, hotel_name, city, star, std_price AS price FROM hotel ORDER BY hotel_no";
                return jsonList(DBUtil.query(sql));
            }

            // === 游客档案 ===
            case "/tourists": {
                String sql = "SELECT tourist_no, name, id_card, gender, birth_date, address, phone FROM tourist ORDER BY tourist_no";
                return jsonList(DBUtil.query(sql));
            }

            // === 报名记录 ===
            case "/registrations": {
                String sql = "SELECT reg.reg_no, reg.tourist_no, t.name AS tourist_name, "
                           + "reg.route_no, r.start_place, r.end_place, reg.reg_date, "
                           + "reg.batch_no FROM registration reg "
                           + "LEFT JOIN tourist t ON reg.tourist_no = t.tourist_no "
                           + "LEFT JOIN route r ON reg.route_no = r.route_no ORDER BY reg.reg_no DESC";
                return jsonList(DBUtil.query(sql));
            }

            // === 旅游团 ===
            case "/groups": {
                String sql = "SELECT tg.group_no, tg.group_name, tg.batch_no, "
                           + "b.depart_date, tg.contact_name, tg.contact_phone, "
                           + "tg.contact_addr, tg.actual_people "
                           + "FROM tour_group tg LEFT JOIN batch b ON tg.batch_no = b.batch_no "
                           + "ORDER BY tg.group_no";
                return jsonList(DBUtil.query(sql));
            }

            // === 团员名单 ===
            case "/group_members": {
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

            // === 保险 ===
            case "/insurances": {
                String sql = "SELECT i.policy_no, i.group_no, tg.group_name, "
                           + "i.company, i.insurance_type, i.amount, i.premium_per, "
                           + "i.issue_date FROM insurance i "
                           + "JOIN tour_group tg ON i.group_no = tg.group_no ORDER BY i.policy_no";
                return jsonList(DBUtil.query(sql));
            }

            // === 统计：班次收入 ===
            case "/stats/income": {
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

            // === 统计：导游排班 ===
            case "/stats/guide_schedule": {
                String sql = "SELECT gs.guide_no, g.name AS guide_name, gs.batch_no, "
                           + "b.depart_date, b.return_date FROM batch_guide gs "
                           + "JOIN guide g ON gs.guide_no = g.guide_no "
                           + "JOIN batch b ON gs.batch_no = b.batch_no ORDER BY b.depart_date";
                return jsonList(DBUtil.query(sql));
            }

            // === 统计：宾馆接待 ===
            case "/stats/hotel_reception": {
                String sql = "SELECT bh.hotel_no, h.hotel_name, bh.batch_no, "
                           + "b.depart_date, b.return_date FROM batch_hotel bh "
                           + "JOIN hotel h ON bh.hotel_no = h.hotel_no "
                           + "JOIN batch b ON bh.batch_no = b.batch_no ORDER BY b.depart_date";
                return jsonList(DBUtil.query(sql));
            }

            // === 系统用户 ===
            case "/users": {
                String role = (String) req.getSession().getAttribute("role");
                if (!"管理员".equals(role)) return jsonError("无权限");
                String sql = "SELECT username, realname, role, phone, status, created FROM sys_user ORDER BY username";
                return jsonList(DBUtil.query(sql));
            }

            // === 仪表盘统计 ===
            case "/dashboard/stats": {
                // 本月报名人数
                int signups = DBUtil.query("SELECT COUNT(*) AS cnt FROM registration "
                    + "WHERE MONTH(reg_date) = MONTH(CURDATE()) AND YEAR(reg_date) = YEAR(CURDATE())")
                    .stream().findFirst().map(m -> ((Number)m.get("cnt")).intValue()).orElse(0);
                // 在团游客
                int inGroup = DBUtil.query("SELECT COALESCE(SUM(actual_people),0) AS cnt FROM tour_group")
                    .stream().findFirst().map(m -> ((Number)m.get("cnt")).intValue()).orElse(0);
                // 进行中团次
                int activeGroups = DBUtil.query("SELECT COUNT(*) AS cnt FROM tour_group WHERE actual_people > 0")
                    .stream().findFirst().map(m -> ((Number)m.get("cnt")).intValue()).orElse(0);
                // 本月收入
                double income = DBUtil.query(
                    "SELECT COALESCE(SUM(b.price * b.discount * tg.actual_people), 0) AS total "
                    + "FROM tour_group tg JOIN batch b ON tg.batch_no = b.batch_no "
                    + "WHERE MONTH(b.depart_date) = MONTH(CURDATE()) AND YEAR(b.depart_date) = YEAR(CURDATE())")
                    .stream().findFirst().map(m -> ((Number)m.get("total")).doubleValue()).orElse(0.0);
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("signups", signups);
                stats.put("inGroup", inGroup);
                stats.put("activeGroups", activeGroups);
                stats.put("income", Math.round(income / 100) / 10.0);
                return "{\"ok\":true," + DBUtil.toJsonOne(stats).substring(1);
            }

            default:
                return jsonError("未知 API 路径: " + path);
        }
    }

    private String handlePost(String path, HttpServletRequest req) throws SQLException {
        if (path == null) path = "/";

        switch (path) {
            case "/routes": {
                String no = req.getParameter("route_no");
                DBUtil.update(
                    "INSERT INTO route (route_no, start_place, end_place, days, main_spots) VALUES (?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE start_place=VALUES(start_place), end_place=VALUES(end_place), "
                    + "days=VALUES(days), main_spots=VALUES(main_spots)",
                    no, req.getParameter("start_place"), req.getParameter("end_place"),
                    req.getParameter("days"), req.getParameter("main_spots"));
                return jsonOk("线路保存成功");
            }
            case "/routes/delete": {
                DBUtil.update("DELETE FROM route WHERE route_no = ?", req.getParameter("route_no"));
                return jsonOk("线路删除成功");
            }

            case "/batches": {
                String no = req.getParameter("batch_no");
                DBUtil.update(
                    "INSERT INTO batch (batch_no, route_no, depart_date, return_date, standard, price, discount) "
                    + "VALUES (?,?,?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE route_no=VALUES(route_no), depart_date=VALUES(depart_date), "
                    + "return_date=VALUES(return_date), standard=VALUES(standard), price=VALUES(price), "
                    + "discount=VALUES(discount)",
                    no, req.getParameter("route_no"), req.getParameter("depart_date"),
                    req.getParameter("return_date"), req.getParameter("standard"),
                    req.getParameter("price"), req.getParameter("discount"));
                return jsonOk("班次保存成功");
            }
            case "/batches/delete": {
                DBUtil.update("DELETE FROM batch WHERE batch_no = ?", req.getParameter("batch_no"));
                return jsonOk("班次删除成功");
            }

            case "/guides": {
                String no = req.getParameter("guide_no");
                DBUtil.update(
                    "INSERT INTO guide (guide_no, name, languages, glevel) VALUES (?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE name=VALUES(name), languages=VALUES(languages), glevel=VALUES(glevel)",
                    no, req.getParameter("name"), req.getParameter("languages"), req.getParameter("glevel"));
                return jsonOk("导游保存成功");
            }
            case "/guides/delete": {
                DBUtil.update("DELETE FROM guide WHERE guide_no = ?", req.getParameter("guide_no"));
                return jsonOk("导游删除成功");
            }

            case "/hotels": {
                String no = req.getParameter("hotel_no");
                DBUtil.update(
                    "INSERT INTO hotel (hotel_no, hotel_name, city, star, std_price) VALUES (?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE hotel_name=VALUES(hotel_name), city=VALUES(city), "
                    + "star=VALUES(star), std_price=VALUES(std_price)",
                    no, req.getParameter("hotel_name"), req.getParameter("city"),
                    req.getParameter("star"), req.getParameter("price"));
                return jsonOk("宾馆保存成功");
            }
            case "/hotels/delete": {
                DBUtil.update("DELETE FROM hotel WHERE hotel_no = ?", req.getParameter("hotel_no"));
                return jsonOk("宾馆删除成功");
            }

            case "/tourists": {
                String no = req.getParameter("tourist_no");
                DBUtil.update(
                    "INSERT INTO tourist (tourist_no, name, id_card, gender, birth_date, address, phone) "
                    + "VALUES (?,?,?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE name=VALUES(name), id_card=VALUES(id_card), "
                    + "gender=VALUES(gender), birth_date=VALUES(birth_date), "
                    + "address=VALUES(address), phone=VALUES(phone)",
                    no, req.getParameter("name"), req.getParameter("id_card"),
                    req.getParameter("gender"), req.getParameter("birth_date"),
                    req.getParameter("address"), req.getParameter("phone"));
                return jsonOk("游客保存成功");
            }
            case "/tourists/delete": {
                DBUtil.update("DELETE FROM tourist WHERE tourist_no = ?", req.getParameter("tourist_no"));
                return jsonOk("游客删除成功");
            }

            case "/users": {
                String role = (String) req.getSession().getAttribute("role");
                if (!"管理员".equals(role)) return jsonError("无权限");
                String username = req.getParameter("username");
                DBUtil.update(
                    "INSERT INTO sys_user (username, realname, role, phone, status) VALUES (?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE realname=VALUES(realname), role=VALUES(role), "
                    + "phone=VALUES(phone), status=VALUES(status)",
                    username, req.getParameter("realname"), req.getParameter("role"),
                    req.getParameter("phone"), req.getParameter("status"));
                return jsonOk("用户保存成功");
            }
            case "/users/delete": {
                String role = (String) req.getSession().getAttribute("role");
                if (!"管理员".equals(role)) return jsonError("无权限");
                DBUtil.update("DELETE FROM sys_user WHERE username = ?", req.getParameter("username"));
                return jsonOk("用户删除成功");
            }

            default:
                return jsonError("未知 API 路径: " + path);
        }
    }

    // === JSON 工具方法 ===
    private String jsonList(List<Map<String, Object>> rows) {
        return "{\"ok\":true,\"data\":" + DBUtil.toJson(rows) + "}";
    }

    private String jsonOk(String msg) {
        return "{\"ok\":true,\"msg\":\"" + DBUtil.escJson(msg) + "\"}";
    }

    private String jsonError(String err) {
        return "{\"ok\":false,\"err\":\"" + DBUtil.escJson(err) + "\"}";
    }
}
