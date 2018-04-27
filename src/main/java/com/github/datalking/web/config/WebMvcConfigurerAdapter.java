package com.github.datalking.web.config;

/**
 * 实现WebMvcConfigurer接口的抽象类，子类只需覆盖感兴趣的方法
 *
 * @author yaoo on 4/27/18
 */
public abstract class WebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

}
