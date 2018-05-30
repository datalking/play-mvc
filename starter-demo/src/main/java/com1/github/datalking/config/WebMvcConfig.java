//package com1.github.datalking.config;
//
//import com.github.datalking.annotation.Bean;
//import com.github.datalking.annotation.ComponentScan;
//import com.github.datalking.annotation.Configuration;
//import com.github.datalking.annotation.web.EnableWebMvc;
//import com.github.datalking.web.config.WebMvcConfigurerAdapter;
//import com.github.datalking.web.mvc.InternalResourceViewResolver;
//import com.github.datalking.web.servlet.ViewResolver;
//
//@Configuration
//@EnableWebMvc
//@ComponentScan(basePackages = {"com1.github.datalking"})
//public class WebMvcConfig extends WebMvcConfigurerAdapter {
//
////    @Override
////    public void addResourceHandlers(ResourceHandlerRegistry registry) {
////        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
////    }
//
////    @Override
////    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
////        configurer.enable();
////    }
//
//    @Bean
//    public ViewResolver resolver() {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
////        resolver.setViewClass(JstlView.class);
//        resolver.setPrefix("/WEB-INF/views/");
//        resolver.setSuffix(".jsp");
//        return resolver;
//    }
//
//}
