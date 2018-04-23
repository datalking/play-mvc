package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Advisor;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author yaoo on 4/18/18
 */
public interface AspectJAdvisorFactory {

    boolean isAspect(Class<?> clazz);

    //    void validate(Class<?> aspectClass) ;

    List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

    Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

    Advice getAdvice(Method candidateAdviceMethod,
                     AspectJExpressionPointcut expressionPointcut,
                     MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                     int declarationOrder,
                     String aspectName);

}
