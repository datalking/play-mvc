package com.github.datalking.web.support;

import com.github.datalking.util.ObjectUtils;
import com.github.datalking.web.context.AnnotationConfigWebApplicationContext;
import com.github.datalking.web.context.WebApplicationContext;

/**
 * @author yaoo on 4/23/18
 */
public abstract class AbstractAnnotationConfigDispatcherServletInitializer extends AbstractDispatcherServletInitializer {

    // 获取 @Configuration或@Component类，并提供给createRootApplicationContext()
    protected abstract Class<?>[] getRootConfigClasses();

    // 获取 @Configuration或@Component类，并提供给createServletApplicationContext()
    protected abstract Class<?>[] getServletConfigClasses();

    /**
     * 初始化 AnnotationConfigWebApplicationContext
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
     * 初始化 AnnotationConfigWebApplicationContext
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
