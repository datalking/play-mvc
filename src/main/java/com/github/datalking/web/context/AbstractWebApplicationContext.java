package com.github.datalking.web.context;

import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.common.env.ConfigurableEnvironment;
import com.github.datalking.context.ConfigurableWebEnvironment;
import com.github.datalking.context.support.AbstractApplicationContext;
import com.github.datalking.context.support.StandardServletEnvironment;
import com.github.datalking.web.support.ServletContextAwareProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoo on 4/25/18
 */
public abstract class AbstractWebApplicationContext extends AbstractApplicationContext
        implements ConfigurableWebApplicationContext {

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    private String namespace;

    public AbstractWebApplicationContext() {
        setDisplayName("Root WebApplicationContext");
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        if (servletConfig != null && this.servletContext == null) {
            setServletContext(servletConfig.getServletContext());
        }
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
        if (namespace != null) {
            setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
        }
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
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

    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new StandardServletEnvironment();
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
//        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
//        beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

        //WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
        // 将servletContext和servletConfig注册到singletonObjects
        registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
    }

    private void registerEnvironmentBeans(ConfigurableListableBeanFactory bf, ServletContext servletContext, ServletConfig servletConfig) {

        if (servletContext != null && !bf.containsBean(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME)) {
            bf.registerSingleton(WebApplicationContext.SERVLET_CONTEXT_BEAN_NAME, servletContext);
        }

        if (servletConfig != null && !bf.containsBean(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME)) {
            bf.registerSingleton(ConfigurableWebApplicationContext.SERVLET_CONFIG_BEAN_NAME, servletConfig);
        }

        if (!bf.containsBean(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME)) {
            Map<String, String> parameterMap = new HashMap<>();
            if (servletContext != null) {
                Enumeration<?> paramNameEnum = servletContext.getInitParameterNames();
                while (paramNameEnum.hasMoreElements()) {
                    String paramName = (String) paramNameEnum.nextElement();
                    parameterMap.put(paramName, servletContext.getInitParameter(paramName));
                }
            }
            if (servletConfig != null) {
                Enumeration<?> paramNameEnum = servletConfig.getInitParameterNames();
                while (paramNameEnum.hasMoreElements()) {
                    String paramName = (String) paramNameEnum.nextElement();
                    parameterMap.put(paramName, servletConfig.getInitParameter(paramName));
                }
            }
            bf.registerSingleton(WebApplicationContext.CONTEXT_PARAMETERS_BEAN_NAME, Collections.unmodifiableMap(parameterMap));
        }

        if (!bf.containsBean(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME)) {
            Map<String, Object> attributeMap = new HashMap<>();
            if (servletContext != null) {
                Enumeration<?> attrNameEnum = servletContext.getAttributeNames();
                while (attrNameEnum.hasMoreElements()) {
                    String attrName = (String) attrNameEnum.nextElement();
                    attributeMap.put(attrName, servletContext.getAttribute(attrName));
                }
            }
            bf.registerSingleton(WebApplicationContext.CONTEXT_ATTRIBUTES_BEAN_NAME, Collections.unmodifiableMap(attributeMap));
        }
    }

    @Override
    protected void initPropertySources() {
        ConfigurableEnvironment env = getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, this.servletConfig);
        }
    }

}
