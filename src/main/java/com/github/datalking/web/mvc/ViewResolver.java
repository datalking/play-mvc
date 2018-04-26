package com.github.datalking.web.mvc;

/**
 * @author yaoo on 4/25/18
 */
public interface ViewResolver {

    //根据名称解析视图
    View resolveViewName(String viewName);

}
