package com.bupt.ta;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

/**
 * Embedded Tomcat launcher for starting the application directly via mvn exec:java during local development.
 * This class encapsulates the Tomcat server startup logic, listening on port 8081 by default,
 * and binding to all network interfaces to support LAN access.
 */
public class EmbeddedTomcat {
    /**
     * Program entry point.
     * Creates and configures an embedded Tomcat instance, setting the port, binding address,
     * and web application directory, then starts the server and waits for incoming requests.
     *
     * @param args command-line arguments (currently unused)
     * @throws Exception if the web application directory does not exist or Tomcat fails to start
     */
    public static void main(String[] args) throws Exception {
        // Use port 8081 by default to avoid conflicts.
        int port = 8081;
        // Use absolute path to webapp directory
        String webappDirLocation = new File("src/main/webapp").getAbsolutePath();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector().setProperty("address", "0.0.0.0"); // Bind to all network interfaces so LAN devices can connect

        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            throw new IllegalStateException("Webapp directory not found: " + webappDir.getAbsolutePath());
        }

        // Use empty context path to avoid warning and serve at root '/'
        Context context = tomcat.addWebapp("", webappDir.getAbsolutePath());
        context.setParentClassLoader(EmbeddedTomcat.class.getClassLoader());

        System.out.println("Starting embedded Tomcat on http://0.0.0.0:" + port + "/");
        tomcat.start();
        tomcat.getServer().await();
    }
}
