package com.github.datalking.context.support;

import com.github.datalking.common.env.MutablePropertySources;
import com.github.datalking.common.env.PropertySource.StubPropertySource;
import com.github.datalking.common.env.StandardEnvironment;
import com.github.datalking.context.ConfigurableWebEnvironment;
import com.github.datalking.util.web.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author yaoo on 5/30/18
 */
public class StandardServletEnvironment extends StandardEnvironment implements ConfigurableWebEnvironment {

    // Servlet context init parameters property source name: {@value}
    public static final String SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams";

    public static final String SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams";

    public static final String JNDI_PROPERTY_SOURCE_NAME = "jndiProperties";

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_PROPERTY_SOURCE_NAME));
        propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
//        if (JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable()) {
//            propertySources.addLast(new JndiPropertySource(JNDI_PROPERTY_SOURCE_NAME));
//        }
        super.customizePropertySources(propertySources);
    }

    /**
     */
    public void initPropertySources(ServletContext servletContext, ServletConfig servletConfig) {

        MutablePropertySources pss = getPropertySources();

        WebApplicationContextUtils.initServletPropertySources(pss, servletContext, servletConfig);
    }

}
