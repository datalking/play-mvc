package com.github.datalking.web.servlet;

import com.github.datalking.web.mvc.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理器适配器，用于支持多种类型的处理器
 * 适配到handler对象的具体某个处理方法上，如controller中的方法，并返回ModelAndView
 *
 * @author yaoo on 4/25/18
 */
public interface HandlerAdapter {

    // 判断此HandlerAdapter是否支持输入的handler
    boolean supports(Object handler);

    // 使用handler处理request
    ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler);

    // 与HttpServlet的getLastModified()相同
    long getLastModified(HttpServletRequest request, Object handler);

}
