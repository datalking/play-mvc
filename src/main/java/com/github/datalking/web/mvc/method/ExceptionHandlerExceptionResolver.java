package com.github.datalking.web.mvc.method;

import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 4/28/18
 */
public class ExceptionHandlerExceptionResolver extends AbstractHandlerMethodExceptionResolver
        implements ApplicationContextAware, InitializingBean {

    private List<HandlerMethodArgumentResolver> customArgumentResolvers;

    private HandlerMethodArgumentResolverComposite argumentResolvers;

    private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    private List<HttpMessageConverter<?>> messageConverters;

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private ApplicationContext applicationContext;

    private final Map<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerCache =
            new ConcurrentHashMap<Class<?>, ExceptionHandlerMethodResolver>(64);

    private final Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> exceptionHandlerAdviceCache =
            new LinkedHashMap<ControllerAdviceBean, ExceptionHandlerMethodResolver>();


    public ExceptionHandlerExceptionResolver() {
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false); // See SPR-7316

        this.messageConverters = new ArrayList<HttpMessageConverter<?>>();
        this.messageConverters.add(new ByteArrayHttpMessageConverter());
        this.messageConverters.add(stringHttpMessageConverter);
        this.messageConverters.add(new SourceHttpMessageConverter<Source>());
        this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());
    }


    /**
     * Provide resolvers for custom argument types. Custom resolvers are ordered
     * after built-in ones. To override the built-in support for argument
     * resolution use {@link #setArgumentResolvers} instead.
     */
    public void setCustomArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.customArgumentResolvers= argumentResolvers;
    }

    /**
     * Return the custom argument resolvers, or {@code null}.
     */
    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
        return this.customArgumentResolvers;
    }

    /**
     * Configure the complete list of supported argument types thus overriding
     * the resolvers that would otherwise be configured by default.
     */
    public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        if (argumentResolvers == null) {
            this.argumentResolvers = null;
        }
        else {
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite();
            this.argumentResolvers.addResolvers(argumentResolvers);
        }
    }

    /**
     * Return the configured argument resolvers, or possibly {@code null} if
     * not initialized yet via {@link #afterPropertiesSet()}.
     */
    public HandlerMethodArgumentResolverComposite getArgumentResolvers() {
        return this.argumentResolvers;
    }

    /**
     * Provide handlers for custom return value types. Custom handlers are
     * ordered after built-in ones. To override the built-in support for
     * return value handling use {@link #setReturnValueHandlers}.
     */
    public void setCustomReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        this.customReturnValueHandlers = returnValueHandlers;
    }

    /**
     * Return the custom return value handlers, or {@code null}.
     */
    public List<HandlerMethodReturnValueHandler> getCustomReturnValueHandlers() {
        return this.customReturnValueHandlers;
    }

    /**
     * Configure the complete list of supported return value types thus
     * overriding handlers that would otherwise be configured by default.
     */
    public void setReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers == null) {
            this.returnValueHandlers = null;
        }
        else {
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite();
            this.returnValueHandlers.addHandlers(returnValueHandlers);
        }
    }

    /**
     * Return the configured handlers, or possibly {@code null} if not
     * initialized yet via {@link #afterPropertiesSet()}.
     */
    public HandlerMethodReturnValueHandlerComposite getReturnValueHandlers() {
        return this.returnValueHandlers;
    }

    /**
     * Set the message body converters to use.
     * <p>These converters are used to convert from and to HTTP requests and responses.
     */
    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    /**
     * Return the configured message body converters.
     */
    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }

    /**
     * Set the {@link ContentNegotiationManager} to use to determine requested media types.
     * If not set, the default constructor is used.
     */
    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }


    public void afterPropertiesSet() {
        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
        initExceptionHandlerAdviceCache();
    }

    /**
     * Return the list of argument resolvers to use including built-in resolvers
     * and custom resolvers provided via {@link #setCustomArgumentResolvers}.
     */
    protected List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());

        // Custom arguments
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        return resolvers;
    }

    /**
     * Return the list of return value handlers to use including built-in and
     * custom handlers provided via {@link #setReturnValueHandlers}.
     */
    protected List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<HandlerMethodReturnValueHandler>();

        // Single-purpose return value types
        handlers.add(new ModelAndViewMethodReturnValueHandler());
        handlers.add(new ModelMethodProcessor());
        handlers.add(new ViewMethodReturnValueHandler());
        handlers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.contentNegotiationManager));

        // Annotation-based return value types
        handlers.add(new ModelAttributeMethodProcessor(false));
        handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.contentNegotiationManager));

        // Multi-purpose return value types
        handlers.add(new ViewNameMethodReturnValueHandler());
        handlers.add(new MapMethodProcessor());

        // Custom return value types
        if (getCustomReturnValueHandlers() != null) {
            handlers.addAll(getCustomReturnValueHandlers());
        }

        // Catch-all
        handlers.add(new ModelAttributeMethodProcessor(true));

        return handlers;
    }

    private void initExceptionHandlerAdviceCache() {
        if (getApplicationContext() == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for exception mappings: " + getApplicationContext());
        }

        List<ControllerAdviceBean> beans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
        Collections.sort(beans, new OrderComparator());

        for (ControllerAdviceBean bean : beans) {
            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(bean.getBeanType());
            if (resolver.hasExceptionMappings()) {
                this.exceptionHandlerAdviceCache.put(bean, resolver);
                logger.info("Detected @ExceptionHandler methods in " + bean);
            }
        }
    }


    /**
     * Find an {@code @ExceptionHandler} method and invoke it to handle the raised exception.
     */
    @Override
    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request,
                                                           HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {

        ServletInvocableHandlerMethod exceptionHandlerMethod = getExceptionHandlerMethod(handlerMethod, exception);
        if (exceptionHandlerMethod == null) {
            return null;
        }

        exceptionHandlerMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        exceptionHandlerMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);

        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        ModelAndViewContainer mavContainer = new ModelAndViewContainer();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking @ExceptionHandler method: " + exceptionHandlerMethod);
            }
            exceptionHandlerMethod.invokeAndHandle(webRequest, mavContainer, exception);
        }
        catch (Exception invocationEx) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to invoke @ExceptionHandler method: " + exceptionHandlerMethod, invocationEx);
            }
            return null;
        }

        if (mavContainer.isRequestHandled()) {
            return new ModelAndView();
        }
        else {
            ModelAndView mav = new ModelAndView().addAllObjects(mavContainer.getModel());
            mav.setViewName(mavContainer.getViewName());
            if (!mavContainer.isViewReference()) {
                mav.setView((View) mavContainer.getView());
            }
            return mav;
        }
    }

    /**
     * Find an {@code @ExceptionHandler} method for the given exception. The default
     * implementation searches methods in the class hierarchy of the controller first
     * and if not found, it continues searching for additional {@code @ExceptionHandler}
     * methods assuming some {@linkplain ControllerAdvice @ControllerAdvice}
     * Spring-managed beans were detected.
     * @param handlerMethod the method where the exception was raised (may be {@code null})
     * @param exception the raised exception
     * @return a method to handle the exception, or {@code null}
     */
    protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
        if (handlerMethod != null) {
            Class<?> handlerType = handlerMethod.getBeanType();
            ExceptionHandlerMethodResolver resolver = this.exceptionHandlerCache.get(handlerType);
            if (resolver == null) {
                resolver = new ExceptionHandlerMethodResolver(handlerType);
                this.exceptionHandlerCache.put(handlerType, resolver);
            }
            Method method = resolver.resolveMethod(exception);
            if (method != null) {
                return new ServletInvocableHandlerMethod(handlerMethod.getBean(), method);
            }
        }

        for (Map.Entry<ControllerAdviceBean, ExceptionHandlerMethodResolver> entry : this.exceptionHandlerAdviceCache.entrySet()) {
            Method method = entry.getValue().resolveMethod(exception);
            if (method != null) {
                return new ServletInvocableHandlerMethod(entry.getKey().resolveBean(), method);
            }
        }

        return null;
    }

}
