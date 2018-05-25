package com.github.datalking.web.mvc.method;

import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.http.converter.StringHttpMessageConverter;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.View;
import com.github.datalking.web.servlet.ServletWebRequest;
import com.github.datalking.web.servlet.handler.AbstractHandlerMethodExceptionResolver;
import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodArgumentResolverComposite;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;
import com.github.datalking.web.support.HandlerMethodReturnValueHandlerComposite;
import com.github.datalking.web.support.HttpEntityMethodProcessor;
import com.github.datalking.web.support.MapMethodProcessor;
import com.github.datalking.web.support.ModelAndViewContainer;
import com.github.datalking.web.support.ModelAndViewMethodReturnValueHandler;
import com.github.datalking.web.support.ModelAttributeMethodProcessor;
import com.github.datalking.web.support.ModelMethodProcessor;
import com.github.datalking.web.support.ServletInvocableHandlerMethod;
import com.github.datalking.web.support.ServletRequestMethodArgumentResolver;
import com.github.datalking.web.support.ServletResponseMethodArgumentResolver;
import com.github.datalking.web.support.ViewMethodReturnValueHandler;
import com.github.datalking.web.support.ViewNameMethodReturnValueHandler;

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

//    private final Map<Class<?>, ExceptionHandlerMethodResolver> exceptionHandlerCache = new ConcurrentHashMap<>(64);
//    private final Map<ControllerAdviceBean, ExceptionHandlerMethodResolver> exceptionHandlerAdviceCache = new LinkedHashMap<>();


    public ExceptionHandlerExceptionResolver() {
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false);

        this.messageConverters = new ArrayList<>();
//        this.messageConverters.add(new ByteArrayHttpMessageConverter());
        this.messageConverters.add(stringHttpMessageConverter);
//        this.messageConverters.add(new SourceHttpMessageConverter<Source>());
//        this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());
    }

    public void setCustomArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        this.customArgumentResolvers = argumentResolvers;
    }

    public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
        return this.customArgumentResolvers;
    }

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

    public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }

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

    protected List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Type-based argument resolution
        resolvers.add(new ServletRequestMethodArgumentResolver());
        resolvers.add(new ServletResponseMethodArgumentResolver());

        // Custom arguments
        if (getCustomArgumentResolvers() != null) {
            resolvers.addAll(getCustomArgumentResolvers());
        }

        return resolvers;
    }

    protected List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

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

//        List<ControllerAdviceBean> beans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
//        Collections.sort(beans, new OrderComparator());
//
//        for (ControllerAdviceBean bean : beans) {
//            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(bean.getBeanType());
//            if (resolver.hasExceptionMappings()) {
//                this.exceptionHandlerAdviceCache.put(bean, resolver);
//                logger.info("Detected @ExceptionHandler methods in " + bean);
//            }
//        }
    }


    @Override
    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request,
                                                           HttpServletResponse response,
                                                           HandlerMethod handlerMethod,
                                                           Exception exception) {

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
        } catch (Exception invocationEx) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to invoke @ExceptionHandler method: " + exceptionHandlerMethod, invocationEx);
            }
            return null;
        }

        if (mavContainer.isRequestHandled()) {
            return new ModelAndView();
        } else {
            ModelAndView mav = new ModelAndView().addAllObjects(mavContainer.getModel());
            mav.setViewName(mavContainer.getViewName());
            if (!mavContainer.isViewReference()) {
                mav.setView((View) mavContainer.getView());
            }
            return mav;
        }
    }


    protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
        if (handlerMethod != null) {
            Class<?> handlerType = handlerMethod.getBeanType();
//            ExceptionHandlerMethodResolver resolver = this.exceptionHandlerCache.get(handlerType);
            ExceptionHandlerMethodResolver resolver = null;
            if (resolver == null) {
                resolver = new ExceptionHandlerMethodResolver(handlerType);
//                this.exceptionHandlerCache.put(handlerType, resolver);
            }
            Method method = resolver.resolveMethod(exception);
            if (method != null) {
                return new ServletInvocableHandlerMethod(handlerMethod.getBean(), method);
            }
        }

//        for (Map.Entry<ControllerAdviceBean, ExceptionHandlerMethodResolver> entry : this.exceptionHandlerAdviceCache.entrySet()) {
//            Method method = entry.getValue().resolveMethod(exception);
//            if (method != null) {
//                return new ServletInvocableHandlerMethod(entry.getKey().resolveBean(), method);
//            }
//        }

        return null;
    }

}
