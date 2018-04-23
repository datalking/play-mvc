package com.github.datalking.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author yaoo on 4/18/18
 */
public interface AdvisorChainFactory {

    List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass);

}
