package com.travel;

import java.io.File;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * 内嵌 Tomcat 启动入口。
 * 在 webapp 目录下执行：mvn compile exec:java
 * 默认端口 8080，可用 -Dport=8090 改端口。启动后访问 http://localhost:端口/
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector().setURIEncoding("UTF-8");

        String webDir = new File("src/main/webapp").getAbsolutePath();
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", webDir);
        // exec:java 下依赖在 Maven 插件类加载器中，必须显式指定父类加载器，
        // 否则 webapp 类加载器找不到 Servlet API
        ctx.setParentClassLoader(Main.class.getClassLoader());

        // 配置 JSP 编码为 UTF-8，解决中文乱码
        ctx.addParameter("jspEncoding", "UTF-8");
        ctx.addParameter("defaultJspEncoding", "UTF-8");
        ctx.addParameter("jsp-file-encoding", "UTF-8");
        // 把 target/classes 挂到 /WEB-INF/classes，使 @WebServlet 注解能被扫描到
        File classes = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        // 禁用静态资源缓存，确保 index.html 每次读取最新文件内容
        resources.setCachingAllowed(false);
        resources.addPreResources(new DirResourceSet(
                resources, "/WEB-INF/classes", classes.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        System.out.println(">>> 旅行社旅游管理系统已启动：http://localhost:" + port + "/");
        tomcat.getServer().await();
    }
}
