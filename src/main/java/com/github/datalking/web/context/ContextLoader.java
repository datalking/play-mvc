package com.github.datalking.web.context;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实际执行root application context的初始化
 */
public class ContextLoader {

    private static Logger logger = LoggerFactory.getLogger(ContextLoader.class);

    public static final String CONTEXT_ID_PARAM = "contextId";

    public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

    public static final String CONTEXT_CLASS_PARAM = "contextClass";

    public static final String CONTEXT_INITIALIZER_CLASSES_PARAM = "contextInitializerClasses";

    public static final String GLOBAL_INITIALIZER_CLASSES_PARAM = "globalInitializerClasses";

    public static final String LOCATOR_FACTORY_SELECTOR_PARAM = "locatorFactorySelector";

    public static final String LOCATOR_FACTORY_KEY_PARAM = "parentContextKey";

    private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

//    private static final String DEFAULT_STRATEGIES_PATH = "ContextLoader.properties";
//
//    private static final Properties defaultStrategies;

//    static {
//        // Load default strategy implementations from properties file.
//        // This is currently strictly internal and not meant to be customized
//        // by application developers.
//        try {
//            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, ContextLoader.class);
//            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
//        } catch (IOException ex) {
//            throw new IllegalStateException("Could not load 'ContextLoader.properties': " + ex.getMessage());
//        }
//    }


    private static final Map<ClassLoader, WebApplicationContext> currentContextPerThread = new ConcurrentHashMap<>(1);

    private static volatile WebApplicationContext currentContext;
    // root
    private WebApplicationContext context;

    private BeanFactoryReference parentContextRef;

    public ContextLoader() {
    }

    public ContextLoader(WebApplicationContext context) {
        this.context = context;
    }

    /**
     * Initialize Spring's web application context for the given servlet context,
     * using the application context provided at construction time, or creating a new one
     * according to the "{@link #CONTEXT_CLASS_PARAM contextClass}" and
     * "{@link #CONFIG_LOCATION_PARAM contextConfigLocation}" context-params.
     *
     * @param servletContext current servlet context
     * @return the new WebApplicationContext
     * @see #ContextLoader(WebApplicationContext)
     * @see #CONTEXT_CLASS_PARAM
     * @see #CONFIG_LOCATION_PARAM
     */
    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
        if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
            throw new IllegalStateException("Cannot initialize context because there is already a root application context present - " +
                    "check whether you have multiple ContextLoader* definitions in your web.xml!");
        }

        servletContext.log("Initializing Spring root WebApplicationContext");
        if (logger.isInfoEnabled()) {
            logger.info("Root WebApplicationContext: initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            // Store context in local instance variable, to guarantee that
            // it is available on ServletContext shutdown.
            if (this.context == null) {
                this.context = createWebApplicationContext(servletContext);
            }
            if (this.context instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) this.context;
                if (!cwac.isActive()) {
                    // The context has not yet been refreshed -> provide services such as setting the parent context, setting the application context id, etc
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent ->  determine parent for root web application context, if any.
                        ApplicationContext parent = loadParentContext(servletContext);
                        cwac.setParent(parent);
                    }
                    configureAndRefreshWebApplicationContext(cwac, servletContext);
                }
            }

            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == ContextLoader.class.getClassLoader()) {
                currentContext = this.context;
            } else if (ccl != null) {
                currentContextPerThread.put(ccl, this.context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Published root WebApplicationContext as ServletContext attribute with name [" +
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
            }
            if (logger.isInfoEnabled()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
            }

            return this.context;
        } catch (RuntimeException ex) {
            logger.error("Context initialization failed", ex);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
            throw ex;
        } catch (Error err) {
            logger.error("Context initialization failed", err);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
            throw err;
        }
    }

    protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
        Class<?> contextClass = determineContextClass(sc);
        if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {

            try {
                throw new Exception("Custom context class [" + contextClass.getName() + "] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //return (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
        Object obj = null;
        try {
            obj = contextClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (ConfigurableWebApplicationContext) obj;
    }

    @Deprecated
    protected WebApplicationContext createWebApplicationContext(ServletContext sc, ApplicationContext parent) {
        return createWebApplicationContext(sc);
    }

    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {

//        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
//            // The application context id is still set to its original default value -> assign a more useful id based on available information
//            String idParam = sc.getInitParameter(CONTEXT_ID_PARAM);
//
//            if (idParam != null) {
//                wac.setId(idParam);
//            } else {
//                // Generate default id...
//                if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
//                    // Servlet <= 2.4: resort to name specified in web.xml, if any.
//                    wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
//                            ObjectUtils.getDisplayString(sc.getServletContextName()));
//                } else {
//                    wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
//                            ObjectUtils.getDisplayString(sc.getContextPath()));
//                }
//            }
//        }

        wac.setServletContext(sc);
//        String configLocationParam = sc.getInitParameter(CONFIG_LOCATION_PARAM);
//        if (configLocationParam != null) {
//            wac.setConfigLocation(configLocationParam);
//        }

        // The wac environment's #initPropertySources will be called in any case when the context
        // is refreshed; do it eagerly here to ensure servlet property sources are in place for
        // use in any post-processing or initialization that occurs below prior to #refresh
//        ConfigurableEnvironment env = wac.getEnvironment();
//        if (env instanceof ConfigurableWebEnvironment) {
//            ((ConfigurableWebEnvironment) env).initPropertySources(sc, null);
//        }

        customizeContext(sc, wac);
        wac.refresh();
    }


//    protected void customizeContext(ServletContext sc, ConfigurableWebApplicationContext wac) {
//        List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> initializerClasses =                determineContextInitializerClasses(sc);
//        if (initializerClasses.isEmpty()) {
//            return;
//        }
//
//        ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerInstances =                new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();
//
//        for (Class<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerClass : initializerClasses) {
//            Class<?> initializerContextClass =                    GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
//            if (initializerContextClass != null) {
//                Assert.isAssignable(initializerContextClass, wac.getClass(), String.format(
//                        "Could not add context initializer [%s] since its generic parameter [%s] " +
//                                "is not assignable from the type of application context used by this " +
//                                "context loader [%s]: ", initializerClass.getName(), initializerContextClass.getName(),
//                        wac.getClass().getName()));
//            }
//
//            Object obj=initializerClass.newInstance();
//            initializerInstances.add(obj);
//        }
//
//       // AnnotationAwareOrderComparator.sort(initializerInstances);
//        for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : initializerInstances) {
//            initializer.initialize(wac);
//        }
//    }

    /**
     * Return the WebApplicationContext implementation class to use, either the
     * default XmlWebApplicationContext or a custom context class if specified.
     */
//    protected Class<?> determineContextClass(ServletContext servletContext) {
//        String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
//        if (contextClassName != null) {
//            try {
//                return ClassUtils.forName(contextClassName, ClassUtils.getDefaultClassLoader());
//            } catch (ClassNotFoundException ex) {
//                throw new ApplicationContextException(
//                        "Failed to load custom context class [" + contextClassName + "]", ex);
//            }
//        } else {
//            contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
//            try {
//                return ClassUtils.forName(contextClassName, ContextLoader.class.getClassLoader());
//            } catch (ClassNotFoundException ex) {
//                throw new ApplicationContextException(
//                        "Failed to load default context class [" + contextClassName + "]", ex);
//            }
//        }
//    }
    protected List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>
    determineContextInitializerClasses(ServletContext servletContext) {

        List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> classes =
                new ArrayList<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>();

        String globalClassNames = servletContext.getInitParameter(GLOBAL_INITIALIZER_CLASSES_PARAM);
        if (globalClassNames != null) {
            for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
                classes.add(loadInitializerClass(className));
            }
        }

        String localClassNames = servletContext.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM);
        if (localClassNames != null) {
            for (String className : StringUtils.tokenizeToStringArray(localClassNames, INIT_PARAM_DELIMITERS)) {
                classes.add(loadInitializerClass(className));
            }
        }

        return classes;
    }

    @SuppressWarnings("unchecked")
    private Class<ApplicationContextInitializer<ConfigurableApplicationContext>> loadInitializerClass(String className) {
        try {
            Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
            Assert.isAssignable(ApplicationContextInitializer.class, clazz);
            return (Class<ApplicationContextInitializer<ConfigurableApplicationContext>>) clazz;
        } catch (ClassNotFoundException ex) {
            throw new ApplicationContextException("Failed to load context initializer class [" + className + "]", ex);
        }
    }

//    protected ApplicationContext loadParentContext(ServletContext servletContext) {
//        ApplicationContext parentContext = null;
//        String locatorFactorySelector = servletContext.getInitParameter(LOCATOR_FACTORY_SELECTOR_PARAM);
//        String parentContextKey = servletContext.getInitParameter(LOCATOR_FACTORY_KEY_PARAM);
//
//        if (parentContextKey != null) {
//            // locatorFactorySelector may be null, indicating the default "classpath*:beanRefContext.xml"
//            BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);
//            Log logger = LogFactory.getLog(ContextLoader.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("Getting parent context definition: using parent context key of '" +
//                        parentContextKey + "' with BeanFactoryLocator");
//            }
//            this.parentContextRef = locator.useBeanFactory(parentContextKey);
//            parentContext = (ApplicationContext) this.parentContextRef.getFactory();
//        }
//
//        return parentContext;
//    }

    public void closeWebApplicationContext(ServletContext servletContext) {
        servletContext.log("Closing Spring root WebApplicationContext");
        try {
            if (this.context instanceof ConfigurableWebApplicationContext) {
                ((ConfigurableWebApplicationContext) this.context).close();
            }
        } finally {
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == ContextLoader.class.getClassLoader()) {
                currentContext = null;
            } else if (ccl != null) {
                currentContextPerThread.remove(ccl);
            }
            servletContext.removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            if (this.parentContextRef != null) {
                this.parentContextRef.release();
            }
        }
    }


    public static WebApplicationContext getCurrentWebApplicationContext() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl != null) {
            WebApplicationContext ccpt = currentContextPerThread.get(ccl);
            if (ccpt != null) {
                return ccpt;
            }
        }
        return currentContext;
    }


}
