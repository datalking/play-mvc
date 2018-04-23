package com.github.datalking.aop;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author yaoo on 4/19/18
 */
public interface ProxyMethodInvocation extends MethodInvocation {

    Object getProxy();

    MethodInvocation invocableClone();

    MethodInvocation invocableClone(Object... arguments);

    void setArguments(Object... arguments);

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);

}
