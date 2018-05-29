package com.github.datalking.web.config;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.io.DefaultResourceLoader;
import com.github.datalking.io.ResourceLoader;
import com.github.datalking.util.Assert;
import com.github.datalking.web.HttpRequestHandler;
import com.github.datalking.web.servlet.handler.AbstractHandlerMapping;
import com.github.datalking.web.servlet.handler.SimpleUrlHandlerMapping;
import com.github.datalking.web.support.ResourceHttpRequestHandler;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 静态资源处理类
 *
 * @author yaoo on 4/27/18
 */
public class ResourceHandlerRegistry {

    private final ServletContext servletContext;

    private final ApplicationContext applicationContext;

    private ResourceLoader resourceLoader;

    private final List<ResourceHandlerRegistration> registrations = new ArrayList<>();

    private int order = Integer.MAX_VALUE - 1;

    public ResourceHandlerRegistry(ApplicationContext applicationContext, ServletContext servletContext) {
        Assert.notNull(applicationContext, "ApplicationContext is required");
        this.applicationContext = applicationContext;
        this.servletContext = servletContext;
        this.resourceLoader = new DefaultResourceLoader();
    }


    public ResourceHandlerRegistration addResourceHandler(String... pathPatterns) {
//        ResourceHandlerRegistration registration = new ResourceHandlerRegistration(applicationContext, pathPatterns);
        ResourceHandlerRegistration registration = new ResourceHandlerRegistration(resourceLoader, pathPatterns);
        registrations.add(registration);
        return registration;
    }


    public ResourceHandlerRegistry setOrder(int order) {
        this.order = order;
        return this;
    }

    protected AbstractHandlerMapping getHandlerMapping() {
        if (registrations.isEmpty()) {
            return null;
        }

        Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<>();
        for (ResourceHandlerRegistration registration : registrations) {
            for (String pathPattern : registration.getPathPatterns()) {
                ResourceHttpRequestHandler requestHandler = registration.getRequestHandler();
                requestHandler.setServletContext(servletContext);
                requestHandler.setApplicationContext(applicationContext);
                urlMap.put(pathPattern, requestHandler);
            }
        }

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(order);
        handlerMapping.setUrlMap(urlMap);
        return handlerMapping;
    }


}
