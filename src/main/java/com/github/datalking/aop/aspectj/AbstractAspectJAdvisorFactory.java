package com.github.datalking.aop.aspectj;

import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

/**
 * @author yaoo on 4/18/18
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {

    protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();


    @Override
    public boolean isAspect(Class<?> clazz) {
        Assert.notNull(clazz, "作为切面的类不能为空");
        return clazz.isAnnotationPresent(Aspect.class);
    }


    protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {

        //切面相关的注解类
        Class<?>[] classesToLookFor = new Class<?>[]{
                Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class, Pointcut.class
        };

        for (Class<?> c : classesToLookFor) {

            //获取指定方法上的注解，并使用AspectJAnnotation 封装
            AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) c);

            if (foundAnnotation != null) {
                return foundAnnotation;
            }
        }

        return null;
    }

    private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> toLookFor) {

        A result = AnnotationUtils.findAnnotation(method, toLookFor);

        if (result != null) {
            return new AspectJAnnotation<>(result);
        } else {
            return null;
        }
    }


    enum AspectJAnnotationType {
        AtPointcut,
        AtBefore,
        AtAfter,
        AtAfterReturning,
        AtAfterThrowing,
        AtAround
    }


    private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

        @Override
        public String[] getParameterNames(Method method) {
            if (method.getParameterTypes().length == 0) {
                return new String[0];
            }
            AspectJAnnotation<?> annotation = findAspectJAnnotationOnMethod(method);
            if (annotation == null) {
                return null;
            }
            StringTokenizer strTok = new StringTokenizer(annotation.getArgumentNames(), ",");
            if (strTok.countTokens() > 0) {
                String[] names = new String[strTok.countTokens()];
                for (int i = 0; i < names.length; i++) {
                    names[i] = strTok.nextToken();
                }
                return names;
            } else {
                return null;
            }
        }

        @Override
        public String[] getParameterNames(Constructor<?> ctor) {
            throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
        }
    }


}
