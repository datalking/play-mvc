package com.github.datalking.common.env;

import com.github.datalking.util.StringUtils;

import javax.servlet.ServletConfig;

/**
 * @author yaoo on 5/30/18
 */
public class ServletConfigPropertySource extends EnumerablePropertySource<ServletConfig> {

    public ServletConfigPropertySource(String name, ServletConfig servletConfig) {
        super(name, servletConfig);
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
