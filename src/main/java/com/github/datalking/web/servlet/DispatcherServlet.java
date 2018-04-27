package com.github.datalking.web.servlet;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.exception.NoSuchBeanDefinitionException;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.View;
import com.github.datalking.web.mvc.ViewResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http请求的中心控制器
 *
 * @author yaoo on 4/25/18
 */
public class DispatcherServlet extends FrameworkServlet {

    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

    public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    /// true表示探测所有，包括在***-servlet.xml中声明的
    private boolean detectAllHandlerMappings = true;

    private boolean detectAllViewResolvers = true;

    private boolean detectAllHandlerAdapters = true;

    //    private MultipartResolver multipartResolver;

    // HandlerMapping链表
    private List<HandlerMapping> handlerMappings;

    // HandlerAdapter链表
    private List<HandlerAdapter> handlerAdapters;

    private RequestToViewNameTranslator viewNameTranslator;

    private List<ViewResolver> viewResolvers;

    public DispatcherServlet() {
        super();
    }


    public DispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
        this.detectAllHandlerMappings = detectAllHandlerMappings;
    }

    public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
        this.detectAllHandlerAdapters = detectAllHandlerAdapters;
    }

    public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
        this.detectAllViewResolvers = detectAllViewResolvers;
    }

    /**
     * 初始化http request处理器
     * <p>
     * 覆盖父类方法，作为initWebApplicationContext()的一小步
     */
    @Override
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    protected void initStrategies(ApplicationContext context) {
//        initMultipartResolver(context);
//        initLocaleResolver(context);
//        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
//        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
//        initFlashMapManager(context);
    }

    /**
     * 实际请求处理方法
     * <p>
     * 覆盖父类方法
     */
    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
//            String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
            logger.debug("DispatcherServlet with name '" + getServletName() + "'" + " processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
        }

        // Keep a snapshot of the request attributes in case of an include, to be able to restore the original attributes after the include.
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

        // Make framework objects available to handlers and view objects.
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());

//        FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
//        if (inputFlashMap != null) {
//            request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
//        }
//        request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
//        request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);

        try {
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
     * 实现MVC
     */
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        boolean multipartRequestParsed = false;

//        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        try {
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {
//                processedRequest = checkMultipart(request);
                //multipartRequestParsed = (processedRequest != request);

                // Determine handler for the current request.
                mappedHandler = getHandler(processedRequest);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    noHandlerFound(processedRequest, response);
                    return;
                }

                // Determine handler adapter for the current request.
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                // Process last-modified header, if supported by the handler.
                String method = request.getMethod();
                boolean isGet = "GET".equals(method);
                if (isGet || "HEAD".equals(method)) {
                    long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                    }
                    if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                        return;
                    }
                }

                if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                    return;
                }

                // Actually invoke the handler.
                mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

//                if (asyncManager.isConcurrentHandlingStarted()) {
//                    return;
//                }

                applyDefaultViewName(request, mv);
                mappedHandler.applyPostHandle(processedRequest, response, mv);
            } catch (Exception ex) {
                dispatchException = ex;
            }
            processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
        } catch (Exception ex) {
            triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
        } catch (Error err) {
            triggerAfterCompletionWithError(processedRequest, response, mappedHandler, err);
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
        if (mv != null && !mv.hasView()) {
            mv.setViewName(getDefaultViewName(request));
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
                // OrderComparator.sort(this.handlerMappings);
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

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
//        if (this.handlerMappings == null) {
//            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
//            }
//        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
//            Map<String, HandlerAdapter> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            Map<String, HandlerAdapter> matchingBeans = context.getBeansOfType(HandlerAdapter.class);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<>(matchingBeans.values());
                //OrderComparator.sort(this.handlerAdapters);
            }
        } else {
            try {
                HandlerAdapter ha = (HandlerAdapter) context.getBean(HANDLER_ADAPTER_BEAN_NAME);
                this.handlerAdapters = Collections.singletonList(ha);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
//        if (this.handlerAdapters == null) {
//            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
//            }
//        }
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
        try {
            this.viewNameTranslator = (RequestToViewNameTranslator) context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME);
            if (logger.isDebugEnabled()) {
                logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            ex.printStackTrace();
            // We need to use the default.
//            this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
//                        REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
//                        "]");
//            }
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

        // Ensure we have at least one ViewResolver, by registering
        // a default ViewResolver if no other resolvers are found.
//        if (this.viewResolvers == null) {
//            this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
//            }
//        }
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
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        if (pageNotFoundLogger.isWarnEnabled()) {
//            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + getRequestUri(request) +
//                    "] in DispatcherServlet with name '" + getServletName() + "'");
//        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler adapter [" + ha + "]");
            }
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler + "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }


    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
        View view;
        if (mv.isReference()) {
            view = resolveViewName(mv.getViewName(), mv.getModelInternal(), request);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + mv.getViewName() + "' in servlet with name '" + getServletName() + "'");
            }
        } else {
            view = mv.getView();
            if (view == null) {
                throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " + "View object in servlet with name '" + getServletName() + "'");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        try {
            view.render(mv.getModelInternal(), request, response);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
                        getServletName() + "'", ex);
            }
            throw ex;
        }
    }

    protected String getDefaultViewName(HttpServletRequest request) throws Exception {
        return this.viewNameTranslator.getViewName(request);
    }

    protected View resolveViewName(String viewName, Map<String, Object> model, HttpServletRequest request) throws Exception {

        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(viewName);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
                                        HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, ex);
        }
        throw ex;
    }

    private void triggerAfterCompletionWithError(HttpServletRequest request, HttpServletResponse response,
                                                 HandlerExecutionChain mappedHandler, Error error) throws Exception {

        Exception ex = new Exception("Handler processing failed", error);
        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, ex);
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
