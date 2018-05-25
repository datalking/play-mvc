package com.github.datalking.web.mvc.condition;

/**
 * @author yaoo on 4/28/18
 */
public interface NameValueExpression<T> {

    String getName();

    T getValue();

    boolean isNegated();

}

