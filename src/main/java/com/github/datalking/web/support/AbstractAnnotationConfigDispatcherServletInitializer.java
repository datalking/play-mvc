package com.github.datalking.web.support;

import com.github.datalking.util.ObjectUtils;
import com.github.datalking.web.context.AnnotationConfigWebApplicationContext;
import com.github.datalking.web.context.WebApplicationContext;

/**
 * @author yaoo on 4/23/18
 */
public abstract class AbstractAnnotationConfigDispatcherServletInitializer extends AbstractDispatcherServletInitializer {

    /**
     * 根上下文配置类配置
     * 对应web.xml中的ContextLoaderListener
     */
    protected abstract Class<?>[] getRootConfigClasses();

    /**
     * servlet上下文配置类配置
     * 对应web.xml中的DispatcherServlet
     */
    protected abstract Class<?>[] getServletConfigClasses();

    /**
     * 初始化 RootApplicationContext
     */
    @Override
    protected WebApplicationContext createRootApplicationContext() {
        Class<?>[] configClasses = getRootConfigClasses();
        if (!ObjectUtils.isEmpty(configClasses)) {
            AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
            rootAppContext.register(configClasses);
            return rootAppContext;
        } else {
            return null;
        }
    }

    /**
     * 初始化 ServletApplicationContext
     */
    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext servletAppContext = new AnnotationConfigWebApplicationContext();
        Class<?>[] configClasses = getServletConfigClasses();
        if (!ObjectUtils.isEmpty(configClasses)) {
            servletAppContext.register(configClasses);
        }
        return servletAppContext;
    }


}
