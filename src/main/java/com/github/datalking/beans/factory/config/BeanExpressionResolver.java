package com.github.datalking.beans.factory.config;

/**
 * SPEL 语言解析器相关
 *
 * @author yaoo on 4/29/18
 */
public interface BeanExpressionResolver {

    Object evaluate(String value, BeanExpressionContext evalContext);

}
