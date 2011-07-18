
//
// Copyright 2011, Glen Ford
//
// Apache 2.0 License
// Please see README.md, LICENSE and NOTICE
//


package net.usersource.tomcatembed.tomcat7;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.AprLifecycleListener;


public class Startup {

    private static final String PORT_NAME = "httpPort";
    private static final String INTERACTIVE_NAME = "httpInteractive";
    private static final String DEBUG_NAME = "httpDebug";
    private static final String TEMP_DIR_NAME = "httpTempDir";

    private static boolean debug = false;
    private static boolean isInteractive = false;
    private static String tempDir = null;
    private static int httpPort = 8080;



    private static void processOptions() {
        if( System.getProperty(DEBUG_NAME) != null ) debug = Boolean.getBoolean(DEBUG_NAME);
        if( System.getProperty(INTERACTIVE_NAME) != null ) isInteractive = Boolean.getBoolean(INTERACTIVE_NAME);

        tempDir = System.getProperty(TEMP_DIR_NAME);
        httpPort = Integer.getInteger(PORT_NAME, httpPort);

        if( debug ) {
            System.out.println("==================");
            System.out.println("Tomcat Embed Debug");
            System.out.println("==================");
            System.out.println("Interactive : " + isInteractive );
            System.out.println("HTTP Port : "  + httpPort );
            System.out.println("=================");
        }
    }

    public static void main(String[] args) throws Exception {
        processOptions();

        String appBase = Startup.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(httpPort);

        tomcat.setBaseDir(".");
        tomcat.getHost().setAppBase(appBase);

        String contextPath = "/";

        StandardServer server = (StandardServer)tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);

        tomcat.addWebapp(contextPath, appBase);
        tomcat.start();
        tomcat.getServer().await();
    }

}
