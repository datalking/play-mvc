package com.github.datalking.web.support;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.web.context.AbstractContextLoaderInitializer;
import com.github.datalking.web.context.WebApplicationContext;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

/**
 * @author yaoo on 4/23/18
 */
public abstract class AbstractDispatcherServletInitializer extends AbstractContextLoaderInitializer {

    // 默认servlet名称，可以通过getServletName()覆盖
    public static final String DEFAULT_SERVLET_NAME = "dispatcher";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        registerDispatcherServlet(servletContext);
    }


    protected void registerDispatcherServlet(ServletContext servletContext) {
        String servletName = getServletName();
        Assert.notNull(servletName, "getServletName() may not return empty or null");

        WebApplicationContext servletAppContext = createServletApplicationContext();
        Assert.notNull(servletAppContext, "createServletApplicationContext() did not return an application " +
                "context for servlet [" + servletName + "]");

        DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
        ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, dispatcherServlet);
        Assert.notNull(registration, "Failed to register servlet with name '" + servletName + "'." +
                "Check if there is another servlet registered under the same name.");

        registration.setLoadOnStartup(1);
        registration.addMapping(getServletMappings());
        registration.setAsyncSupported(isAsyncSupported());

        Filter[] filters = getServletFilters();
        if (!ObjectUtils.isEmpty(filters)) {
            for (Filter filter : filters) {
                registerServletFilter(servletContext, filter);
            }
        }

        customizeRegistration(registration);
    }


    protected String getServletName() {
        return DEFAULT_SERVLET_NAME;
    }

    protected abstract WebApplicationContext createServletApplicationContext();


    protected abstract String[] getServletMappings();


    protected Filter[] getServletFilters() {
        return null;
    }


    protected FilterRegistration.Dynamic registerServletFilter(ServletContext servletContext, Filter filter) {
        //String filterName = Conventions.getVariableName(filter);
        // todo 处理数组集合的情况
        Class c = filter.getClass();
        String filterName = ClassUtils.getCamelCaseNameFromClass(c);
        FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, filter);
        registration.setAsyncSupported(isAsyncSupported());
        registration.addMappingForServletNames(getDispatcherTypes(), false, getServletName());
        return registration;
    }

    private EnumSet<DispatcherType> getDispatcherTypes() {
        return isAsyncSupported() ?
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ASYNC) :
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
    }

    protected boolean isAsyncSupported() {
        return true;
    }

    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
    }

}
