package com.soecode.lyf.config;

import com.github.datalking.annotation.Bean;
import com.github.datalking.annotation.ComponentScan;
import com.github.datalking.annotation.Configuration;
import com.github.datalking.annotation.web.EnableWebMvc;
import com.github.datalking.web.config.DefaultServletHandlerConfigurer;
import com.github.datalking.web.config.WebMvcConfigurerAdapter;
import com.github.datalking.web.mvc.InternalResourceViewResolver;
import com.github.datalking.web.mvc.JstlView;
import com.github.datalking.web.servlet.ViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.soecode.lyf.web"})
//@ComponentScan("com.soecode.lyf.web")
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

}
