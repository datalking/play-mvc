package com.github.datalking.web.mvc.method;

import com.github.datalking.annotation.InitBinder;
import com.github.datalking.annotation.ModelAttribute;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.common.LocalVariableTableParameterNameDiscoverer;
import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.ReflectionUtils.MethodFilter;
import com.github.datalking.util.web.RequestContextUtils;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.bind.DefaultDataBinderFactory;
import com.github.datalking.web.bind.ServletRequestDataBinderFactory;
import com.github.datalking.web.bind.WebBindingInitializer;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.http.converter.MappingJackson2HttpMessageConverter;
import com.github.datalking.web.http.converter.StringHttpMessageConverter;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.ModelAndViewResolver;
import com.github.datalking.web.mvc.ModelFactory;
import com.github.datalking.web.mvc.ModelMap;
import com.github.datalking.web.mvc.View;
import com.github.datalking.web.servlet.InvocableHandlerMethod;
import com.github.datalking.web.servlet.ServletWebRequest;
import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodArgumentResolverComposite;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;
import com.github.datalking.web.support.HandlerMethodReturnValueHandlerComposite;
import com.github.datalking.web.support.HttpEntityMethodProcessor;
import com.github.datalking.web.support.MapMethodProcessor;
import com.github.datalking.web.support.ModelAndViewContainer;
import com.github.datalking.web.support.ModelAndViewMethodReturnValueHandler;
import com.github.datalking.web.support.ModelAndViewResolverMethodReturnValueHandler;
import com.github.datalking.web.support.ModelAttributeMethodProcessor;
import com.github.datalking.web.support.ModelMethodProcessor;
import com.github.datalking.web.support.PathVariableMethodArgumentResolver;
import com.github.datalking.web.support.RequestParamMethodArgumentResolver;
import com.github.datalking.web.support.ServletInvocableHandlerMethod;
import com.github.datalking.web.support.ServletRequestMethodArgumentResolver;
import com.github.datalking.web.support.ServletResponseMethodArgumentResolver;
import com.github.datalking.web.support.SessionAttributeStore;
import com.github.datalking.web.support.SessionAttributesHandler;
import com.github.datalking.web.support.ViewMethodReturnValueHandler;
import com.github.datalking.web.support.ViewNameMethodReturnValueHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现调用处理请求的方法
 *
 * @author yaoo on 4/28/18
 */
public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter
        implements BeanFactoryAware, InitializingBean {

//    private List<HandlerMethodArgumentResolver> customArgumentResolvers;

    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private HandlerMethodArgumentResolverComposite initBinderArgumentResolvers;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    private List<ModelAndViewResolver> modelAndViewResolvers;

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private List<HttpMessageConverter<?>> messageConverters;

    private WebBindingInitializer webBindingInitializer;

//    private AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("MvcAsync");

    private Long asyncRequestTimeout;

//    private CallableProcessingInterceptor[] callableInterceptors = new CallableProcessingInterceptor[0];
//    private DeferredResultProcessingInterceptor[] deferredResultInterceptors = new DeferredResultProcessingInterceptor[0];

    private boolean ignoreDefaultModelOnRedirect = false;

    private int cacheSecondsForSessionAttributeHandlers = 0;

    private boolean synchronizeOnSession = false;

    private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private ConfigurableBeanFactory beanFactory;

    private final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache = new ConcurrentHashMap<>(64);

    private final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<>(64);

//    private final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache = new LinkedHashMap<>();

    private final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<>(64);

//    private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();

    public RequestMappingHandlerAdapter() {

        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false);

        /// 添加4种http数据转换器
        this.messageConverters = new ArrayList<>(4);
//        this.messageConverters.add(new ByteArrayHttpMessageConverter());
        this.messageConverters.add(stringHttpMessageConverter);
//        this.messageConverters.add(new SourceHttpMessageConverter<Source>());
//        this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());
    }

//    public void setCustomArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        this.customArgumentResolvers = argumentResolvers;
//    }
//
//    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
//        return this.customArgumentResolvers;
//    }

    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.argumentResolvers = null;
        } else {
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.argumentResolvers.addResolvers(argumentResolvers);
        }
    }

    public HandlerMethodArgumentResolverComposite getArgumentResolvers() {
        return this.argumentResolvers;
    }

    public void setInitBinderArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.initBinderArgumentResolvers = null;
        } else {
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.initBinderArgumentResolvers.addResolvers(argumentResolvers);
        }
    }

    public HandlerMethodArgumentResolverComposite getInitBinderArgumentResolvers() {
        return this.initBinderArgumentResolvers;
    }

    public void setCustomReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        this.customReturnValueHandlers = returnValueHandlers;
    }

    public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
        return this.customReturnValueHandlers;
    }

    public void setReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers == null) {
            this.returnValueHandlers = null;
        } else {
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addHandlers(returnValueHandlers);
        }
    }

    public HandlerMethodReturnValueHandlerComposite getReturnValueHandlers() {
        return this.returnValueHandlers;
    }

    public void setModelAndViewResolvers(List<ModelAndViewResolver> modelAndViewResolvers) {
        this.modelAndViewResolvers = modelAndViewResolvers;
    }

    public List<ModelAndViewResolver> getModelAndViewResolvers() {
        return modelAndViewResolvers;
    }

    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager;
    }

    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }

    public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
        this.webBindingInitializer = webBindingInitializer;
    }

    public WebBindingInitializer getWebBindingInitializer() {
        return this.webBindingInitializer;
    }

//    public void setTaskExecutor(AsyncTaskExecutor taskExecutor) {
//        this.taskExecutor = taskExecutor;
//    }


    public void setAsyncRequestTimeout(long timeout) {
        this.asyncRequestTimeout = timeout;
    }

//    public void setCallableInterceptors(List<CallableProcessingInterceptor> interceptors) {
//        Assert.notNull(interceptors);
//        this.callableInterceptors = interceptors.toArray(new CallableProcessingInterceptor[interceptors.size()]);
//    }
//
//
//    public void setDeferredResultInterceptors(List<DeferredResultProcessingInterceptor> interceptors) {
//        Assert.notNull(interceptors);
//        this.deferredResultInterceptors = interceptors.toArray(new DeferredResultProcessingInterceptor[interceptors.size()]);
//    }


    public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
        this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
    }


    public void setSessionAttributeStore(SessionAttributeStore sessionAttributeStore) {
        this.sessionAttributeStore = sessionAttributeStore;
    }


    public void setCacheSecondsForSessionAttributeHandlers(int cacheSecondsForSessionAttributeHandlers) {
        this.cacheSecondsForSessionAttributeHandlers = cacheSecondsForSessionAttributeHandlers;
    }


    public void setSynchronizeOnSession(boolean synchronizeOnSession) {
        this.synchronizeOnSession = synchronizeOnSession;
    }

    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    protected ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public void afterPropertiesSet() {

        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }

        if (this.initBinderArgumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers();
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }

        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }

        initControllerAdviceCache();
    }


    private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
//        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver());
//        resolvers.add(new PathVariableMapMethodArgumentResolver());
//        resolvers.add(new MatrixVariableMethodArgumentResolver());
//        resolvers.add(new MatrixVariableMapMethodArgumentResolver());
//        resolvers.add(new ServletModelAttributeMethodProcessor(false));
        resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters()));
//        resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters()));
//        resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
//        resolvers.add(new RequestHeaderMapMethodArgumentResolver());
//        resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
//        resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());
        resolvers.add(new HttpEntityMethodProcessor(getMessageConverters()));
//        resolvers.add(new RedirectAttributesMethodArgumentResolver());
        resolvers.add(new ModelMethodProcessor());
        resolvers.add(new MapMethodProcessor());
//        resolvers.add(new ErrorsMethodArgumentResolver());
//        resolvers.add(new SessionStatusMethodArgumentResolver());
//        resolvers.add(new UriComponentsBuilderMethodArgumentResolver());

        // Custom arguments
//        if (getCustomArgumentResolvers() != null) {
//            resolvers.addAll(getCustomArgumentResolvers());
//        }

        // Catch-all
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
//        resolvers.add(new ServletModelAttributeMethodProcessor(true));

        return resolvers;
    }

    private List<HandlerMethodArgumentResolver> getDefaultInitBinderArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
//        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver());
//        resolvers.add(new PathVariableMapMethodArgumentResolver());
//        resolvers.add(new MatrixVariableMethodArgumentResolver());
//        resolvers.add(new MatrixVariableMapMethodArgumentResolver());
//        resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());

        // Custom arguments
//        if (getCustomArgumentResolvers() != null) {
//            resolvers.addAll(getCustomArgumentResolvers());
//        }

        // Catch-all
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));

        return resolvers;
    }

    private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

        // Single-purpose return value types
        handlers.add(new ModelAndViewMethodReturnValueHandler());
        handlers.add(new ModelMethodProcessor());
        handlers.add(new ViewMethodReturnValueHandler());
        handlers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.contentNegotiationManager));
//        handlers.add(new CallableMethodReturnValueHandler());
//        handlers.add(new DeferredResultMethodReturnValueHandler());
//        handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));

        // Annotation-based return value types
        handlers.add(new ModelAttributeMethodProcessor(false));
        handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.contentNegotiationManager));

        // Multi-purpose return value types
        handlers.add(new ViewNameMethodReturnValueHandler());
        handlers.add(new MapMethodProcessor());

        if (getCustomReturnValueHandlers() != null) {
            handlers.addAll(getCustomReturnValueHandlers());
        }

        if (!CollectionUtils.isEmpty(getModelAndViewResolvers())) {
            handlers.add(new ModelAndViewResolverMethodReturnValueHandler(getModelAndViewResolvers()));
        } else {
            handlers.add(new ModelAttributeMethodProcessor(true));
        }

        return handlers;
    }

    private void initControllerAdviceCache() {
        if (getApplicationContext() == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for controller advice: " + getApplicationContext());
        }

//        List<ControllerAdviceBean> beans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
//        Collections.sort(beans, new OrderComparator());
//
//        for (ControllerAdviceBean bean : beans) {
//            Set<Method> attrMethods = HandlerMethodSelector.selectMethods(bean.getBeanType(), MODEL_ATTRIBUTE_METHODS);
//            if (!attrMethods.isEmpty()) {
//                this.modelAttributeAdviceCache.put(bean, attrMethods);
//                logger.info("Detected @ModelAttribute methods in " + bean);
//            }
//            Set<Method> binderMethods = HandlerMethodSelector.selectMethods(bean.getBeanType(), INIT_BINDER_METHODS);
//            if (!binderMethods.isEmpty()) {
//                this.initBinderAdviceCache.put(bean, binderMethods);
//                logger.info("Detected @InitBinder methods in " + bean);
//            }
//        }

    }

    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        return true;
    }

    /**
     * 转发执行执行请求对应的方法
     */
    @Override
    protected final ModelAndView handleInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HandlerMethod handlerMethod) throws Exception {

        if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
            checkAndPrepare(request, response, this.cacheSecondsForSessionAttributeHandlers, true);
        } else {
            checkAndPrepare(request, response, true);
        }

        if (this.synchronizeOnSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object mutex = WebUtils.getSessionMutex(session);
                synchronized (mutex) {
                    return invokeHandleMethod(request, response, handlerMethod);
                }
            }
        }

        // ==== 调用执行方法
        return invokeHandleMethod(request, response, handlerMethod);
    }


    @Override
    protected long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod) {
        return -1;
    }

    private SessionAttributesHandler getSessionAttributesHandler(HandlerMethod handlerMethod) {
        Class<?> handlerType = handlerMethod.getBeanType();
        SessionAttributesHandler sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
        if (sessionAttrHandler == null) {
            synchronized (this.sessionAttributesHandlerCache) {

                sessionAttrHandler = this.sessionAttributesHandlerCache.get(handlerType);
                if (sessionAttrHandler == null) {

                    sessionAttrHandler = new SessionAttributesHandler(handlerType, sessionAttributeStore);

                    this.sessionAttributesHandlerCache.put(handlerType, sessionAttrHandler);
                }
            }
        }
        return sessionAttrHandler;
    }

    /**
     * 实际执行执行请求对应的方法
     */
    private ModelAndView invokeHandleMethod(HttpServletRequest request,
                                            HttpServletResponse response,
                                            HandlerMethod handlerMethod) throws Exception {

        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        // 创建数据绑定工厂
        WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);

        ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

        ServletInvocableHandlerMethod invocableMethod = createRequestMappingMethod(handlerMethod, binderFactory);

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));

        modelFactory.initModel(webRequest, mavContainer, invocableMethod);
        mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

//        AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
//        asyncWebRequest.setTimeout(this.asyncRequestTimeout);

//        final WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
//        asyncManager.setTaskExecutor(this.taskExecutor);
//        asyncManager.setAsyncWebRequest(asyncWebRequest);
//        asyncManager.registerCallableInterceptors(this.callableInterceptors);
//        asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

//        if (asyncManager.hasConcurrentResult()) {
//            Object result = asyncManager.getConcurrentResult();
//            mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
//            asyncManager.clearConcurrentResult();
//
//            if (logger.isDebugEnabled()) {
//                logger.debug("Found concurrent result value [" + result + "]");
//            }
//            requestMappingMethod = requestMappingMethod.wrapConcurrentResult(result);
//        }

        // ==== 调用处理请求的方法，处理结果放入mavContainer
        invocableMethod.invokeAndHandle(webRequest, mavContainer);

//        if (asyncManager.isConcurrentHandlingStarted()) {
//            return null;
//        }

        // 请求处理结果数据转换成视图
        return getModelAndView(mavContainer, modelFactory, webRequest);
    }

    private ServletInvocableHandlerMethod createRequestMappingMethod(HandlerMethod handlerMethod,
                                                                     WebDataBinderFactory binderFactory) {

        ServletInvocableHandlerMethod requestMethod;
        requestMethod = new ServletInvocableHandlerMethod(handlerMethod);
        requestMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        requestMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        requestMethod.setDataBinderFactory(binderFactory);
        requestMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        return requestMethod;
    }

    private ModelFactory getModelFactory(HandlerMethod handlerMethod, WebDataBinderFactory binderFactory) {
        SessionAttributesHandler sessionAttrHandler = getSessionAttributesHandler(handlerMethod);
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.modelAttributeCache.get(handlerType);
        if (methods == null) {
            methods = HandlerMethodSelector.selectMethods(handlerType, MODEL_ATTRIBUTE_METHODS);
            this.modelAttributeCache.put(handlerType, methods);
        }
        List<InvocableHandlerMethod> attrMethods = new ArrayList<>();
        // Global methods first
//        for (Map.Entry<ControllerAdviceBean, Set<Method>> entry : this.modelAttributeAdviceCache.entrySet()) {
//            Object bean = entry.getKey().resolveBean();
//            for (Method method : entry.getValue()) {
//                attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
//            }
//        }
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
        }
        return new ModelFactory(attrMethods, binderFactory, sessionAttrHandler);
    }

    private InvocableHandlerMethod createModelAttributeMethod(WebDataBinderFactory factory, Object bean, Method method) {
        InvocableHandlerMethod attrMethod = new InvocableHandlerMethod(bean, method);
        attrMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        attrMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
        attrMethod.setDataBinderFactory(factory);
        return attrMethod;
    }

    private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
        Class<?> handlerType = handlerMethod.getBeanType();
        Set<Method> methods = this.initBinderCache.get(handlerType);
        if (methods == null) {
            methods = HandlerMethodSelector.selectMethods(handlerType, INIT_BINDER_METHODS);
            this.initBinderCache.put(handlerType, methods);
        }
        List<InvocableHandlerMethod> initBinderMethods = new ArrayList<>();
        // Global methods first
//        for (Map.Entry<ControllerAdviceBean, Set<Method>> entry : this.initBinderAdviceCache.entrySet()) {
//            Object bean = entry.getKey().resolveBean();
//            for (Method method : entry.getValue()) {
//                initBinderMethods.add(createInitBinderMethod(bean, method));
//            }
//        }
        for (Method method : methods) {
            Object bean = handlerMethod.getBean();
            initBinderMethods.add(createInitBinderMethod(bean, method));
        }
        return createDataBinderFactory(initBinderMethods);
    }

    private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {

        InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);

        binderMethod.setHandlerMethodArgumentResolvers(this.initBinderArgumentResolvers);

        binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));

        binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

        return binderMethod;
    }


    protected ServletRequestDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods) throws Exception {

        return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
    }

    /**
     * 将mavContainer中的数据转换成视图
     */
    private ModelAndView getModelAndView(ModelAndViewContainer mavContainer,
                                         ModelFactory modelFactory,
                                         WebRequest webRequest) throws Exception {

        modelFactory.updateModel(webRequest, mavContainer);
        if (mavContainer.isRequestHandled()) {
            return null;
        }
        ModelMap model = mavContainer.getModel();
        ModelAndView mav = new ModelAndView(mavContainer.getViewName(), model);
        if (!mavContainer.isViewReference()) {
            mav.setView((View) mavContainer.getView());
        }
//        if (model instanceof RedirectAttributes) {
//            Map<String, ?> flashAttributes = ((RedirectAttributes) model).getFlashAttributes();
//            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//            RequestContextUtils.getOutputFlashMap(request).putAll(flashAttributes);
//        }

        return mav;
    }

    public static final MethodFilter INIT_BINDER_METHODS = new MethodFilter() {

        public boolean matches(Method method) {
            return AnnotationUtils.findAnnotation(method, InitBinder.class) != null;
        }
    };

    public static final MethodFilter MODEL_ATTRIBUTE_METHODS = new MethodFilter() {

        public boolean matches(Method method) {
            return ((AnnotationUtils.findAnnotation(method, RequestMapping.class) == null) &&
                    (AnnotationUtils.findAnnotation(method, ModelAttribute.class) != null));
        }
    };


}
