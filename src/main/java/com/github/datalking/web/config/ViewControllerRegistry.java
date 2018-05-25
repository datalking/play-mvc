package com.github.datalking.web.config;

import com.github.datalking.web.servlet.handler.AbstractHandlerMapping;
import com.github.datalking.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 5/4/18
 */
public class ViewControllerRegistry {

    private final List<ViewControllerRegistration> registrations = new ArrayList<>();

    private int order = 1;

    public ViewControllerRegistration addViewController(String urlPath) {
        ViewControllerRegistration registration = new ViewControllerRegistration(urlPath);
        registrations.add(registration);
        return registration;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    protected AbstractHandlerMapping getHandlerMapping() {
        if (registrations.isEmpty()) {
            return null;
        }

        Map<String, Object> urlMap = new LinkedHashMap<>();
        for (ViewControllerRegistration registration : registrations) {
            urlMap.put(registration.getUrlPath(), registration.getViewController());
        }

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(order);
        handlerMapping.setUrlMap(urlMap);
        return handlerMapping;
    }

}
