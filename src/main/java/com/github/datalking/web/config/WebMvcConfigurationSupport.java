package com.github.datalking.web.config;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextAware;
import com.github.datalking.exception.Errors;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.support.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 在代码中配置mvc的主要类
 *
 * @author yaoo on 4/25/18
 */
public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {

    private static boolean romePresent = ClassUtils.isPresent("com.sun.syndication.feed.WireFeed", WebMvcConfigurationSupport.class.getClassLoader());

    private static final boolean jaxb2Present = ClassUtils.isPresent("javax.xml.bind.Binder", WebMvcConfigurationSupport.class.getClassLoader());

    private static final boolean jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", WebMvcConfigurationSupport.class.getClassLoader()) &&
            ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", WebMvcConfigurationSupport.class.getClassLoader());

    private static final boolean jacksonPresent = ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", WebMvcConfigurationSupport.class.getClassLoader()) &&
            ClassUtils.isPresent("org.codehaus.jackson.JsonGenerator", WebMvcConfigurationSupport.class.getClassLoader());


    private ApplicationContext applicationContext;

    private ServletContext servletContext;

    private List<Object> interceptors;

    private PathMatchConfigurer pathMatchConfigurer;

    private ContentNegotiationManager contentNegotiationManager;

    private List<HttpMessageConverter<?>> messageConverters;


    /**
     * Set the Spring {@link ApplicationContext}, e.g. for resource loading.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Set the {@link javax.servlet.ServletContext}, e.g. for resource handling,
     * looking up file extensions, etc.
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    /**
     * Return a {@link RequestMappingHandlerMapping} ordered at 0 for mapping
     * requests to annotated controllers.
     */
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

    /**
     * Provide access to the shared handler interceptors used to configure
     * {@link HandlerMapping} instances with. This method cannot be overridden,
     * use {@link #addInterceptors(InterceptorRegistry)} instead.
     */
    protected final Object[] getInterceptors() {
        if (this.interceptors == null) {
            InterceptorRegistry registry = new InterceptorRegistry();
            addInterceptors(registry);
            registry.addInterceptor(new ConversionServiceExposingInterceptor(mvcConversionService()));
            this.interceptors = registry.getInterceptors();
        }
        return this.interceptors.toArray();
    }

    /**
     * Override this method to add Spring MVC interceptors for
     * pre- and post-processing of controller invocation.
     *
     * @see InterceptorRegistry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
    }

    /**
     * Callback for building the {@link PathMatchConfigurer}.
     * Delegates to {@link #configurePathMatch}.
     *
     * @since 3.2.17
     */
    protected PathMatchConfigurer getPathMatchConfigurer() {
        if (this.pathMatchConfigurer == null) {
            this.pathMatchConfigurer = new PathMatchConfigurer();
            configurePathMatch(this.pathMatchConfigurer);
        }
        return this.pathMatchConfigurer;
    }

    /**
     * Override this method to configure path matching options.
     *
     * @see PathMatchConfigurer
     * @since 3.2.17
     */
    public void configurePathMatch(PathMatchConfigurer configurer) {
    }

    /**
     * Return a global {@link PathMatcher} instance for path matching
     * patterns in {@link HandlerMapping}s.
     * This instance can be configured using the {@link PathMatchConfigurer}
     * in {@link #configurePathMatch(PathMatchConfigurer)}.
     *
     * @since 3.2.17
     */
    @Bean
    public PathMatcher mvcPathMatcher() {
        PathMatcher pathMatcher = getPathMatchConfigurer().getPathMatcher();
        return (pathMatcher != null ? pathMatcher : new AntPathMatcher());
    }

    /**
     * Return a global {@link UrlPathHelper} instance for path matching
     * patterns in {@link HandlerMapping}s.
     * This instance can be configured using the {@link PathMatchConfigurer}
     * in {@link #configurePathMatch(PathMatchConfigurer)}.
     *
     * @since 3.2.17
     */
    @Bean
    public UrlPathHelper mvcUrlPathHelper() {
        UrlPathHelper pathHelper = getPathMatchConfigurer().getUrlPathHelper();
        return (pathHelper != null ? pathHelper : new UrlPathHelper());
    }

    /**
     * Return a {@link ContentNegotiationManager} instance to use to determine
     * requested {@linkplain MediaType media types} in a given request.
     */
    @Bean
    public ContentNegotiationManager mvcContentNegotiationManager() {
        if (this.contentNegotiationManager == null) {
            ContentNegotiationConfigurer configurer = new ContentNegotiationConfigurer(this.servletContext);
            configurer.mediaTypes(getDefaultMediaTypes());
            configureContentNegotiation(configurer);
            try {
                this.contentNegotiationManager = configurer.getContentNegotiationManager();
            } catch (Exception ex) {
                throw new BeanInitializationException("Could not create ContentNegotiationManager", ex);
            }
        }
        return this.contentNegotiationManager;
    }

    protected Map<String, MediaType> getDefaultMediaTypes() {
        Map<String, MediaType> map = new HashMap<String, MediaType>(4);
        if (romePresent) {
            map.put("atom", MediaType.APPLICATION_ATOM_XML);
            map.put("rss", MediaType.valueOf("application/rss+xml"));
        }
        if (jaxb2Present) {
            map.put("xml", MediaType.APPLICATION_XML);
        }
        if (jackson2Present || jacksonPresent) {
            map.put("json", MediaType.APPLICATION_JSON);
        }
        return map;
    }

    /**
     * Override this method to configure content negotiation.
     *
     * @see DefaultServletHandlerConfigurer
     */
    protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    }

    /**
     * Return a handler mapping ordered at 1 to map URL paths directly to
     * view names. To configure view controllers, override
     * {@link #addViewControllers}.
     */
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

    /**
     * Override this method to add view controllers.
     *
     * @see ViewControllerRegistry
     */
    protected void addViewControllers(ViewControllerRegistry registry) {
    }

    /**
     * Return a {@link BeanNameUrlHandlerMapping} ordered at 2 to map URL
     * paths to controller bean names.
     */
    @Bean
    public BeanNameUrlHandlerMapping beanNameHandlerMapping() {
        BeanNameUrlHandlerMapping mapping = new BeanNameUrlHandlerMapping();
        mapping.setOrder(2);
        mapping.setInterceptors(getInterceptors());
        return mapping;
    }

    /**
     * Return a handler mapping ordered at Integer.MAX_VALUE-1 with mapped
     * resource handlers. To configure resource handling, override
     * {@link #addResourceHandlers}.
     */
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

    /**
     * Override this method to add resource handlers for serving static resources.
     *
     * @see ResourceHandlerRegistry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /**
     * Return a handler mapping ordered at Integer.MAX_VALUE with a mapped
     * default servlet handler. To configure "default" Servlet handling,
     * override {@link #configureDefaultServletHandling}.
     */
    @Bean
    public HandlerMapping defaultServletHandlerMapping() {
        DefaultServletHandlerConfigurer configurer = new DefaultServletHandlerConfigurer(servletContext);
        configureDefaultServletHandling(configurer);
        AbstractHandlerMapping handlerMapping = configurer.getHandlerMapping();
        handlerMapping = handlerMapping != null ? handlerMapping : new EmptyHandlerMapping();
        return handlerMapping;
    }

    /**
     * Override this method to configure "default" Servlet handling.
     *
     * @see DefaultServletHandlerConfigurer
     */
    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }

    /**
     * Returns a {@link RequestMappingHandlerAdapter} for processing requests
     * through annotated controller methods. Consider overriding one of these
     * other more fine-grained methods:
     * <ul>
     * <li>{@link #addArgumentResolvers} for adding custom argument resolvers.
     * <li>{@link #addReturnValueHandlers} for adding custom return value handlers.
     * <li>{@link #configureMessageConverters} for adding custom message converters.
     * </ul>
     */
    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<HandlerMethodArgumentResolver>();
        addArgumentResolvers(argumentResolvers);

        List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<HandlerMethodReturnValueHandler>();
        addReturnValueHandlers(returnValueHandlers);

        RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        adapter.setContentNegotiationManager(mvcContentNegotiationManager());
        adapter.setMessageConverters(getMessageConverters());
        adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
        adapter.setCustomArgumentResolvers(argumentResolvers);
        adapter.setCustomReturnValueHandlers(returnValueHandlers);

        AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();
        configureAsyncSupport(configurer);
        if (configurer.getTaskExecutor() != null) {
            adapter.setTaskExecutor(configurer.getTaskExecutor());
        }
        if (configurer.getTimeout() != null) {
            adapter.setAsyncRequestTimeout(configurer.getTimeout());
        }
        adapter.setCallableInterceptors(configurer.getCallableInterceptors());
        adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());

        return adapter;
    }

    /**
     * Return the {@link ConfigurableWebBindingInitializer} to use for
     * initializing all {@link WebDataBinder} instances.
     */
    protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer() {
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        initializer.setConversionService(mvcConversionService());
        initializer.setValidator(mvcValidator());
        initializer.setMessageCodesResolver(getMessageCodesResolver());
        return initializer;
    }

    /**
     * Override this method to provide a custom {@link MessageCodesResolver}.
     */
    protected MessageCodesResolver getMessageCodesResolver() {
        return null;
    }

    /**
     * Override this method to configure asynchronous request processing options.
     *
     * @see AsyncSupportConfigurer
     */
    protected void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    }

    /**
     * Return a {@link FormattingConversionService} for use with annotated
     * controller methods and the {@code spring:eval} JSP tag.
     * Also see {@link #addFormatters} as an alternative to overriding this method.
     */
    @Bean
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        addFormatters(conversionService);
        return conversionService;
    }

    /**
     * Override this method to add custom {@link Converter}s and {@link Formatter}s.
     */
    protected void addFormatters(FormatterRegistry registry) {
    }

    /**
     * Return a global {@link Validator} instance for example for validating
     * {@code @ModelAttribute} and {@code @RequestBody} method arguments.
     * Delegates to {@link #getValidator()} first and if that returns {@code null}
     * checks the classpath for the presence of a JSR-303 implementations
     * before creating a {@code LocalValidatorFactoryBean}.If a JSR-303
     * implementation is not available, a no-op {@link Validator} is returned.
     */
    @Bean
    public Validator mvcValidator() {
        Validator validator = getValidator();
        if (validator == null) {
            if (ClassUtils.isPresent("javax.validation.Validator", getClass().getClassLoader())) {
                Class<?> clazz;
                try {
                    String className = "org.springframework.validation.beanvalidation.LocalValidatorFactoryBean";
                    clazz = ClassUtils.forName(className, WebMvcConfigurationSupport.class.getClassLoader());
                } catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException("Could not find default validator class", ex);
                } catch (LinkageError ex) {
                    throw new BeanInitializationException("Could not load default validator class", ex);
                }
                validator = (Validator) BeanUtils.instantiateClass(clazz);
            } else {
                validator = new NoOpValidator();
            }
        }
        return validator;
    }

    /**
     * Override this method to provide a custom {@link Validator}.
     */
    protected Validator getValidator() {
        return null;
    }

    /**
     * Add custom {@link HandlerMethodArgumentResolver}s to use in addition to
     * the ones registered by default.
     * <p>Custom argument resolvers are invoked before built-in resolvers
     * except for those that rely on the presence of annotations (e.g.
     * {@code @RequestParameter}, {@code @PathVariable}, etc.).
     * The latter can  be customized by configuring the
     * {@link RequestMappingHandlerAdapter} directly.
     *
     * @param argumentResolvers the list of custom converters;
     *                          initially an empty list.
     */
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    /**
     * Add custom {@link HandlerMethodReturnValueHandler}s in addition to the
     * ones registered by default.
     * <p>Custom return value handlers are invoked before built-in ones except
     * for those that rely on the presence of annotations (e.g.
     * {@code @ResponseBody}, {@code @ModelAttribute}, etc.).
     * The latter can be customized by configuring the
     * {@link RequestMappingHandlerAdapter} directly.
     *
     * @param returnValueHandlers the list of custom handlers;
     *                            initially an empty list.
     */
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    /**
     * Provides access to the shared {@link HttpMessageConverter}s used by the
     * {@link RequestMappingHandlerAdapter} and the
     * {@link ExceptionHandlerExceptionResolver}.
     * This method cannot be overridden.
     * Use {@link #configureMessageConverters(List)} instead.
     * Also see {@link #addDefaultHttpMessageConverters(List)} that can be
     * used to add default message converters.
     */
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

    /**
     * Override this method to add custom {@link HttpMessageConverter}s to use
     * with the {@link RequestMappingHandlerAdapter} and the
     * {@link ExceptionHandlerExceptionResolver}. Adding converters to the
     * list turns off the default converters that would otherwise be registered
     * by default. Also see {@link #addDefaultHttpMessageConverters(List)} that
     * can be used to add default message converters.
     *
     * @param converters a list to add message converters to;
     *                   initially an empty list.
     */
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * Adds a set of default HttpMessageConverter instances to the given list.
     * Subclasses can call this method from {@link #configureMessageConverters(List)}.
     *
     * @param messageConverters the list to add the default message converters to
     */
    protected final void addDefaultHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);

        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(stringConverter);
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<Source>());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());

        if (romePresent) {
            messageConverters.add(new AtomFeedHttpMessageConverter());
            messageConverters.add(new RssChannelHttpMessageConverter());
        }
        if (jaxb2Present) {
            messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
        }
        if (jackson2Present) {
            messageConverters.add(new MappingJackson2HttpMessageConverter());
        } else if (jacksonPresent) {
            messageConverters.add(new MappingJacksonHttpMessageConverter());
        }
    }

    /**
     * Returns a {@link HttpRequestHandlerAdapter} for processing requests
     * with {@link HttpRequestHandler}s.
     */
    @Bean
    public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
        return new HttpRequestHandlerAdapter();
    }

    /**
     * Returns a {@link SimpleControllerHandlerAdapter} for processing requests
     * with interface-based controllers.
     */
    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    /**
     * Returns a {@link HandlerExceptionResolverComposite} containing a list
     * of exception resolvers obtained either through
     * {@link #configureHandlerExceptionResolvers(List)} or through
     * {@link #addDefaultHandlerExceptionResolvers(List)}.
     * <p><strong>Note:</strong> This method cannot be made final due to CGLib
     * constraints. Rather than overriding it, consider overriding
     * {@link #configureHandlerExceptionResolvers(List)}, which allows
     * providing a list of resolvers.
     */
    @Bean
    public HandlerExceptionResolver handlerExceptionResolver() {
        List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<HandlerExceptionResolver>();
        configureHandlerExceptionResolvers(exceptionResolvers);
        if (exceptionResolvers.isEmpty()) {
            addDefaultHandlerExceptionResolvers(exceptionResolvers);
        }
        HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
        composite.setOrder(0);
        composite.setExceptionResolvers(exceptionResolvers);
        return composite;
    }

    /**
     * Override this method to configure the list of
     * {@link HandlerExceptionResolver}s to use. Adding resolvers to the list
     * turns off the default resolvers that would otherwise be registered by
     * default. Also see {@link #addDefaultHandlerExceptionResolvers(List)}
     * that can be used to add the default exception resolvers.
     *
     * @param exceptionResolvers a list to add exception resolvers to;
     *                           initially an empty list.
     */
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    }

    /**
     * A method available to subclasses for adding default {@link HandlerExceptionResolver}s.
     * <p>Adds the following exception resolvers:
     * <ul>
     * <li>{@link ExceptionHandlerExceptionResolver}
     * for handling exceptions through @{@link ExceptionHandler} methods.
     * <li>{@link ResponseStatusExceptionResolver}
     * for exceptions annotated with @{@link ResponseStatus}.
     * <li>{@link DefaultHandlerExceptionResolver}
     * for resolving known Spring exception types
     * </ul>
     */
    protected final void addDefaultHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new ExceptionHandlerExceptionResolver();
        exceptionHandlerExceptionResolver.setContentNegotiationManager(mvcContentNegotiationManager());
        exceptionHandlerExceptionResolver.setMessageConverters(getMessageConverters());
        exceptionHandlerExceptionResolver.setApplicationContext(this.applicationContext);
        exceptionHandlerExceptionResolver.afterPropertiesSet();
        exceptionResolvers.add(exceptionHandlerExceptionResolver);

        ResponseStatusExceptionResolver responseStatusExceptionResolver = new ResponseStatusExceptionResolver();
        responseStatusExceptionResolver.setMessageSource(this.applicationContext);
        exceptionResolvers.add(responseStatusExceptionResolver);

        exceptionResolvers.add(new DefaultHandlerExceptionResolver());
    }


    private static final class EmptyHandlerMapping extends AbstractHandlerMapping {

        @Override
        protected Object getHandlerInternal(HttpServletRequest request) {
            return null;
        }
    }



//    private static final class NoOpValidator implements Validator {
//
//        @Override
//        public boolean supports(Class<?> clazz) {
//            return false;
//        }
//
//        @Override
//        public void validate(Object target, Errors errors) {
//        }
//    }

}
