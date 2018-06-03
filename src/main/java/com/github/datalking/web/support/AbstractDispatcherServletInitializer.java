package com.github.datalking.web.support;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.web.context.AbstractContextLoaderInitializer;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

/**
 * 初始化ServletApplicationContext 抽象类
 * 初始化前端控制器DispatcherServlet
 *
 * @author yaoo on 4/23/18
 */
public abstract class AbstractDispatcherServletInitializer extends AbstractContextLoaderInitializer {

    // 默认servlet名称，可以通过getServletName()覆盖
    public static final String DEFAULT_SERVLET_NAME = "dispatcher";

    // 创建ServletApplicationContext抽象方法
    protected abstract WebApplicationContext createServletApplicationContext();

    /**
     * 获取默认servlet即DispatcherServlet的映射配置
     */
    protected abstract String[] getServletMappings();

    protected Filter[] getServletFilters() {
        return null;
    }

    // 获取默认servlet名，默认为dispatcher
    protected String getServletName() {
        return DEFAULT_SERVLET_NAME;
    }

    /**
     * servlet容器会调用这个方法
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        // 初始化RootApplicationContext
        super.onStartup(servletContext);

        // 初始化ServletApplicationContext，再创建并注册DispatcherServlet
        registerDispatcherServlet(servletContext);
    }


    protected void registerDispatcherServlet(ServletContext servletContext) {

        String servletName = getServletName();
        Assert.notNull(servletName, "getServletName() may not return empty or null");

        // ==== 初始化ServletApplicationContext
        WebApplicationContext servletAppContext = createServletApplicationContext();
        Assert.notNull(servletAppContext, "createServletApplicationContext() cannot be null for servlet [" + servletName + "]");

        // ==== 初始化
        DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);

        // 注册servlet组件
        ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, dispatcherServlet);
        Assert.notNull(registration, "Failed to register servlet '" + servletName + "'. Check if same name.");

        registration.setLoadOnStartup(1);
        // 配置servlet的映射信息
        registration.addMapping(getServletMappings());
        registration.setAsyncSupported(isAsyncSupported());

        // 注册Filter并配置映射信息
        Filter[] filters = getServletFilters();
        if (!ObjectUtils.isEmpty(filters)) {
            for (Filter filter : filters) {
                registerServletFilter(servletContext, filter);
            }
        }

        // 注册Listener
        //servletContext.addListener(UserListener.class);

        // 空实现
        customizeRegistration(registration);
    }


    protected FilterRegistration.Dynamic registerServletFilter(ServletContext servletContext, Filter filter) {
//        String filterName = Conventions.getVariableName(filter);
        // todo 处理数组集合的情况
        Class c = filter.getClass();
        String filterName = ClassUtils.getCamelCaseNameFromClass(c);

        FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, filter);
        registration.setAsyncSupported(isAsyncSupported());
        // 配置filter映射信息
        registration.addMappingForServletNames(getDispatcherTypes(), false, getServletName());
        return registration;
    }

    private EnumSet<DispatcherType> getDispatcherTypes() {
        return isAsyncSupported() ?
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ASYNC) :
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
    }

    protected boolean isAsyncSupported() {
        //return true;
        return false;
    }

    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
    }

}
