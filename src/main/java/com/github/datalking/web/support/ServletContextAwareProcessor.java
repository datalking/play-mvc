package com.github.datalking.web.support;

import com.github.datalking.beans.factory.config.BeanPostProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaoo on 4/26/18
 */
public class ServletContextAwareProcessor implements BeanPostProcessor {

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    public ServletContextAwareProcessor(ServletContext servletContext) {
        this(servletContext, null);
    }


    public ServletContextAwareProcessor(ServletConfig servletConfig) {
        this(null, servletConfig);
    }


    public ServletContextAwareProcessor(ServletContext servletContext, ServletConfig servletConfig) {
        this.servletContext = servletContext;
        this.servletConfig = servletConfig;
        if (servletContext == null && servletConfig != null) {
            this.servletContext = servletConfig.getServletContext();
        }
    }


    public Object postProcessBeforeInitialization(Object bean, String beanName)  {
        if (this.servletContext != null && bean instanceof ServletContextAware) {
            ((ServletContextAware) bean).setServletContext(this.servletContext);
        }
        if (this.servletConfig != null && bean instanceof ServletConfigAware) {
            ((ServletConfigAware) bean).setServletConfig(this.servletConfig);
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
