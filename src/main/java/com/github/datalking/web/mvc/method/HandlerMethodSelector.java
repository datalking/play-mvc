package com.github.datalking.web.mvc.method;

import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author yaoo on 4/28/18
 */
public abstract class HandlerMethodSelector {


    public static Set<Method> selectMethods(final Class<?> handlerType, final MethodFilter handlerMethodFilter) {
        final Set<Method> handlerMethods = new LinkedHashSet<>();
        Set<Class<?>> handlerTypes = new LinkedHashSet<>();
        Class<?> specificHandlerType = null;
        if (!Proxy.isProxyClass(handlerType)) {
            handlerTypes.add(handlerType);
            specificHandlerType = handlerType;
        }
        handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
        for (Class<?> currentHandlerType : handlerTypes) {
            final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);
            ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
//                    Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                    Method bridgedMethod = specificMethod;
                    if (handlerMethodFilter.matches(specificMethod) &&
                            (bridgedMethod == specificMethod || !handlerMethodFilter.matches(bridgedMethod))) {
                        handlerMethods.add(specificMethod);
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
        return handlerMethods;
    }

}
