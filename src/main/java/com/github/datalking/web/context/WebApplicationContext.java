package com.github.datalking.web.context;

import com.github.datalking.context.ApplicationContext;

import javax.servlet.ServletContext;

/**
 * web应用只读配置接口
 *
 * @author yaoo on 4/25/18
 */
public interface WebApplicationContext extends ApplicationContext {

    String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

    String SCOPE_REQUEST = "request";

    String SCOPE_SESSION = "session";

    String SCOPE_GLOBAL_SESSION = "globalSession";

    String SCOPE_APPLICATION = "application";

    String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

    String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

    String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";

    /**
     * 获取应用的ServletContext
     */
    ServletContext getServletContext();

}
