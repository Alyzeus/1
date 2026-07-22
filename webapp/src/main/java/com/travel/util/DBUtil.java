package com.travel.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 数据库连接与通用 JDBC 工具（HikariCP 连接池）。
 * 连接参数在 src/main/resources/db.properties 中配置。
 */
public final class DBUtil {

    private static final Properties P = new Properties();
    private static HikariDataSource ds;

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            P.load(in);
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(P.getProperty("jdbc.url"));
            config.setUsername(P.getProperty("jdbc.user"));
            config.setPassword(P.getProperty("jdbc.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(600000);
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /** 查询并把结果集转成 List&lt;Map&gt;，列名保留 SQL 中的别名（含中文） */
    public static List<Map<String, Object>> query(String sql, Object... args) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = prepare(c, sql, args);
             ResultSet rs = ps.executeQuery()) {
            return readAll(rs);
        }
    }

    public static List<Map<String, Object>> readAll(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    public static int update(String sql, Object... args) throws SQLException {
        try (Connection c = getConnection(); PreparedStatement ps = prepare(c, sql, args)) {
            return ps.executeUpdate();
        }
    }

    public static PreparedStatement prepare(Connection c, String sql, Object... args) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
        return ps;
    }

    /** 将 List&lt;Map&gt; 序列化为 JSON 字符串（轻量实现，无第三方依赖） */
    public static String toJson(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder("[");
        for (int r = 0; r < rows.size(); r++) {
            if (r > 0) sb.append(",");
            Map<String, Object> row = rows.get(r);
            sb.append("{");
            int i = 0;
            for (Map.Entry<String, Object> e : row.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(escJson(e.getKey())).append("\":");
                Object v = e.getValue();
                if (v == null) sb.append("null");
                else if (v instanceof Number) sb.append(v.toString());
                else if (v instanceof Boolean) sb.append(v.toString());
                else sb.append("\"").append(escJson(v.toString())).append("\"");
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    /** 将单个 Map 序列化为 JSON 对象 */
    public static String toJsonOne(Map<String, Object> row) {
        if (row == null) return "null";
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(row);
        return toJson(list).replaceAll("^\\[|\\]$", "");
    }

    /** JSON 字符串转义 */
    public static String escJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /** 把常见 SQL 异常翻译成友好的中文提示 */
    public static String friendly(SQLException e) {
        String state = e.getSQLState();
        if ("23505".equals(state)) {
            return "操作失败：主键或唯一约束冲突（该记录已存在）";
        }
        if ("23503".equals(state)) {
            return "操作失败：该记录已被其他数据引用，禁止删除（外键约束）";
        }
        if ("23514".equals(state)) {
            return "操作失败：数据不满足 CHECK 约束（请检查取值范围）";
        }
        if ("P0001".equals(state)) {
            return e.getMessage();
        }
        return "数据库错误[" + state + "]：" + e.getMessage();
    }
}
