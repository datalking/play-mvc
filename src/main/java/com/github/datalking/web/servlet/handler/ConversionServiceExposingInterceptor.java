package com.github.datalking.web.servlet.handler;

import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yaoo on 5/4/18
 */
public class ConversionServiceExposingInterceptor extends HandlerInterceptorAdapter {

    private final ConversionService conversionService;

    public ConversionServiceExposingInterceptor(ConversionService conversionService) {
        Assert.notNull(conversionService, "The ConversionService may not be null");
        this.conversionService = conversionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {

        request.setAttribute(ConversionService.class.getName(), this.conversionService);

        return true;
    }

}
