package com.github.datalking.web.config;

import com.github.datalking.annotation.Bean;
import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.util.AntPathMatcher;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.PathMatcher;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.web.bind.ConfigurableWebBindingInitializer;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.http.converter.MappingJackson2HttpMessageConverter;
import com.github.datalking.web.http.converter.StringHttpMessageConverter;
import com.github.datalking.web.mvc.HttpRequestHandlerAdapter;
import com.github.datalking.web.mvc.SimpleControllerHandlerAdapter;
import com.github.datalking.web.mvc.method.ExceptionHandlerExceptionResolver;
import com.github.datalking.web.mvc.method.RequestMappingHandlerAdapter;
import com.github.datalking.web.mvc.method.RequestMappingHandlerMapping;
import com.github.datalking.web.mvc.method.ResponseStatusExceptionResolver;
import com.github.datalking.web.servlet.HandlerExceptionResolver;
import com.github.datalking.web.servlet.HandlerMapping;
import com.github.datalking.web.servlet.handler.AbstractHandlerMapping;
import com.github.datalking.web.servlet.handler.ConversionServiceExposingInterceptor;
import com.github.datalking.web.servlet.handler.HandlerExceptionResolverComposite;
import com.github.datalking.web.support.DefaultHandlerExceptionResolver;
import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;
import com.github.datalking.web.support.PathMatchConfigurer;
import com.github.datalking.web.support.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在代码中配置mvc的主要类
 *
 * @author yaoo on 4/25/18
 */
public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

    private static final boolean jaxb2Present = ClassUtils.isPresent(
            "javax.xml.bind.Binder",
            WebMvcConfigurationSupport.class.getClassLoader());

    private static final boolean jackson2Present = ClassUtils.isPresent(
            "com.fasterxml.jackson.databind.ObjectMapper",
            WebMvcConfigurationSupport.class.getClassLoader()) && ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", WebMvcConfigurationSupport.class.getClassLoader());

    private ApplicationContext applicationContext;

    private ServletContext servletContext;

    private List<Object> interceptors;

    private PathMatchConfigurer pathMatchConfigurer;

    private ContentNegotiationManager contentNegotiationManager;

    private List<HttpMessageConverter<?>> messageConverters;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setOrder(0);
        handlerMapping.setInterceptors(getInterceptors());
        handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());

        PathMatchConfigurer configurer = getPathMatchConfigurer();
        if (configurer.isUseSuffixPatternMatch() != null) {
            handlerMapping.setUseSuffixPatternMatch(configurer.isUseSuffixPatternMatch());
        }
        if (configurer.isUseRegisteredSuffixPatternMatch() != null) {
            handlerMapping.setUseRegisteredSuffixPatternMatch(configurer.isUseRegisteredSuffixPatternMatch());
        }
        if (configurer.isUseTrailingSlashMatch() != null) {
            handlerMapping.setUseTrailingSlashMatch(configurer.isUseTrailingSlashMatch());
        }
        UrlPathHelper pathHelper = configurer.getUrlPathHelper();
        if (pathHelper != null) {
            handlerMapping.setUrlPathHelper(pathHelper);
        }
        PathMatcher pathMatcher = configurer.getPathMatcher();
        if (pathMatcher != null) {
            handlerMapping.setPathMatcher(pathMatcher);
        }

        return handlerMapping;
    }

    protected final Object[] getInterceptors() {
        if (this.interceptors == null) {
            InterceptorRegistry registry = new InterceptorRegistry();
            addInterceptors(registry);
//            registry.addInterceptor(new ConversionServiceExposingInterceptor(mvcConversionService()));
            this.interceptors = registry.getInterceptors();
        }
        return this.interceptors.toArray();
    }

    protected void addInterceptors(InterceptorRegistry registry) {
    }

    protected PathMatchConfigurer getPathMatchConfigurer() {
        if (this.pathMatchConfigurer == null) {
            this.pathMatchConfigurer = new PathMatchConfigurer();
            configurePathMatch(this.pathMatchConfigurer);
        }
        return this.pathMatchConfigurer;
    }

    public void configurePathMatch(PathMatchConfigurer configurer) {
    }

    @Bean
    public PathMatcher mvcPathMatcher() {
        PathMatcher pathMatcher = getPathMatchConfigurer().getPathMatcher();
        return (pathMatcher != null ? pathMatcher : new AntPathMatcher());
    }

    @Bean
    public UrlPathHelper mvcUrlPathHelper() {
        UrlPathHelper pathHelper = getPathMatchConfigurer().getUrlPathHelper();
        return (pathHelper != null ? pathHelper : new UrlPathHelper());
    }

    @Bean
    public ContentNegotiationManager mvcContentNegotiationManager() {
        if (this.contentNegotiationManager == null) {
            ContentNegotiationConfigurer configurer = new ContentNegotiationConfigurer(this.servletContext);
            configurer.mediaTypes(getDefaultMediaTypes());
            configureContentNegotiation(configurer);
            try {
                this.contentNegotiationManager = configurer.getContentNegotiationManager();
            } catch (Exception ex) {
//                throw new BeanInitializationException("Could not create ContentNegotiationManager", ex);
                ex.printStackTrace();
            }
        }
        return this.contentNegotiationManager;
    }

    protected Map<String, MediaType> getDefaultMediaTypes() {
        Map<String, MediaType> map = new HashMap<>(4);
        if (jaxb2Present) {
            map.put("xml", MediaType.APPLICATION_XML);
        }
        if (jackson2Present) {
            map.put("json", MediaType.APPLICATION_JSON);
        }
        return map;
    }

    protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    }

    @Bean
    public HandlerMapping viewControllerHandlerMapping() {
        ViewControllerRegistry registry = new ViewControllerRegistry();
        addViewControllers(registry);

        AbstractHandlerMapping handlerMapping = registry.getHandlerMapping();
        handlerMapping = (handlerMapping != null ? handlerMapping : new EmptyHandlerMapping());
        handlerMapping.setInterceptors(getInterceptors());
        handlerMapping.setPathMatcher(mvcPathMatcher());
        handlerMapping.setUrlPathHelper(mvcUrlPathHelper());
        return handlerMapping;
    }

    protected void addViewControllers(ViewControllerRegistry registry) {
    }

//    @Bean
//    public BeanNameUrlHandlerMapping beanNameHandlerMapping() {
//        BeanNameUrlHandlerMapping mapping = new BeanNameUrlHandlerMapping();
//        mapping.setOrder(2);
//        mapping.setInterceptors(getInterceptors());
//        return mapping;
//    }

    @Bean
    public HandlerMapping resourceHandlerMapping() {
        ResourceHandlerRegistry registry = new ResourceHandlerRegistry(this.applicationContext, this.servletContext);
        addResourceHandlers(registry);

        AbstractHandlerMapping handlerMapping = registry.getHandlerMapping();
        if (handlerMapping != null) {
            handlerMapping.setPathMatcher(mvcPathMatcher());
            handlerMapping.setUrlPathHelper(mvcUrlPathHelper());
        } else {
            handlerMapping = new EmptyHandlerMapping();
        }
        return handlerMapping;
    }

    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    @Bean
    public HandlerMapping defaultServletHandlerMapping() {
        DefaultServletHandlerConfigurer configurer = new DefaultServletHandlerConfigurer(servletContext);
        configureDefaultServletHandling(configurer);
        AbstractHandlerMapping handlerMapping = configurer.getHandlerMapping();
        handlerMapping = handlerMapping != null ? handlerMapping : new EmptyHandlerMapping();
        return handlerMapping;
    }

    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }

    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
        addArgumentResolvers(argumentResolvers);

        List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();
        addReturnValueHandlers(returnValueHandlers);

        RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        adapter.setContentNegotiationManager(mvcContentNegotiationManager());
        adapter.setMessageConverters(getMessageConverters());
        adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
//        adapter.setCustomArgumentResolvers(argumentResolvers);
//        adapter.setCustomReturnValueHandlers(returnValueHandlers);

//        AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
//        configureAsyncSupport(configurer);
//        if (configurer.getTaskExecutor() != null) {
//            adapter.setTaskExecutor(configurer.getTaskExecutor());
//        }
//        if (configurer.getTimeout() != null) {
//            adapter.setAsyncRequestTimeout(configurer.getTimeout());
//        }
//        adapter.setCallableInterceptors(configurer.getCallableInterceptors());
//        adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());

        return adapter;
    }

    protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer() {
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
//        initializer.setConversionService(mvcConversionService());
//        initializer.setValidator(mvcValidator());
//        initializer.setMessageCodesResolver(getMessageCodesResolver());
        return initializer;
    }

//    protected MessageCodesResolver getMessageCodesResolver() {
//        return null;
//    }
//
//    protected void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//    }

//    @Bean
//    public FormattingConversionService mvcConversionService() {
//        FormattingConversionService conversionService = new DefaultFormattingConversionService();
//        addFormatters(conversionService);
//        return conversionService;
//    }

//    protected void addFormatters(FormatterRegistry registry) {
//    }

//    @Bean
//    public Validator mvcValidator() {
//        Validator validator = getValidator();
//        if (validator == null) {
//            if (ClassUtils.isPresent("javax.validation.Validator", getClass().getClassLoader())) {
//                Class<?> clazz;
//                try {
//                    String className = "org.springframework.validation.beanvalidation.LocalValidatorFactoryBean";
//                    clazz = ClassUtils.forName(className, WebMvcConfigurationSupport.class.getClassLoader());
//                } catch (ClassNotFoundException ex) {
//                    throw new BeanInitializationException("Could not find default validator class", ex);
//                } catch (LinkageError ex) {
//                    throw new BeanInitializationException("Could not load default validator class", ex);
//                }
////                validator = (Validator) BeanUtils.instantiateClass(clazz);
//            } else {
//                validator = new NoOpValidator();
//            }
//        }
//        return validator;
//    }

//    protected Validator getValidator() {
//        return null;
//    }

    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    protected final List<HttpMessageConverter<?>> getMessageConverters() {
        if (this.messageConverters == null) {
            this.messageConverters = new ArrayList<HttpMessageConverter<?>>();
            configureMessageConverters(this.messageConverters);
            if (this.messageConverters.isEmpty()) {
                addDefaultHttpMessageConverters(this.messageConverters);
            }
        }
        return this.messageConverters;
    }

    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    protected final void addDefaultHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);

        messageConverters.add(stringConverter);
//        messageConverters.add(new ByteArrayHttpMessageConverter());
//        messageConverters.add(new ResourceHttpMessageConverter());
//        messageConverters.add(new SourceHttpMessageConverter<Source>());
//        messageConverters.add(new AllEncompassingFormHttpMessageConverter());

//        if (jaxb2Present) {
//            messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
//        }
        if (jackson2Present) {
            messageConverters.add(new MappingJackson2HttpMessageConverter());
        }
    }

    @Bean
    public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
        return new HttpRequestHandlerAdapter();
    }

    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    @Bean
    public HandlerExceptionResolver handlerExceptionResolver() {
        List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
        configureHandlerExceptionResolvers(exceptionResolvers);
        if (exceptionResolvers.isEmpty()) {
            addDefaultHandlerExceptionResolvers(exceptionResolvers);
        }
        HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
        composite.setOrder(0);
        composite.setExceptionResolvers(exceptionResolvers);
        return composite;
    }

    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    }

    protected final void addDefaultHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionHandlerExceptionResolver.setContentNegotiationManager(mvcContentNegotiationManager());
        exceptionHandlerExceptionResolver.setMessageConverters(getMessageConverters());
        exceptionHandlerExceptionResolver.setApplicationContext(this.applicationContext);
        exceptionHandlerExceptionResolver.afterPropertiesSet();
        exceptionResolvers.add(exceptionHandlerExceptionResolver);

        ResponseStatusExceptionResolver responseStatusExceptionResolver = new ResponseStatusExceptionResolver();
//        responseStatusExceptionResolver.setMessageSource(this.applicationContext);
        exceptionResolvers.add(responseStatusExceptionResolver);

        exceptionResolvers.add(new DefaultHandlerExceptionResolver());
    }

    private static final class EmptyHandlerMapping extends AbstractHandlerMapping {

        @Override
        protected Object getHandlerInternal(HttpServletRequest request) {
            return null;
        }
    }

}
