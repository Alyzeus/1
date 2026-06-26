package com.travel.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** 通用 HTML 表格渲染器：把 List&lt;Map&gt; 结果集渲染为表格，供 JSP 调用 */
public final class Html {

    private Html() {
    }

    /** 把 request 属性安全地转为结果集，为 null 时返回空集合（数据库异常时页面仍可渲染） */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> rows(Object attr) {
        return attr == null ? Collections.<Map<String, Object>>emptyList() : (List<Map<String, Object>>) attr;
    }

    public static String table(List<Map<String, Object>> rows) {
        return table(rows, null, null);
    }

    /**
     * @param rows        查询结果
     * @param actionTitle 操作列标题，null 表示无操作列
     * @param actionHtml  操作列 HTML 模板，其中 {0} 会被替换为该行第一列的值
     */
    public static String table(List<Map<String, Object>> rows, String actionTitle, String actionHtml) {
        if (rows == null || rows.isEmpty()) {
            return "<p class='empty'>（暂无数据）</p>";
        }
        StringBuilder sb = new StringBuilder("<table><tr>");
        for (String k : rows.get(0).keySet()) {
            sb.append("<th>").append(esc(k)).append("</th>");
        }
        if (actionTitle != null) {
            sb.append("<th>").append(esc(actionTitle)).append("</th>");
        }
        sb.append("</tr>");
        for (Map<String, Object> r : rows) {
            sb.append("<tr>");
            Object first = null;
            boolean isFirst = true;
            for (Object v : r.values()) {
                if (isFirst) {
                    first = v;
                    isFirst = false;
                }
                sb.append("<td>").append(v == null ? "" : esc(String.valueOf(v))).append("</td>");
            }
            if (actionTitle != null) {
                sb.append("<td>")
                  .append(actionHtml.replace("{0}", esc(first == null ? "" : String.valueOf(first))))
                  .append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.append("</table>").toString();
    }

    public static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
