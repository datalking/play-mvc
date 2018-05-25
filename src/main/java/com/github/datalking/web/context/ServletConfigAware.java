package com.github.datalking.web.context;

import com.github.datalking.beans.factory.Aware;

import javax.servlet.ServletConfig;

/**
 * @author yaoo on 4/26/18
 */
public interface ServletConfigAware extends Aware {

    void setServletConfig(ServletConfig servletConfig);

}
