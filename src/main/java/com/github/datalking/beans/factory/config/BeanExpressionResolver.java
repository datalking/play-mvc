package com.github.datalking.beans.factory.config;

/**
 * @author yaoo on 4/29/18
 */
public interface BeanExpressionResolver {

    Object evaluate(String value, BeanExpressionContext evalContext);

}
