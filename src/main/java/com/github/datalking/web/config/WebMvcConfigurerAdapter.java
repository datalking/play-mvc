package com.github.datalking.web.config;

import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;

import java.util.List;

/**
 * WebMvcConfigurer接口实现类的抽象类
 * 都是空方法，方便子类只重写感兴趣的方法
 *
 * @author yaoo on 4/27/18
 */
public abstract class WebMvcConfigurerAdapter implements WebMvcConfigurer {

//    public void addFormatters(FormatterRegistry registry) {
//    }
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//    }
//    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//    }
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//    }
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//    }
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }
//    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//    }
//    public void addInterceptors(InterceptorRegistry registry) {
//    }
    public void addViewControllers(ViewControllerRegistry registry) {
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }
//    public Validator getValidator() {
//        return null;
//    }
//    public MessageCodesResolver getMessageCodesResolver() {
//        return null;
//    }

}
