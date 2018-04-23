package com.github.datalking.aop.framework;

import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.Pointcut;
import com.github.datalking.aop.support.ComposablePointcut;
import com.github.datalking.aop.support.StaticMethodMatcherPointcut;
import com.github.datalking.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Pointcut的工具类
 *
 * @author yaoo on 4/19/18
 */
public abstract class Pointcuts {

    public static final Pointcut SETTERS = SetterPointcut.INSTANCE;


    public static final Pointcut GETTERS = GetterPointcut.INSTANCE;

    public static Pointcut union(Pointcut pc1, Pointcut pc2) {
        return new ComposablePointcut(pc1).union(pc2);
    }

    public static Pointcut intersection(Pointcut pc1, Pointcut pc2) {
        return new ComposablePointcut(pc1).intersection(pc2);
    }


    public static boolean matches(Pointcut pointcut, Method method, Class<?> targetClass, Object... args) {
        Assert.notNull(pointcut, "Pointcut must not be null");
        if (pointcut == Pointcut.TRUE) {
            return true;
        }
        if (pointcut.getClassFilter().matches(targetClass)) {
            // Only check if it gets past first hurdle.
            MethodMatcher mm = pointcut.getMethodMatcher();
            if (mm.matches(method, targetClass)) {
                // We may need additional runtime (argument) check.
                return (!mm.isRuntime() || mm.matches(method, targetClass, args));
            }
        }
        return false;
    }


    /**
     * 匹配bean属性设置器的Pointcut
     */
    private static class SetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

        public static SetterPointcut INSTANCE = new SetterPointcut();

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (method.getName().startsWith("set") &&
                    method.getParameterTypes().length == 1 &&
                    method.getReturnType() == Void.TYPE);
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }


    /**
     * 匹配bean属性读取器的Pointcut
     */
    private static class GetterPointcut extends StaticMethodMatcherPointcut implements Serializable {

        public static GetterPointcut INSTANCE = new GetterPointcut();

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (method.getName().startsWith("get") &&
                    method.getParameterTypes().length == 0);
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }


}
