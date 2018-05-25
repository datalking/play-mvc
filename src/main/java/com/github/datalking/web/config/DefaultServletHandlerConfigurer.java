package com.github.datalking.web.config;

import com.github.datalking.util.Assert;
import com.github.datalking.web.HttpRequestHandler;
import com.github.datalking.web.servlet.handler.AbstractHandlerMapping;
import com.github.datalking.web.servlet.handler.SimpleUrlHandlerMapping;
import com.github.datalking.web.support.DefaultServletHttpRequestHandler;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoo on 5/4/18
 */
public class DefaultServletHandlerConfigurer {

    private final ServletContext servletContext;

    private DefaultServletHttpRequestHandler handler;

    public DefaultServletHandlerConfigurer(ServletContext servletContext) {
        Assert.notNull(servletContext, "A ServletContext is required to configure default servlet handling");
        this.servletContext = servletContext;
    }

    public void enable() {
        enable(null);
    }

    public void enable(String defaultServletName) {
        handler = new DefaultServletHttpRequestHandler();
        handler.setDefaultServletName(defaultServletName);
        handler.setServletContext(servletContext);
    }

    protected AbstractHandlerMapping getHandlerMapping() {
        if (handler == null) {
            return null;
        }

        Map<String, HttpRequestHandler> urlMap = new HashMap<>();
        urlMap.put("/**", handler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(Integer.MAX_VALUE);
        handlerMapping.setUrlMap(urlMap);
        return handlerMapping;
    }

}
