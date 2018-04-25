package com.github.datalking.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * mvc spi接口
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
