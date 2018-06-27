package com.github.datalking.web.servlet;

import com.github.datalking.common.OrderComparator;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.exception.NoSuchBeanDefinitionException;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * http请求的中心控制器
 * 每一个请求都会调用 doService() > doDispatch()
 *
 * @author yaoo on 4/25/18
 */
public class DispatcherServlet extends FrameworkServlet {

    public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

    public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String EXCEPTION_ATTRIBUTE = DispatcherServlet.class.getName() + ".EXCEPTION";

    public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";
    public static final String FLASH_MAP_MANAGER_ATTRIBUTE = DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";
    public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";
    public static final String OUTPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";

    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    protected static final Logger pageNotFoundLogger = LoggerFactory.getLogger(PAGE_NOT_FOUND_LOG_CATEGORY);

    private static final Properties defaultStrategies = new Properties();

    static {
        // 从内部配置文件 DispatcherServlet.properties 加载mvc相关类
        try {
            ClassLoader classLoader = DispatcherServlet.class.getClassLoader();
            InputStream in = classLoader.getResourceAsStream(DEFAULT_STRATEGIES_PATH);
            defaultStrategies.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
        }
    }

    /// true表示探测所有，false表示只探测在***-servlet.xml中声明的
    private boolean detectAllHandlerMappings = true;
    private boolean detectAllHandlerAdapters = true;
    private boolean detectAllHandlerExceptionResolvers = true;
    private boolean detectAllViewResolvers = true;
    private boolean cleanupAfterInclude = true;

    //    private MultipartResolver multipartResolver;

    // HandlerMapping链表
    private List<HandlerMapping> handlerMappings;

    // HandlerAdapter链表
    private List<HandlerAdapter> handlerAdapters;

    private List<HandlerExceptionResolver> handlerExceptionResolvers;

    private RequestToViewNameTranslator viewNameTranslator;

    private List<ViewResolver> viewResolvers;

    private FlashMapManager flashMapManager;

    public DispatcherServlet() {
        super();
    }

    // 默认调用此构造函数
    public DispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
        this.detectAllHandlerMappings = detectAllHandlerMappings;
    }

    public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
        this.detectAllHandlerAdapters = detectAllHandlerAdapters;
    }

    public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
        this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
    }

    public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
        this.detectAllViewResolvers = detectAllViewResolvers;
    }

    public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
        this.cleanupAfterInclude = cleanupAfterInclude;
    }

    /**
     * 初始化http请求参数解析、返回值处理、视图渲染等各种处理器
     * <p>
     * 覆盖父类方法，作为initWebApplicationContext()的一小步
     */
    @Override
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    protected void initStrategies(ApplicationContext context) {
//        initMultipartResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
//        initFlashMapManager(context);
    }

    /**
     * 实际处理请求的方法，每一个请求都会经过这个方法处理
     * <p>
     * 覆盖父类方法
     */
    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
//            String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
            logger.debug("DispatcherServlet with name '" + getServletName() + "'" + " processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
        }

        // 保存include情况下请求属性的快照
        Map<String, Object> attributesSnapshot = null;
        if (WebUtils.isIncludeRequest(request)) {
            attributesSnapshot = new HashMap<>();
            Enumeration<?> attrNames = request.getAttributeNames();

            while (attrNames.hasMoreElements()) {
                String attrName = (String) attrNames.nextElement();
//                if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
                if (attrName.startsWith("com.github.datalking.web.servlet")) {
                    attributesSnapshot.put(attrName, request.getAttribute(attrName));
                }
            }

        }

        // 在当前请求中设置context对象
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());

//        FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
//        if (inputFlashMap != null) {
//            request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
//        }
//        request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
//        request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);

        try {
            // ==== 转发到合适控制器Controller
            doDispatch(request, response);
        } finally {
//            if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
//                // Restore the original attribute snapshot, in case of an include.
//                if (attributesSnapshot != null) {
//                    restoreAttributesAfterInclude(request, attributesSnapshot);
//                }
//            }
        }
    }

    /**
     * 转发请求到控制器方法
     * 每一个请求都会经过这个方法处理
     */
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HttpServletRequest processedRequest = request;
        //
        HandlerExecutionChain handlerExecutionChain = null;

//        boolean multipartRequestParsed = false;
//        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        try {
            // 响应的视图
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {
//                processedRequest = checkMultipart(request);
//                multipartRequestParsed = (processedRequest != request);

                // 遍历handlerMappings，获取HandlerExecutionChain
                handlerExecutionChain = getHandler(processedRequest);

                if (handlerExecutionChain == null || handlerExecutionChain.getHandler() == null) {
                    noHandlerFound(processedRequest, response);
                    return;
                }

                // 遍历handlerAdapters，获取匹配的HandlerAdapter
                HandlerAdapter ha = getHandlerAdapter(handlerExecutionChain.getHandler());

                /// 检查与上次请求相比是否修改过
                String method = request.getMethod();
                boolean isGet = "GET".equals(method);
                if (isGet || "HEAD".equals(method)) {
                    long lastModified = ha.getLastModified(request, handlerExecutionChain.getHandler());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                    }
                    if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                        return;
                    }
                }
                if (!handlerExecutionChain.applyPreHandle(processedRequest, response)) {
                    return;
                }

                Object handler = handlerExecutionChain.getHandler();

                // ==== 实际执行请求处理方法，包括 参数解析、返回结果处理
                mv = ha.handle(processedRequest, response, handler);

//                if (asyncManager.isConcurrentHandlingStarted()) {
//                    return;
//                }

                // 视图名翻译，只有当view名不存在时才执行
                applyDefaultViewName(request, mv);

                // 执行intercept的后置处理器，todo 说明场景
                handlerExecutionChain.applyPostHandle(processedRequest, response, mv);
            } catch (Exception ex) {
                dispatchException = ex;
            }

            // 处理返回结果，主要是视图渲染
            processDispatchResult(processedRequest, response, handlerExecutionChain, mv, dispatchException);

        } catch (Exception ex) {
            triggerAfterCompletion(processedRequest, response, handlerExecutionChain, ex);
        } catch (Error err) {
            triggerAfterCompletionWithError(processedRequest, response, handlerExecutionChain, err);
        } finally {
//            if (asyncManager.isConcurrentHandlingStarted()) {
//                // Instead of postHandle and afterCompletion
//                if (mappedHandler != null) {
//                    mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
//                }
//            } else {
            // Clean up any resources used by a multipart request.
//                if (multipartRequestParsed) {
//                    cleanupMultipart(processedRequest);
//                }
//            }
        }
    }


    private void applyDefaultViewName(HttpServletRequest request, ModelAndView mv) throws Exception {
        /// 只有当ModelAndView非空，且没有view时，才需要翻译
        if (mv != null && !mv.hasView()) {
            String vname = getDefaultViewName(request);
            mv.setViewName(vname);
        }
    }

    private void processDispatchResult(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HandlerExecutionChain mappedHandler,
                                       ModelAndView mv,
                                       Exception exception) throws Exception {

        boolean errorView = false;

//        if (exception != null) {
//            if (exception instanceof ModelAndViewDefiningException) {
//                logger.debug("ModelAndViewDefiningException encountered", exception);
//                mv = ((ModelAndViewDefiningException) exception).getModelAndView();
//            } else {
//                Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
//                mv = processHandlerException(request, response, handler, exception);
//                errorView = (mv != null);
//            }
//        }

        // Did the handler return a view to render?
        if (mv != null && !mv.wasCleared()) {
            // ==== 渲染视图
            render(mv, request, response);

            if (errorView) {
                WebUtils.clearErrorRequestAttributes(request);
            }

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
                        "': assuming HandlerAdapter completed request handling");
            }
        }

//        if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
//            // Concurrent handling started during a forward
//            return;
//        }

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, null);
        }
    }


//    private void initMultipartResolver(ApplicationContext context) {
//        try {
//            this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
//            }
//        } catch (NoSuchBeanDefinitionException ex) {
//            // Default is no multipart resolver.
//            this.multipartResolver = null;
//            if (logger.isDebugEnabled()) {
//                logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
//                        "': no multipart request handling provided");
//            }
//        }
//    }


    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        /// 默认为true
        if (this.detectAllHandlerMappings) {
//            Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            // 获取所有HandlerMapping类型的bean
            Map<String, HandlerMapping> matchingBeans = context.getBeansOfType(HandlerMapping.class);

            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<>(matchingBeans.values());
                OrderComparator.sort(this.handlerMappings);
            }
        } else {
            try {
//                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                HandlerMapping hm = (HandlerMapping) context.getBean(HANDLER_MAPPING_BEAN_NAME);
                this.handlerMappings = Collections.singletonList(hm);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // 当容器中没有时，确保至少有一个根据配置文件创建的HandlerMapping
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
//            Map<String, HandlerAdapter> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            Map<String, HandlerAdapter> matchingBeans = context.getBeansOfType(HandlerAdapter.class);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<>(matchingBeans.values());
                OrderComparator.sort(this.handlerAdapters);
            }
        } else {
            try {
                HandlerAdapter ha = (HandlerAdapter) context.getBean(HANDLER_ADAPTER_BEAN_NAME);
                this.handlerAdapters = Collections.singletonList(ha);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // 当容器中没有时，确保至少有一个根据配置文件创建的HandlerMapping
        if (this.handlerAdapters == null) {
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        if (this.detectAllHandlerExceptionResolvers) {
//            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
            Map<String, HandlerExceptionResolver> matchingBeans = context.getBeansOfType(HandlerExceptionResolver.class);

            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
                OrderComparator.sort(this.handlerExceptionResolvers);
            }
        } else {
            try {
                HandlerExceptionResolver her = (HandlerExceptionResolver) context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
        try {
            // 下面的bean默认不存在，会进入catch块
            this.viewNameTranslator = (RequestToViewNameTranslator) context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME);
            if (logger.isDebugEnabled()) {
                logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            //ex.printStackTrace();
            // 使用默认的RequestToViewNameTranslator
            this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
                        REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
                        "]");
            }
        }
    }

    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = null;

        if (this.detectAllViewResolvers) {
//            Map<String, ViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
            Map<String, ViewResolver> matchingBeans = context.getBeansOfType(ViewResolver.class);
            if (!matchingBeans.isEmpty()) {
                this.viewResolvers = new ArrayList<>(matchingBeans.values());
                // OrderComparator.sort(this.viewResolvers);
            }
        } else {
            try {
                ViewResolver vr = (ViewResolver) context.getBean(VIEW_RESOLVER_BEAN_NAME);
                this.viewResolvers = Collections.singletonList(vr);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            }
        }

        if (this.viewResolvers == null) {
            this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<>(classNames.length);
            for (String className : classNames) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());

                    // ==== 创建默认bean，并注册到beanFactory
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                } catch (ClassNotFoundException | LinkageError ex) {
                    ex.printStackTrace();
                }
            }
            return strategies;
        } else {
            return new LinkedList<T>();
        }
    }

    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {

        Object obj = null;
        try {
            obj = context.getAutowireCapableBeanFactory().createBean(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;

    }

    protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
        List<T> strategies = getDefaultStrategies(context, strategyInterface);
        if (strategies.size() == 0 || strategies.size() != 1) {

            try {
                throw new Exception("DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return strategies.get(0);
    }

    /**
     * 接收http request的输入，返回拦截链
     */
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {

        /// 遍历 handlerMappings
        for (HandlerMapping hm : this.handlerMappings) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
            }

            // 默认使用 RequestMappingHandlerMapping 处理，得到匹配url的bean及其中的method
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler adapter [" + ha + "]");
            }

            // 默认使用RequestMappingHandlerAdapter
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler + "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        if (pageNotFoundLogger.isWarnEnabled()) {
//            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + getRequestUri(request) +
//                    "] in DispatcherServlet with name '" + getServletName() + "'");
//        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * 渲染ModelAndView
     */
    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
        View view;

        Map<String, Object> model = mv.getModelInternal();

        if (mv.isReference()) {

            // ==== 默认解析成jstlView
            view = resolveViewName(mv.getViewName(), model, request);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + mv.getViewName() + "' in servlet with name '" + getServletName() + "'");
            }

        } else {
            view = mv.getView();
            if (view == null) {
                throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " + "View object in servlet with name '" + getServletName() + "'");
            }
        }

        try {

            // ==== 渲染视图
            view.render(model, request, response);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
                        getServletName() + "'", ex);
            }
            throw ex;
        }
    }

    protected ModelAndView processHandlerException(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   Object handler,
                                                   Exception ex) throws Exception {

        ModelAndView exMv = null;
        for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
            exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
            if (exMv != null) {
                break;
            }
        }
        if (exMv != null) {
            if (exMv.isEmpty()) {
                request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
                return null;
            }
            if (!exMv.hasView()) {
                exMv.setViewName(getDefaultViewName(request));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv, ex);
            }
            WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
            return exMv;
        }

        throw ex;
    }

    protected String getDefaultViewName(HttpServletRequest request) {
        return this.viewNameTranslator.getViewName(request);
    }

    protected View resolveViewName(String viewName, Map<String, Object> model, HttpServletRequest request) {

        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(viewName);
            if (view != null) {
                return view;
            }
        }

        return null;
    }

    private void triggerAfterCompletion(HttpServletRequest request,
                                        HttpServletResponse response,
                                        HandlerExecutionChain handlerExecutionChain,
                                        Exception ex) throws Exception {

        if (handlerExecutionChain != null) {
            handlerExecutionChain.triggerAfterCompletion(request, response, ex);
        }
        throw ex;
    }

    private void triggerAfterCompletionWithError(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 HandlerExecutionChain handlerExecutionChain,
                                                 Error error) throws Exception {
        Exception ex = new Exception("Handler processing failed", error);
        if (handlerExecutionChain != null) {
            handlerExecutionChain.triggerAfterCompletion(request, response, ex);
        }
        throw ex;
    }

    private static String getRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return uri;
    }

}
