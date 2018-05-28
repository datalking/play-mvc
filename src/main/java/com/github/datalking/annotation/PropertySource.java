package com.github.datalking.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yaoo on 5/28/18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertySource {

    String name() default "";

    /**
     * 指定要加载的资源的位置
     * Indicate the resource location(s) of the properties file to be loaded.
     * For example, {@code "classpath:/com/myco/app.properties"} or {@code "file:/path/to/file"}.
     * Resource location wildcards (e.g. *&#42;/*.properties) are not permitted;
     * each location must evaluate to exactly one {@code .properties} resource.
     * ${...} placeholders will be resolved against any/all property sources already registered with the {@code Environment}.
     * Each location will be added to the enclosing {@code Environment} as its own property source, and in the order declared.
     */
    String[] value();

}
