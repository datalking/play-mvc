package com.github.datalking.web.context;

import com.github.datalking.beans.factory.DisposableBean;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

/**
 * @author yaoo on 4/25/18
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

    private ContextLoader contextLoader;

    public ContextLoaderListener(WebApplicationContext context) {
        super(context);
    }

    // 初始化 root web application context
    public void contextInitialized(ServletContextEvent event) {
        this.contextLoader.initWebApplicationContext(event.getServletContext());
    }

    // 关闭 root web application context
    public void contextDestroyed(ServletContextEvent event) {
        if (this.contextLoader != null) {
            this.contextLoader.closeWebApplicationContext(event.getServletContext());
        }
        cleanupAttributes(event.getServletContext());
    }

    private void cleanupAttributes(ServletContext sc) {
        Enumeration attrNames = sc.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();

//            if (attrName.startsWith("org.springframework.")) {
            if (attrName.startsWith("com.github.datalking")) {
                Object attrValue = sc.getAttribute(attrName);
                if (attrValue instanceof DisposableBean) {
                    try {
                        ((DisposableBean) attrValue).destroy();
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }


}
