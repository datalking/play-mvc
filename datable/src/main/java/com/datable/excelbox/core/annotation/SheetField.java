package com.datable.excelbox.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标注 bean模型类属性 与 excel表中列 的关系的注解
 *
 * @author jinyaoo
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SheetField {
    /**
     * 简单列名
     */
    String title() default "";

    /**
     * 列名，支持嵌套表头
     * 默认会使用模型对象的属性名
     */
    String[] value() default {""};

    /**
     * 列的顺序，从1开始
     * 读取解析时可用可不用，用于生成sheet时指定顺序
     */
    int order() default 99999;

    /**
     * 用来声明列字段的格式，如日期格式
     */
    String format() default "";
}
