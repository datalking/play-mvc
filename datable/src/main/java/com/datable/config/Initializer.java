package com.datable.config;

import com.github.datalking.web.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class Initializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    protected Class<?>[] getRootConfigClasses() {

        return new Class[0];
    }

    protected Class<?>[] getServletConfigClasses() {

        return new Class[]{RootConfig.class};
    }
    protected String[] getServletMappings() {

        return new String[]{"/"};
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        super.onStartup(servletContext);
    }
}
