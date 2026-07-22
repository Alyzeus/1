package com.travel.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.travel.util.DBUtil;
import com.travel.web.handler.*;

/** 统一 REST JSON API 调度器 — 将请求分发到各领域 Handler */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private final RouteHandler      routeH      = new RouteHandler();
    private final BatchHandler      batchH      = new BatchHandler();
    private final GuideHandler      guideH      = new GuideHandler();
    private final HotelHandler      hotelH      = new HotelHandler();
    private final TouristHandler    touristH    = new TouristHandler();
    private final RegGroupInsHandler regGrpInsH = new RegGroupInsHandler();
    private final StatsHandler      statsH      = new StatsHandler();
    private final UserHandler       userH       = new UserHandler();
    private final DashboardHandler  dashH       = new DashboardHandler();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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
        if (path == null) path = "/";
        resp.setContentType("application/json;charset=UTF-8");
        try {
            resp.getWriter().write(dispatchGet(path, req));
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
        if (path == null) path = "/";
        resp.setContentType("application/json;charset=UTF-8");
        // CSRF 防护
        if (!"/login".equals(path)) {
            String sessionToken = (String) req.getSession().getAttribute("csrf_token");
            String reqToken = req.getHeader("X-CSRF-Token");
            if (sessionToken == null || !sessionToken.equals(reqToken)) {
                resp.getWriter().write(jsonError("CSRF 校验失败，请刷新页面重试"));
                return;
            }
        }
        try {
            resp.getWriter().write(dispatchPost(path, req));
        } catch (SQLException e) {
            resp.getWriter().write(jsonError(DBUtil.friendly(e)));
        } catch (Exception e) {
            resp.getWriter().write(jsonError(e.getMessage()));
        }
    }

    private String dispatchGet(String path, HttpServletRequest req) throws SQLException {
        switch (path) {
            case "/routes":           return routeH.handleGet(req);
            case "/batches":          return batchH.handleGet(req);
            case "/guides":           return guideH.handleGet(req);
            case "/hotels":           return hotelH.handleGet(req);
            case "/tourists":         return touristH.handleGet(req);
            case "/registrations":    return regGrpInsH.handleRegistrations(req);
            case "/groups":           return regGrpInsH.handleGroups(req);
            case "/group_members":    return regGrpInsH.handleGroupMembers(req);
            case "/insurances":       return regGrpInsH.handleInsurances(req);
            case "/stats/income":     return statsH.handleIncome(req);
            case "/stats/guide_schedule": return statsH.handleGuideSchedule(req);
            case "/stats/hotel_reception":return statsH.handleHotelReception(req);
            case "/users":            return userH.handleGet(req);
            case "/dashboard/stats":  return dashH.handleGet(req);
            default:                  return jsonError("未知 API 路径: " + path);
        }
    }

    private String dispatchPost(String path, HttpServletRequest req) throws SQLException {
        switch (path) {
            case "/routes":           return routeH.handlePost(req);
            case "/routes/delete":    return routeH.handleDelete(req);
            case "/batches":          return batchH.handlePost(req);
            case "/batches/delete":   return batchH.handleDelete(req);
            case "/guides":           return guideH.handlePost(req);
            case "/guides/delete":    return guideH.handleDelete(req);
            case "/hotels":           return hotelH.handlePost(req);
            case "/hotels/delete":    return hotelH.handleDelete(req);
            case "/tourists":         return touristH.handlePost(req);
            case "/tourists/delete":  return touristH.handleDelete(req);
            case "/users":            return userH.handlePost(req);
            case "/users/delete":     return userH.handleDelete(req);
            default:                  return jsonError("未知 API 路径: " + path);
        }
    }

    private static String jsonError(String err) {
        return "{\"ok\":false,\"err\":\"" + DBUtil.escJson(err) + "\"}";
    }
}
