package com.github.datalking.web.servlet;

import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextInitializer;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.WebApplicationContextUtils;
import com.github.datalking.web.context.AnnotationConfigWebApplicationContext;
import com.github.datalking.web.context.ConfigurableWebApplicationContext;
import com.github.datalking.web.context.ContextLoader;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.http.RequestMethod;
import com.github.datalking.web.http.request.RequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author yaoo on 4/25/18
 */
@SuppressWarnings({"unused", "unchecked"})
public abstract class FrameworkServlet extends HttpServletBean {

    public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

    //public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;
    public static final Class<?> DEFAULT_CONTEXT_CLASS = AnnotationConfigWebApplicationContext.class;

    public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

    private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

    private String contextAttribute;

    private String contextId;

    private String namespace;

    private String contextInitializerClasses;

    private final ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers = new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();

    private WebApplicationContext webApplicationContext;

    private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

    private boolean threadContextInheritable = false;

    public FrameworkServlet() {
    }

    public FrameworkServlet(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
    }

    protected abstract void doService(HttpServletRequest request, HttpServletResponse response) throws Exception;

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (RequestMethod.PATCH.name().equalsIgnoreCase(request.getMethod())) {
            processRequest(request, response);
        } else {
            super.service(request, response);
        }
    }

    protected final void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        Throwable failureCause = null;

        RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

        //WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        //asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

        initContextHolders(request, requestAttributes);

        try {
            doService(request, response);
        } catch (ServletException ex) {
            failureCause = ex;
            throw ex;
        } catch (IOException ex) {
            failureCause = ex;
            throw ex;
        } catch (Throwable ex) {
            failureCause = ex;
            try {
                throw new Exception("Request processing failed", ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {

            resetContextHolders(request, previousAttributes);

            if (requestAttributes != null) {
                requestAttributes.requestCompleted();
            }

            if (logger.isDebugEnabled()) {
                if (failureCause != null) {
                    this.logger.debug("Could not complete request", failureCause);
                } else {
//                    if (asyncManager.isConcurrentHandlingStarted()) {
//                        logger.debug("Leaving response open for concurrent processing");
//                    } else {
                    this.logger.debug("Successfully completed request");
//                    }
                }
            }

//            publishRequestHandledEvent(request, startTime, failureCause);
        }
    }

    private void initContextHolders(HttpServletRequest request, RequestAttributes requestAttributes) {
        if (requestAttributes != null) {
            RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Bound request context to thread: " + request);
        }
    }

    private void resetContextHolders(HttpServletRequest request, RequestAttributes previousAttributes) {

        RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
        if (logger.isTraceEnabled()) {
            logger.trace("Cleared thread-bound request context: " + request);
        }
    }


    protected ServletRequestAttributes buildRequestAttributes(
            HttpServletRequest request, HttpServletResponse response, RequestAttributes previousAttributes) {

        if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
            return new ServletRequestAttributes(request);
        } else {
            // preserve the pre-bound RequestAttributes instance
            return null;
        }
    }

    @Override
    protected final void initServletBean() throws ServletException {
        getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
        if (this.logger.isInfoEnabled()) {
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            this.webApplicationContext = initWebApplicationContext();
            initFrameworkServlet();
        } catch (ServletException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        } catch (RuntimeException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        }

        if (this.logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
                    elapsedTime + " ms");
        }
    }

    protected WebApplicationContext initWebApplicationContext() {
        WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        WebApplicationContext wac = null;

        if (this.webApplicationContext != null) {
            // A context instance was injected at construction time -> use it
            wac = this.webApplicationContext;
            if (wac instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
                if (!cwac.isActive()) {
                    // The context has not yet been refreshed -> provide services such as
                    // setting the parent context, setting the application context id, etc
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent -> set
                        // the root application context (if any; may be null) as the parent
                        cwac.setParent(rootContext);
                    }
                    configureAndRefreshWebApplicationContext(cwac);
                }
            }
        }
        if (wac == null) {
            // No context instance was injected at construction time -> see if one
            // has been registered in the servlet context. If one exists, it is assumed
            // that the parent context (if any) has already been set and that the
            // user has performed any initialization such as setting the context id
            wac = findWebApplicationContext();
        }
        if (wac == null) {
            // No context instance is defined for this servlet -> create a local one
            wac = createWebApplicationContext(rootContext);
        }

//        if (!this.refreshEventReceived) {
//            onRefresh(wac);
//        }

//        if (this.publishContext) {
//            // Publish the context as a servlet context attribute.
//            String attrName = getServletContextAttributeName();
//            getServletContext().setAttribute(attrName, wac);
//            if (this.logger.isDebugEnabled()) {
//                this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
//                        "' as ServletContext attribute with name [" + attrName + "]");
//            }
//        }

        return wac;
    }

    protected WebApplicationContext findWebApplicationContext() {
        String attrName = getContextAttribute();
        if (attrName == null) {
            return null;
        }
        WebApplicationContext wac =
                WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
        if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
        }
        return wac;
    }

    protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {
        Class<?> contextClass = getContextClass();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Servlet with name '" + getServletName() +
                    "' will try to create custom WebApplicationContext context of class '" +
                    contextClass.getName() + "'" + ", using parent context [" + parent + "]");
        }
        if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
            try {
                throw new Exception("Fatal initialization error in servlet with name '" + getServletName() +
                        "': custom WebApplicationContext class [" + contextClass.getName() +
                        "] is not of type ConfigurableWebApplicationContext");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object obj = null;
        try {
            obj = contextClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

//        ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
        ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext) obj;

//        wac.setEnvironment(getEnvironment());
        wac.setParent(parent);
//        wac.setConfigLocation(getContextConfigLocation());

        configureAndRefreshWebApplicationContext(wac);

        return wac;
    }

    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
            // The application context id is still set to its original default value
            // -> assign a more useful id based on available information
            if (this.contextId != null) {
                wac.setId(this.contextId);
            } else {
                // Generate default id...
                ServletContext sc = getServletContext();
                if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
                    // Servlet <= 2.4: resort to name specified in web.xml, if any.
                    String servletContextName = sc.getServletContextName();
                    if (servletContextName != null) {
                        wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + servletContextName +
                                "." + getServletName());
                    } else {
                        wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + getServletName());
                    }
                } else {
                    // Servlet 2.5's getContextPath available!
                    wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                            ObjectUtils.getDisplayString(sc.getContextPath()) + "/" + getServletName());
                }
            }
        }

        wac.setServletContext(getServletContext());
        wac.setServletConfig(getServletConfig());
        wac.setNamespace(getNamespace());
//        wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

        // The wac environment's #initPropertySources will be called in any case when the context
        // is refreshed; do it eagerly here to ensure servlet property sources are in place for
        // use in any post-processing or initialization that occurs below prior to #refresh
//        ConfigurableEnvironment env = wac.getEnvironment();
//        if (env instanceof ConfigurableWebEnvironment) {
//            ((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
//        }

        postProcessWebApplicationContext(wac);
        applyInitializers(wac);
        wac.refresh();
    }

    protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
        return createWebApplicationContext((ApplicationContext) parent);
    }

    protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
    }


    protected void applyInitializers(ConfigurableApplicationContext wac) {
        String globalClassNames = getServletContext().getInitParameter(ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM);
        if (globalClassNames != null) {
            for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
                this.contextInitializers.add(loadInitializer(className, wac));
            }
        }

        if (this.contextInitializerClasses != null) {
            for (String className : StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS)) {
                this.contextInitializers.add(loadInitializer(className, wac));
            }
        }

//        AnnotationAwareOrderComparator.sort(this.contextInitializers);
        for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
            initializer.initialize(wac);
        }
    }

    @SuppressWarnings("unchecked")
    private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(String className, ConfigurableApplicationContext wac) {
        try {
            Class<?> initializerClass = Class.forName(className);
            Class<?> initializerContextClass = GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
            if (initializerContextClass != null) {
                Assert.isAssignable(initializerContextClass, wac.getClass(), String.format(
                        "Could not add context initializer [%s] since its generic parameter [%s] " +
                                "is not assignable from the type of application context used by this " +
                                "framework servlet [%s]: ", initializerClass.getName(), initializerContextClass.getName(),
                        wac.getClass().getName()));
            }

            Object obj = initializerClass.newInstance();

            ApplicationContextInitializer result = (ApplicationContextInitializer) obj;

//            return BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
            return result;
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("Could not instantiate class [%s] specified " +
                    "via 'contextInitializerClasses' init-param", className), ex);
        }
    }

    public String getServletContextAttributeName() {
        return SERVLET_CONTEXT_PREFIX + getServletName();
    }

    protected void initFrameworkServlet() throws ServletException {
    }

    /**
     * 调用refresh()
     */
    public void refresh() {
        WebApplicationContext wac = getWebApplicationContext();
        if (!(wac instanceof ConfigurableApplicationContext)) {
            throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
        }
        ((ConfigurableApplicationContext) wac).refresh();
    }


    @Override
    public void destroy() {
        getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
        if (this.webApplicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) this.webApplicationContext).close();
        }
    }

    public final WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    public void setContextAttribute(String contextAttribute) {
        this.contextAttribute = contextAttribute;
    }

    public String getContextAttribute() {
        return this.contextAttribute;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }


    public String getContextId() {
        return this.contextId;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
    }

    public void setContextInitializers(ApplicationContextInitializer<? extends ConfigurableApplicationContext>... contextInitializers) {
        for (ApplicationContextInitializer<? extends ConfigurableApplicationContext> initializer : contextInitializers) {
            this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
        }
    }

    public void setContextInitializerClasses(String contextInitializerClasses) {
        this.contextInitializerClasses = contextInitializerClasses;
    }

    public void setContextClass(Class<?> contextClass) {
        this.contextClass = contextClass;
    }

    public Class<?> getContextClass() {
        return this.contextClass;
    }

}
