package com.github.datalking.web.config;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.support.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * 在代码中配置mvc的主要类
 *
 * @author yaoo on 4/25/18
 */
public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

    private static boolean romePresent = ClassUtils.isPresent("com.sun.syndication.feed.WireFeed", WebMvcConfigurationSupport.class.getClassLoader());


    private static final boolean jackson2Present = ClassUtils.isPresent(
            "com.fasterxml.jackson.databind.ObjectMapper", WebMvcConfigurationSupport.class.getClassLoader()) &&
            ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator",
                    WebMvcConfigurationSupport.class.getClassLoader());


    private ApplicationContext applicationContext;

    private ServletContext servletContext;

    private List<Object> interceptors;

//    private PathMatchConfigurer pathMatchConfigurer;

//    private ContentNegotiationManager contentNegotiationManager;

    private List<HttpMessageConverter<?>> messageConverters;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    }



}
