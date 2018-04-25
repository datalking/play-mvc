package com.github.datalking.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * 在代码中配置ServletContext
 * <p>
 * 任何servlet, listener, 或者filter都可以在WebApplicationInitializer中注册
 *
 * @author yaoo on 4/25/18
 */
public interface WebApplicationInitializer {

    void onStartup(ServletContext servletContext) throws ServletException;
}
