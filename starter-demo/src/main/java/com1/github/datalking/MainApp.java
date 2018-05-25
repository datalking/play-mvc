package com1.github.datalking;

import java.io.File;
import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * @author yaoo on 4/24/18
 */
public class MainApp {

    public static void main(String[] args) throws ServletException, LifecycleException {

        String webappDirLoc = "starter-demo/src/main/webapp/";
        File webInfClasses = new File("target/classes");

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8999);
        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLoc).getAbsolutePath());
        ctx.setReloadable(false);
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
                new DirResourceSet(
                        resources,
                        "/WEB-INF/classes",
                        webInfClasses.getAbsolutePath(),
                        "/")
        );

        tomcat.start();
        tomcat.getServer().await();

    }

}
