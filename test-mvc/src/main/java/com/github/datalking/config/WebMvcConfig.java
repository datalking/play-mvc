//package com.github.datalking.config;
//
//import com.github.datalking.annotation.Bean;
//import com.github.datalking.annotation.ComponentScan;
//import com.github.datalking.annotation.Configuration;
//import com.github.datalking.annotation.web.EnableWebMvc;
//import com.github.datalking.web.config.WebMvcConfigurer;
//import com.github.datalking.web.view.InternalResourceViewResolver;
//
//@Configuration
//@EnableWebMvc
//@ComponentScan(basePackages = {"com.github.datalking"})
//public class WebMvcConfig implements WebMvcConfigurer {
//
//    //View resolver bean
//    @Bean
//    //public InternalResourceViewResolver resolver() {
//    public ViewResolver resolver() {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//        resolver.setViewClass(JstlView.class);
//        resolver.setPrefix("/WEB-INF/views/");
//        resolver.setSuffix(".jsp");
//        return resolver;
//    }
//
//
//}
