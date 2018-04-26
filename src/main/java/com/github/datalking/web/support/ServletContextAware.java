package com.github.datalking.web.support;

import com.github.datalking.beans.factory.Aware;

import javax.servlet.ServletContext;

/**
 * @author yaoo on 4/26/18
 */
public interface ServletContextAware extends Aware {

    void setServletContext(ServletContext servletContext);

}
