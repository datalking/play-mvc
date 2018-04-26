package com.github.datalking.web.context;

import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.context.support.AbstractApplicationContext;
import com.github.datalking.web.support.ServletContextAwareProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaoo on 4/25/18
 */
public abstract class AbstractWebApplicationContext
        extends AbstractApplicationContext implements ConfigurableWebApplicationContext {

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    private String namespace;

    public AbstractWebApplicationContext() {
        setDisplayName("Root WebApplicationContext");
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        if (servletConfig != null && this.servletContext == null) {
            setServletContext(servletConfig.getServletContext());
        }
    }

    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null) {
            setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
        }
    }

    public String getNamespace() {
        return this.namespace;
    }


    public String getApplicationName() {
        if (this.servletContext == null) {
            return "";
        }
        if (this.servletContext.getMajorVersion() == 2 && this.servletContext.getMinorVersion() < 5) {
            String name = this.servletContext.getServletContextName();
            return (name != null ? name : "");
        } else {
            return this.servletContext.getContextPath();
        }
    }

    //@Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
//        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
//        beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

        //WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
        // WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
    }


}
