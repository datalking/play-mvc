package com.github.datalking.web.mvc.method;

import com.github.datalking.annotation.ExceptionHandler;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 5/3/18
 */
public class ExceptionHandlerMethodResolver {

    public static final MethodFilter EXCEPTION_HANDLER_METHODS = new MethodFilter() {
        public boolean matches(Method method) {
            return (AnnotationUtils.findAnnotation(method, ExceptionHandler.class) != null);
        }
    };

    private static final Method NO_METHOD_FOUND = ClassUtils.getMethodIfAvailable(System.class, "currentTimeMillis");

    private final Map<Class<? extends Throwable>, Method> mappedMethods = new ConcurrentHashMap<>(16);

    private final Map<Class<? extends Throwable>, Method> exceptionLookupCache = new ConcurrentHashMap<>(16);

    public ExceptionHandlerMethodResolver(Class<?> handlerType) {
        for (Method method : HandlerMethodSelector.selectMethods(handlerType, EXCEPTION_HANDLER_METHODS)) {
            for (Class<? extends Throwable> exceptionType : detectExceptionMappings(method)) {
                addExceptionMapping(exceptionType, method);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends Throwable>> detectExceptionMappings(Method method) {
        List<Class<? extends Throwable>> result = new ArrayList<Class<? extends Throwable>>();
        detectAnnotationExceptionMappings(method, result);
        if (result.isEmpty()) {
            for (Class<?> paramType : method.getParameterTypes()) {
                if (Throwable.class.isAssignableFrom(paramType)) {
                    result.add((Class<? extends Throwable>) paramType);
                }
            }
        }
        Assert.notEmpty(result.toArray(), "No exception types mapped to {" + method + "}");
        return result;
    }

    protected void detectAnnotationExceptionMappings(Method method, List<Class<? extends Throwable>> result) {
        ExceptionHandler annot = AnnotationUtils.findAnnotation(method, ExceptionHandler.class);
        result.addAll(Arrays.asList(annot.value()));
    }

    private void addExceptionMapping(Class<? extends Throwable> exceptionType, Method method) {
        Method oldMethod = this.mappedMethods.put(exceptionType, method);
        if (oldMethod != null && !oldMethod.equals(method)) {
            throw new IllegalStateException(
                    "Ambiguous @ExceptionHandler method mapped for [" + exceptionType + "]: {" +
                            oldMethod + ", " + method + "}.");
        }
    }

    public boolean hasExceptionMappings() {
        return !this.mappedMethods.isEmpty();
    }

    public Method resolveMethod(Exception exception) {
        return resolveMethodByExceptionType(exception.getClass());
    }

    public Method resolveMethodByExceptionType(Class<? extends Exception> exceptionType) {
        Method method = this.exceptionLookupCache.get(exceptionType);
        if (method == null) {
            method = getMappedMethod(exceptionType);
            this.exceptionLookupCache.put(exceptionType, (method != null ? method : NO_METHOD_FOUND));
        }
        return (method != NO_METHOD_FOUND ? method : null);
    }

    private Method getMappedMethod(Class<? extends Exception> exceptionType) {
        List<Class<? extends Throwable>> matches = new ArrayList<Class<? extends Throwable>>();
        for (Class<? extends Throwable> mappedException : this.mappedMethods.keySet()) {
            if (mappedException.isAssignableFrom(exceptionType)) {
                matches.add(mappedException);
            }
        }
        if (!matches.isEmpty()) {
//            Collections.sort(matches, new ExceptionDepthComparator(exceptionType));
            return this.mappedMethods.get(matches.get(0));
        } else {
            return null;
        }
    }

}
