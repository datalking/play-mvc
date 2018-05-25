package com.github.datalking.web.support;

import com.github.datalking.util.ObjectUtils;
import com.github.datalking.web.context.AnnotationConfigWebApplicationContext;
import com.github.datalking.web.context.WebApplicationContext;

/**
 * 实现WebApplicationInitializer接口的抽象类，此抽象类的实现类会被servlet容器启动时调用
 * <p>
 * 初始化容器RootApplicationContext和ServletApplicationContext
 * 初始化前端控制器DispatcherServlet
 *
 * @author yaoo on 4/23/18
 */
public abstract class AbstractAnnotationConfigDispatcherServletInitializer extends AbstractDispatcherServletInitializer {

    /**
     * RootApplicationContext配置类，扫描普通Bean，对应web.xml中的ContextLoaderListener
     */
    protected abstract Class<?>[] getRootConfigClasses();

    /**
     * ServletApplicationContext配置类，扫描web相关配置类，对应web.xml中的DispatcherServlet
     */
    protected abstract Class<?>[] getServletConfigClasses();

    /**
     * 初始化 RootApplicationContext，获取 getRootConfigClasses()
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
     * 初始化 ServletApplicationContext，获取 getServletConfigClasses()
     */
    @Override
    protected WebApplicationContext createServletApplicationContext() {

        Class<?>[] configClasses = getServletConfigClasses();

        AnnotationConfigWebApplicationContext servletAppContext = new AnnotationConfigWebApplicationContext();

        if (!ObjectUtils.isEmpty(configClasses)) {
            servletAppContext.register(configClasses);
        }

        return servletAppContext;
    }


}
