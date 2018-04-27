package com.github.datalking.web.config;

import com.github.datalking.annotation.Autowired;
import com.github.datalking.annotation.Configuration;

import java.util.List;

/**
 * @author yaoo on 4/25/18
 */
@Configuration
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {

    private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();


    @Autowired(required = false)
    public void setConfigurers(List<WebMvcConfigurer> configurers) {
        if (configurers == null || configurers.isEmpty()) {
            return;
        }
        this.configurers.addWebMvcConfigurers(configurers);
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        this.configurers.addResourceHandlers(registry);
    }

//    @Override
//    protected void addInterceptors(InterceptorRegistry registry) {
//        this.configurers.addInterceptors(registry);
//    }
//
//    @Override
//    protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//        this.configurers.configureContentNegotiation(configurer);
//    }
//
//    @Override
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//        this.configurers.configureAsyncSupport(configurer);
//    }
//
//    @Override
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//        this.configurers.configurePathMatch(configurer);
//    }
//
//    @Override
//    protected void addViewControllers(ViewControllerRegistry registry) {
//        this.configurers.addViewControllers(registry);
//    }
//
//
//
//    @Override
//    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//        this.configurers.configureDefaultServletHandling(configurer);
//    }
//
//    @Override
//    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        this.configurers.addArgumentResolvers(argumentResolvers);
//    }
//
//    @Override
//    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
//        this.configurers.addReturnValueHandlers(returnValueHandlers);
//    }
//
//    @Override
//    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        this.configurers.configureMessageConverters(converters);
//    }
//
//    @Override
//    protected void addFormatters(FormatterRegistry registry) {
//        this.configurers.addFormatters(registry);
//    }
//
//    @Override
//    protected Validator getValidator() {
//        return this.configurers.getValidator();
//    }
//
//    @Override
//    protected MessageCodesResolver getMessageCodesResolver() {
//        return this.configurers.getMessageCodesResolver();
//    }
//
//    @Override
//    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//        this.configurers.configureHandlerExceptionResolvers(exceptionResolvers);
//    }

}
