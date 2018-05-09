package com.github.datalking.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 默认使用的获取方法参数名的类
 *
 * @author yaoo on 4/19/18
 */
public class StandardReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

    @Override
    public String[] getParameterNames(Method method) {

        Parameter[] parameters = method.getParameters();
        String[] parameterNames = new String[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (!param.isNamePresent()) {
                return null;
            }
            parameterNames[i] = param.getName();
        }

        return parameterNames;
    }

    @Override
    public String[] getParameterNames(Constructor<?> ctor) {

        Parameter[] parameters = ctor.getParameters();
        String[] parameterNames = new String[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (!param.isNamePresent()) {
                return null;
            }
            parameterNames[i] = param.getName();
        }

        return parameterNames;
    }

}
