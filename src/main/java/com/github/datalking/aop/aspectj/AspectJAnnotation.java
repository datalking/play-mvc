package com.github.datalking.aop.aspectj;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoo on 4/19/18
 */
public class AspectJAnnotation<A extends Annotation> {

    private static final String[] EXPRESSION_PROPERTIES = new String[]{"value", "pointcut"};

    private static Map<Class<?>, AbstractAspectJAdvisorFactory.AspectJAnnotationType> annotationTypes = new HashMap<>();

    static {
        annotationTypes.put(Pointcut.class, AbstractAspectJAdvisorFactory.AspectJAnnotationType.AtPointcut);
        annotationTypes.put(After.class, AbstractAspectJAdvisorFactory.AspectJAnnotationType.AtAfter);
        annotationTypes.put(AfterReturning.class, AbstractAspectJAdvisorFactory.AspectJAnnotationType.AtAfterReturning);
        annotationTypes.put(AfterThrowing.class, AbstractAspectJAdvisorFactory.AspectJAnnotationType.AtAfterThrowing);
        annotationTypes.put(Around.class, AbstractAspectJAdvisorFactory.AspectJAnnotationType.AtAround);
        annotationTypes.put(Before.class, AbstractAspectJAdvisorFactory.AspectJAnnotationType.AtBefore);
    }

    private final A annotation;

    private final AbstractAspectJAdvisorFactory.AspectJAnnotationType annotationType;

    private final String pointcutExpression;

    private final String argumentNames;

    public AspectJAnnotation(A annotation) {
        this.annotation = annotation;
        this.annotationType = determineAnnotationType(annotation);
        try {
            this.pointcutExpression = resolveExpression(annotation);
            this.argumentNames = (String) annotation.getClass().getMethod("argNames").invoke(annotation);
        } catch (Exception ex) {
            throw new IllegalArgumentException(annotation + " cannot be an AspectJ annotation", ex);
        }
    }

    private AbstractAspectJAdvisorFactory.AspectJAnnotationType determineAnnotationType(A annotation) {
        for (Class<?> type : annotationTypes.keySet()) {
            if (type.isInstance(annotation)) {
                return annotationTypes.get(type);
            }
        }
        throw new IllegalStateException("Unknown annotation type: " + annotation.toString());
    }

    private String resolveExpression(A annotation) throws Exception {
        String expression = null;
        for (String methodName : EXPRESSION_PROPERTIES) {
            Method method;
            try {
                method = annotation.getClass().getDeclaredMethod(methodName);
            } catch (NoSuchMethodException ex) {
                method = null;
            }
            if (method != null) {
                String candidate = (String) method.invoke(annotation);
                if (candidate != null && candidate.trim().length() > 0) {
                    expression = candidate;
                }
            }
        }
        return expression;
    }

    public AbstractAspectJAdvisorFactory.AspectJAnnotationType getAnnotationType() {
        return this.annotationType;
    }

    public A getAnnotation() {
        return this.annotation;
    }

    public String getPointcutExpression() {
        return this.pointcutExpression;
    }

    public String getArgumentNames() {
        return this.argumentNames;
    }

    @Override
    public String toString() {
        return this.annotation.toString();
    }

}
