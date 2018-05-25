package com.github.datalking.web.servlet;

import com.github.datalking.web.mvc.View;

/**
 * 把ModelAndView解析为具体的视图，返回具体的View
 *
 * @author yaoo on 4/25/18
 */
public interface ViewResolver {

    //根据名称解析视图
    View resolveViewName(String viewName);

}
