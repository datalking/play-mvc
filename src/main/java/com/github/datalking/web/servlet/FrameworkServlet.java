package com.github.datalking.web.servlet;

import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.env.ConfigurableEnvironment;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextInitializer;
import com.github.datalking.context.ConfigurableApplicationContext;
import com.github.datalking.context.ConfigurableWebEnvironment;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.WebApplicationContextUtils;
import com.github.datalking.web.context.AnnotationConfigWebApplicationContext;
import com.github.datalking.web.context.ConfigurableWebApplicationContext;
import com.github.datalking.web.context.ContextLoader;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.context.request.RequestContextHolder;
import com.github.datalking.web.http.RequestAttributes;
import com.github.datalking.web.http.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author yaoo on 4/25/18
 */
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

    private final ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers = new ArrayList<>();

    private WebApplicationContext webApplicationContext;

    private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

    private boolean threadContextInheritable = false;

    public FrameworkServlet() {
    }

    public FrameworkServlet(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
    }

    protected abstract void doService(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 初始化WebApplicationContext
     * <p>
     * 覆盖父类中的方法，作为init()的一小步
     */
    @Override
    protected final void initServletBean() throws ServletException {
        getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
        if (this.logger.isInfoEnabled()) {
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
        }

        long startTime = System.currentTimeMillis();

        try {
            // ==== 初始化WebApplicationContext
            this.webApplicationContext = initWebApplicationContext();

            // 空方法，留给子类实现
            initFrameworkServlet();
        } catch (ServletException | RuntimeException ex) {
            this.logger.error("Context initialization failed", ex);
            throw ex;
        }

        /// 打印容器启动时间
        if (this.logger.isInfoEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " + elapsedTime + " ms");
        }
    }

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
            // ==== 实际处理请求的位置，由子类实现
            doService(request, response);
        } catch (Throwable ex) {
            failureCause = ex;
            ex.printStackTrace();
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

    /**
     * 初始化WebApplicationContext
     */
    protected WebApplicationContext initWebApplicationContext() {
        // 获取由ContextLoaderListener初始化并注册在ServletContext中的根上下文，记为rootContext
        WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());

        WebApplicationContext wac = null;

        /// 非空表示这个Servlet类是通过编程式注册到容器中的，context也由编程方式式传入
        if (this.webApplicationContext != null) {
            wac = this.webApplicationContext;
            if (wac instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
                // 若为调用过refresh()，调用过refresh()方法后才会active
                if (!cwac.isActive()) {
                    if (cwac.getParent() == null) {
                        cwac.setParent(rootContext);
                    }

                    // ==== 调用ApplicationContext的refresh()，实例化bean
                    configureAndRefreshWebApplicationContext(cwac);
                }
            }
        }

        /// wac==null说明未完成上下文的设置，该Servlet不是由编程方式注册到容器中
        if (wac == null) {
            wac = findWebApplicationContext();
        }

        /// wac==null说明上面的初始化策略都没成功
        if (wac == null) {
            // 建立一个全新的以rootContext为父上下文的上下文
            wac = createWebApplicationContext(rootContext);
        }

//        if (!this.refreshEventReceived) {
        // 留给子类实现
        onRefresh(wac);
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

    protected void onRefresh(ApplicationContext context) {
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

    /**
     * 调用ApplicationContext的refresh()，实例化bean
     */
    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {

        /// ApplicationContext的id设置
        if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
            if (this.contextId != null) {
                wac.setId(this.contextId);
            } else {
                wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                        ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
            }
        }

        wac.setServletContext(getServletContext());
        wac.setServletConfig(getServletConfig());
        wac.setNamespace(getNamespace());
//        wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

        ConfigurableEnvironment env = wac.getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            // 属性
            ((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
        }

        // 空方法
        postProcessWebApplicationContext(wac);
        applyInitializers(wac); // 处理ApplicationContextInitializer

        // ==== 调用ApplicationContext的refresh()，实例化bean
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

    private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(String className, ConfigurableApplicationContext wac) {
        try {
            Class<?> initializerClass = Class.forName(className);
            Class<?> initializerContextClass = GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
            if (initializerContextClass != null) {
                Assert.isAssignable(initializerContextClass, wac.getClass(),
                        String.format("Could not add context initializer [%s] since its generic parameter [%s] " +
                                "is not assignable from the type of application context used by this " +
                                "framework servlet [%s]: ", initializerClass.getName(), initializerContextClass.getName(), wac.getClass().getName()));
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
     * 调用AbstractApplicationContext类的refresh()
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
