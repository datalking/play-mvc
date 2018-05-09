package com.github.datalking.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 获取普通方法或构造方法的参数名称 接口
 *
 * @author yaoo on 4/19/18
 */
public interface ParameterNameDiscoverer {

    // 获取普通方法参数名
    String[] getParameterNames(Method method);

    // 获取构造方法参数名
    String[] getParameterNames(Constructor<?> ctor);

}
