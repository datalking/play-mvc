package com.github.datalking.web.servlet;

import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.web.context.WebApplicationContext;

import java.util.ArrayList;

/**
 * @author yaoo on 4/25/18
 */
public abstract class FrameworkServlet extends HttpServletBean {

    public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

    //public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;
    public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";
    private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

    private String contextAttribute;
    //    private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;
    private String namespace;
    private String contextConfigLocation;
    //private final ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers = new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();
    private String contextInitializerClasses;
    private WebApplicationContext webApplicationContext;


}
