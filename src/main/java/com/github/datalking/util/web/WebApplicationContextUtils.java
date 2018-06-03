package com.github.datalking.util.web;

import com.github.datalking.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.datalking.common.env.MutablePropertySources;
import com.github.datalking.common.env.PropertySource.StubPropertySource;
import com.github.datalking.common.env.ServletConfigPropertySource;
import com.github.datalking.common.env.ServletContextPropertySource;
import com.github.datalking.context.support.StandardServletEnvironment;
import com.github.datalking.util.Assert;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.context.request.RequestScope;
import com.github.datalking.web.context.request.SessionScope;
import com.github.datalking.web.support.ServletContextScope;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaoo on 4/25/18
 */
public abstract class WebApplicationContextUtils {

    public static WebApplicationContext getWebApplicationContext(ServletContext sc) {
        return getWebApplicationContext(sc, WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }

    public static WebApplicationContext getWebApplicationContext(ServletContext sc, String attrName) {
        Assert.notNull(sc, "ServletContext must not be null");

        Object attr = sc.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (attr instanceof RuntimeException) {
            throw (RuntimeException) attr;
        }
        if (attr instanceof Error) {
            throw (Error) attr;
        }
        if (attr instanceof Exception) {
            throw new IllegalStateException((Exception) attr);
        }
        if (!(attr instanceof WebApplicationContext)) {
            throw new IllegalStateException("Context attribute is not of type WebApplicationContext: " + attr);
        }
        return (WebApplicationContext) attr;
    }

    public static WebApplicationContext getRequiredWebApplicationContext(ServletContext sc) throws IllegalStateException {
        WebApplicationContext wac = getWebApplicationContext(sc);
        if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
        }
        return wac;
    }

    // 将propertySources中的servletContextInitParams和servletConfigInitParams替换为ServletCon***PropertySource
    public static void initServletPropertySources(MutablePropertySources propertySources,
                                                  ServletContext servletContext,
                                                  ServletConfig servletConfig) {

        Assert.notNull(propertySources, "propertySources must not be null");

        if (servletContext != null
                && propertySources.contains(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME)
                && propertySources.get(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME) instanceof StubPropertySource) {

            propertySources.replace(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME,
                    new ServletContextPropertySource(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME, servletContext));
        }

        if (servletConfig != null
                && propertySources.contains(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME)
                && propertySources.get(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME) instanceof StubPropertySource) {

            propertySources.replace(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME,
                    new ServletConfigPropertySource(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME, servletConfig));
        }
    }

    public static void registerWebApplicationScopes(ConfigurableListableBeanFactory beanFactory, ServletContext sc) {
        beanFactory.registerScope(WebApplicationContext.SCOPE_REQUEST, new RequestScope());
        beanFactory.registerScope(WebApplicationContext.SCOPE_SESSION, new SessionScope(false));
        beanFactory.registerScope(WebApplicationContext.SCOPE_GLOBAL_SESSION, new SessionScope(true));
        if (sc != null) {
            ServletContextScope appScope = new ServletContextScope(sc);
            beanFactory.registerScope(WebApplicationContext.SCOPE_APPLICATION, appScope);
            // Register as ServletContext attribute, for ContextCleanupListener to detect it.
            sc.setAttribute(ServletContextScope.class.getName(), appScope);
        }

//        beanFactory.registerResolvableDependency(ServletRequest.class, new RequestObjectFactory());
//        beanFactory.registerResolvableDependency(ServletResponse.class, new ResponseObjectFactory());
//        beanFactory.registerResolvableDependency(HttpSession.class, new SessionObjectFactory());
//        beanFactory.registerResolvableDependency(WebRequest.class, new WebRequestObjectFactory());
    }
}
