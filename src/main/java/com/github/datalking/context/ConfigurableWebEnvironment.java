package com.github.datalking.context;

import com.github.datalking.common.env.ConfigurableEnvironment;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaoo on 5/30/18
 */
public interface ConfigurableWebEnvironment extends ConfigurableEnvironment {

    void initPropertySources(ServletContext servletContext, ServletConfig servletConfig);

}
