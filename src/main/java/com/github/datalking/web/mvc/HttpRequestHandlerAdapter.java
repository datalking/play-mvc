package com.github.datalking.web.mvc;

import com.github.datalking.web.HttpRequestHandler;
import com.github.datalking.web.servlet.HandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 5/4/18
 */
public class HttpRequestHandlerAdapter implements HandlerAdapter {

    public boolean supports(Object handler) {
        return (handler instanceof HttpRequestHandler);
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        ((HttpRequestHandler) handler).handleRequest(request, response);
        return null;
    }

    public long getLastModified(HttpServletRequest request, Object handler) {
        if (handler instanceof LastModified) {
            return ((LastModified) handler).getLastModified(request);
        }
        return -1L;
    }

}
