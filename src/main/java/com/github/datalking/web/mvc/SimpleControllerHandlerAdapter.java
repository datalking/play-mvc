package com.github.datalking.web.mvc;

import com.github.datalking.web.servlet.HandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 匹配@Controller标注的类中的方法
 *
 * @author yaoo on 5/4/18
 */
public class SimpleControllerHandlerAdapter implements HandlerAdapter {

    public boolean supports(Object handler) {

        return (handler instanceof Controller);
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        return ((Controller) handler).handleRequest(request, response);
    }

    public long getLastModified(HttpServletRequest request, Object handler) {

        if (handler instanceof LastModified) {

            return ((LastModified) handler).getLastModified(request);
        }

        return -1L;
    }

}
