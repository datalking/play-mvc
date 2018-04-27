package com.github.datalking.annotation;

import com.github.datalking.web.config.DelegatingWebMvcConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用基于注解来使用mvc
 *
 * @author yaoo on 4/23/18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DelegatingWebMvcConfiguration.class)
public @interface EnableWebMvc {

}
