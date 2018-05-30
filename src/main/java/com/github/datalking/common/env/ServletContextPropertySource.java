package com.github.datalking.common.env;

import com.github.datalking.util.StringUtils;

import javax.servlet.ServletContext;

/**
 * @author yaoo on 5/30/18
 */
public class ServletContextPropertySource extends EnumerablePropertySource<ServletContext> {

    public ServletContextPropertySource(String name, ServletContext servletContext) {
        super(name, servletContext);
    }

    @Override
    public String[] getPropertyNames() {
        return StringUtils.toStringArray(this.source.getInitParameterNames());
    }

    @Override
    public String getProperty(String name) {
        return this.source.getInitParameter(name);
    }

}
