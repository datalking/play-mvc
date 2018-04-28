package com.github.datalking.web.servlet;

import com.github.datalking.web.mvc.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 4/28/18
 */
public interface HandlerExceptionResolver {

    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);

}
