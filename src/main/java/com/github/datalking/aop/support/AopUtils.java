package com.github.datalking.aop.support;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.Pointcut;
import com.github.datalking.aop.PointcutAdvisor;
import com.github.datalking.aop.framework.Advised;
import com.github.datalking.aop.framework.AdvisedSupport;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author yaoo on 4/18/18
 */
public interface AopUtils {


    /**
     * 选择可以作用于目标类的advisor
     */
    static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {

        if (candidateAdvisors.isEmpty()) {
            return candidateAdvisors;
        }

        List<Advisor> eligibleAdvisors = new LinkedList<>();

        for (Advisor candidate : candidateAdvisors) {

            // 对普通bean的匹配处理，匹配在canApply中实现
            if (canApply(candidate, clazz)) {
                eligibleAdvisors.add(candidate);
            }
        }

        return eligibleAdvisors;
    }


    static boolean canApply(Advisor advisor, Class<?> targetClass) {

        /// 如果是切入点增强
        if (advisor instanceof PointcutAdvisor) {
            PointcutAdvisor pca = (PointcutAdvisor) advisor;

            //从Advisor中获取Pointcut的实现类 这里是AspectJExpressionPointcut
            return canApply(pca.getPointcut(), targetClass);
        } else {
            // 没有pointcut，默认符合
            return true;
        }
    }


    static boolean canApply(Pointcut pc, Class<?> targetClass) {
        Assert.notNull(pc, "Pointcut must not be null");

        //先进行ClassFilter的matches方法校验，首先这个类要在所匹配的规则下
        if (!pc.getClassFilter().matches(targetClass)) {
            return false;
        }

        //再进行 MethodMatcher 方法级别的校验
        MethodMatcher methodMatcher = pc.getMethodMatcher();
        if (methodMatcher == MethodMatcher.TRUE) {
            return true;
        }

        Set<Class<?>> classes = new LinkedHashSet<>(ClassUtils.getAllInterfacesForClassAsSet(targetClass));
        classes.add(targetClass);

        //只要有一个方法能匹配到就返回true
        //有一个问题：因为在一个目标中可能会有多个方法存在，有的方法是满足这个切点的匹配规则的，也可能有一些方法是不匹配切点规则的，
        //这里检测的是只有一个Method满足切点规则就返回true了，所以在运行时进行方法拦截的时候还会有一次运行时的方法切点规则匹配
        for (Class<?> clazz : classes) {

            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {

                if (methodMatcher.matches(method, targetClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    static Object invokeJoinpointUsingReflection(Object target, Method method, Object[] args) {

        try {
            //ReflectionUtils.makeAccessible(method);
            return method.invoke(target, args);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    static Class<?>[] completeProxiedInterfaces(AdvisedSupport advised) {

        Class<?>[] specifiedInterfaces = advised.getProxiedInterfaces();

        if (specifiedInterfaces.length == 0) {
            // 检查targetClass是否是接口
            Class<?> targetClass = advised.getTargetClass();

            if (targetClass != null) {
                /// 如果是接口
                if (targetClass.isInterface()) {
                    advised.setInterfaces(targetClass);
                }
                /// 如果本身是代理类
                else if (Proxy.isProxyClass(targetClass)) {
                    advised.setInterfaces(targetClass.getInterfaces());
                }

                specifiedInterfaces = advised.getProxiedInterfaces();
            }
        }

        boolean addAdvised = !advised.isInterfaceProxied(Advised.class);
        int nonUserIfcCount = 0;

        if (addAdvised) {
            nonUserIfcCount++;
        }

        Class<?>[] proxiedInterfaces = new Class<?>[specifiedInterfaces.length + nonUserIfcCount];
        System.arraycopy(specifiedInterfaces, 0, proxiedInterfaces, 0, specifiedInterfaces.length);
        int index = specifiedInterfaces.length;

        if (addAdvised) {
            proxiedInterfaces[index] = Advised.class;
            index++;
        }

        return proxiedInterfaces;
    }

    static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // If we are dealing with method with generic parameters, find the original method.
        //return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
        return resolvedMethod;
    }

    static Class<?> getTargetClass(Object candidate) {
        Assert.notNull(candidate, "Candidate object must not be null");
        Class<?> result = null;
//        if (candidate instanceof TargetClassAware) {
//            result = ((TargetClassAware) candidate).getTargetClass();
//        }
//        if (result == null) {
//            result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
//        }
        result = candidate.getClass();
        return result;
    }

}
