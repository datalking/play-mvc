package com.github.datalking.web.context;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ConfigurableApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaoo on 4/25/18
 */
public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

    String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";

    String SERVLET_CONFIG_BEAN_NAME = "servletConfig";

    void setServletContext(ServletContext servletContext);

    ServletConfig getServletConfig();

    void setServletConfig(ServletConfig servletConfig);

    void setNamespace(String namespace);

    String getNamespace();

//    String[] getConfigLocations();
//    void setConfigLocations(String[] configLocations);


}
