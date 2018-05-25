package com.github.datalking.web.config;

import com.github.datalking.web.context.request.WebRequestInterceptor;
import com.github.datalking.web.servlet.HandlerInterceptor;
import com.github.datalking.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yaoo on 5/4/18
 */
public class InterceptorRegistry {

    private final List<InterceptorRegistration> registrations = new ArrayList<>();

    public InterceptorRegistration addInterceptor(HandlerInterceptor interceptor) {
        InterceptorRegistration registration = new InterceptorRegistration(interceptor);
        registrations.add(registration);
        return registration;
    }

    public InterceptorRegistration addWebRequestInterceptor(WebRequestInterceptor interceptor) {
        WebRequestHandlerInterceptorAdapter adapted = new WebRequestHandlerInterceptorAdapter(interceptor);
        InterceptorRegistration registration = new InterceptorRegistration(adapted);
        registrations.add(registration);
        return registration;
    }

    protected List<Object> getInterceptors() {
        List<Object> interceptors = new ArrayList<>();
        for (InterceptorRegistration registration : registrations) {
            interceptors.add(registration.getInterceptor());
        }
        return interceptors;
    }

}
