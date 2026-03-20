package com.bupt.ta;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * 嵌入式 Tomcat 启动器，用于本地开发时直接通过 mvn exec:java 启动应用。
 */
public class EmbeddedTomcat {

    public static void main(String[] args) throws Exception {
        // Use port 8080 by default.
        int port = 8080;
        String webappDirLocation = "src/main/webapp";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Ensure the connector is created

        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            throw new IllegalStateException("Webapp directory not found: " + webappDir.getAbsolutePath());
        }

        // Use empty context path to avoid warning and serve at root '/'
        Context context = tomcat.addWebapp("", webappDir.getAbsolutePath());
        context.setParentClassLoader(EmbeddedTomcat.class.getClassLoader());

        System.out.println("Starting embedded Tomcat on http://localhost:" + port + "/");
        tomcat.start();
        tomcat.getServer().await();
    }
}
