package com.github.datalking.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author yaoo on 4/19/18
 */
public interface ParameterNameDiscoverer {

    String[] getParameterNames(Method method);

    String[] getParameterNames(Constructor<?> ctor);

}
