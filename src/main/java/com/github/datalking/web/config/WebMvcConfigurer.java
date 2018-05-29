package com.github.datalking.web.config;

import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;

import java.util.List;

/**
 * 提供给带有@EnbaleWebMvc的配置类修改web应用配置的接口
 *
 * @author yaoo on 4/23/18
 */
public interface WebMvcConfigurer {

//    void addFormatters(FormatterRegistry registry);
//    void configureMessageConverters(List<HttpMessageConverter<?>> converters);
//    void configureContentNegotiation(ContentNegotiationConfigurer configurer);
//    void configureAsyncSupport(AsyncSupportConfigurer configurer);
//    void configurePathMatch(PathMatchConfigurer configurer);

    void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers);

    void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers);

//    void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers);
//    void addInterceptors(InterceptorRegistry registry);

    void addViewControllers(ViewControllerRegistry registry);

    /**
     * 将静态资源交给本框架处理，可处理web应用容器根目录、classpath或其他目录下的静态资源
     * Add handlers to serve static resources such as images, js, and, css files from specific locations
     * under web application root, the classpath, and others.
     */
    void addResourceHandlers(ResourceHandlerRegistry registry);

    /**
     * 将未匹配的请求url转发给Web容器处理，由web容器而不是本框架处理静态资源
     * Configure a handler to delegate unhandled requests by forwarding to Servlet container's "default" servlet.
     * A common use case for this is when DispatcherServlet is mapped to "/" thus overriding the
     * Servlet container's default handling of static resources.
     */
    void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer);

//    Validator getValidator();
//    MessageCodesResolver getMessageCodesResolver();

}
