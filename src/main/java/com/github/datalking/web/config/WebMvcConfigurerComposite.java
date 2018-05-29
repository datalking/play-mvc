package com.github.datalking.web.config;

import com.github.datalking.util.CollectionUtils;
import com.github.datalking.web.support.HandlerMethodArgumentResolver;
import com.github.datalking.web.support.HandlerMethodReturnValueHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yaoo on 4/27/18
 */
public class WebMvcConfigurerComposite implements WebMvcConfigurer {

    private final List<WebMvcConfigurer> delegates = new ArrayList<>();

    public void addWebMvcConfigurers(List<WebMvcConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.delegates.addAll(configurers);
        }
    }


//    public void addFormatters(FormatterRegistry registry) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.addFormatters(registry);
//        }
//    }
//
//    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.configureContentNegotiation(configurer);
//        }
//    }
//
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.configureAsyncSupport(configurer);
//        }
//    }
//
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.configurePathMatch(configurer);
//        }
//    }
//
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.configureMessageConverters(converters);
//        }
//    }
//
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        for (WebMvcConfigurer delegate : this.delegates) {
            delegate.addArgumentResolvers(argumentResolvers);
        }
    }

    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        for (WebMvcConfigurer delegate : this.delegates) {
            delegate.addReturnValueHandlers(returnValueHandlers);
        }
    }
//
//    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.configureHandlerExceptionResolvers(exceptionResolvers);
//        }
//    }
//
//    public void addInterceptors(InterceptorRegistry registry) {
//        for (WebMvcConfigurer delegate : this.delegates) {
//            delegate.addInterceptors(registry);
//        }
//    }
//
    public void addViewControllers(ViewControllerRegistry registry) {
        for (WebMvcConfigurer delegate : this.delegates) {
            delegate.addViewControllers(registry);
        }
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (WebMvcConfigurer delegate : this.delegates) {
            delegate.addResourceHandlers(registry);
        }
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {

        for (WebMvcConfigurer delegate : this.delegates) {
            delegate.configureDefaultServletHandling(configurer);
        }

    }
//
//    public Validator getValidator() {
//        List<Validator> candidates = new ArrayList<Validator>();
//        for (WebMvcConfigurer configurer : this.delegates) {
//            Validator validator = configurer.getValidator();
//            if (validator != null) {
//                candidates.add(validator);
//            }
//        }
//        return selectSingleInstance(candidates, Validator.class);
//    }
//
//    public MessageCodesResolver getMessageCodesResolver() {
//        List<MessageCodesResolver> candidates = new ArrayList<MessageCodesResolver>();
//        for (WebMvcConfigurer configurer : this.delegates) {
//            MessageCodesResolver messageCodesResolver = configurer.getMessageCodesResolver();
//            if (messageCodesResolver != null) {
//                candidates.add(messageCodesResolver);
//            }
//        }
//        return selectSingleInstance(candidates, MessageCodesResolver.class);
//    }

    private <T> T selectSingleInstance(List<T> instances, Class<T> instanceType) {

        if (instances.size() > 1) {

            throw new IllegalStateException("Only one [" + instanceType + "] was expected but multiple instances were provided: " + instances);
        } else if (instances.size() == 1) {

            return instances.get(0);
        } else {

            return null;
        }
    }

}
