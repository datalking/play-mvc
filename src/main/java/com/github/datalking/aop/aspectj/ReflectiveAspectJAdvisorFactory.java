package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.aspectj.jadvice.AbstractAspectJAdvice;
import com.github.datalking.aop.aspectj.jadvice.AspectJAfterAdvice;
import com.github.datalking.aop.aspectj.jadvice.AspectJAfterReturningAdvice;
import com.github.datalking.aop.aspectj.jadvice.AspectJAroundAdvice;
import com.github.datalking.aop.aspectj.jadvice.AspectJMethodBeforeAdvice;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.util.StringUtils;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @author yaoo on 4/18/18
 */
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory implements Serializable {

    private final BeanFactory beanFactory;

    public ReflectiveAspectJAdvisorFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public ReflectiveAspectJAdvisorFactory() {
        this(null);
    }


    @Override
    public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {

        //获取标记为Aspect的类
        Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        //获取标记为Aspect的name
        String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();

        // 封装MetadataAwareAspectInstanceFactory使之成为单例
        MetadataAwareAspectInstanceFactory lazySingletonAspectFactory = new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);
        //声明为pointcut的方法不处理
        List<Advisor> advisors = new LinkedList<>();
        List<Method> ms= getAdvisorMethods(aspectClass);
        for (Method method : ms) {

            //==== 普通增强器的获取
            Advisor advisor = getAdvisor(method, lazySingletonAspectFactory, advisors.size(), aspectName);
            if (advisor != null) {
                advisors.add(advisor);
            }
        }

        return advisors;
    }


    private List<Method> getAdvisorMethods(Class<?> aspectClass) {
        final List<Method> methods = new LinkedList<>();

        ReflectionUtils.doWithMethods(
                aspectClass,
                new ReflectionUtils.MethodCallback() {

                    @Override
                    public void doWith(Method method) throws IllegalArgumentException {

                        // Exclude pointcuts
                        if (AnnotationUtils.getAnnotation(method, Pointcut.class) == null) {
                            methods.add(method);
                        }
                    }
                },
                null);

        return methods;
    }


    /**
     * 获取普通增强器
     */
    @Override
    public Advisor getAdvisor(Method candidateAdviceMethod,
                              MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                              int declarationOrderInAspect,
                              String aspectName) {


        //切点信息的获取，就是切入点表达式信息的获取
        AspectJExpressionPointcut expressionPointcut = getPointcut(
                candidateAdviceMethod,
                aspectInstanceFactory.getAspectMetadata().getAspectClass());

        if (expressionPointcut == null) {
            return null;
        }

        //根据切点信息生成增强器，所有增强实现的入口
        return new InstantiationModelAwarePointcutAdvisorImpl(
                expressionPointcut,
                candidateAdviceMethod,
                this,
                aspectInstanceFactory,
                declarationOrderInAspect,
                aspectName);
    }

    private AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod, Class<?> candidateAspectClass) {

        //获取方法上的注解，为了提取切入点表达式
        AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }

        //使用AspectJExcepressionPointcut 实例封装获取的信息
        AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);

        //提取得到的注解中的表达式如：@Pointcut("execution(* *.*test*(..))")中的execution(* *.*test*(..))
        String exp = aspectJAnnotation.getPointcutExpression();
        ajexp.setExpression(exp);
        ajexp.setBeanFactory(this.beanFactory);

        return ajexp;
    }


    /**
     * 实际创建Advice的方法
     */
    @Override
    public Advice getAdvice(Method candidateAdviceMethod,
                            AspectJExpressionPointcut expressionPointcut,
                            MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                            int declarationOrder,
                            String aspectName) {

        Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();

        AspectJAnnotation<?> aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }

        if (!isAspect(candidateAspectClass)) {
            try {
                throw new Exception("Advice must be declared inside an aspect type:  Offending method '" + candidateAdviceMethod + "' in class [" +
                        candidateAspectClass.getName() + "]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AbstractAspectJAdvice jAdvice;

        //根据不同的注解类封装增强器
        switch (aspectJAnnotation.getAnnotationType()) {
            case AtBefore:
                jAdvice = new AspectJMethodBeforeAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtAfter:
                jAdvice = new AspectJAfterAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtAfterReturning:
                jAdvice = new AspectJAfterReturningAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
                if (StringUtils.hasText(afterReturningAnnotation.returning())) {
                    jAdvice.setReturningName(afterReturningAnnotation.returning());
                }
                break;
//            case AtAfterThrowing:
//                springAdvice = new AspectJAfterThrowingAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
//                AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
//                if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
//                    springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
//                }
//                break;
            case AtAround:
                jAdvice = new AspectJAroundAdvice(candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtPointcut:
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Processing pointcut '" + candidateAdviceMethod.getName() + "'");
//                }
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported advice type on method: " + candidateAdviceMethod);
        }

        jAdvice.setAspectName(aspectName);
        jAdvice.setDeclarationOrder(declarationOrder);
        String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAdviceMethod);
        if (argNames != null) {
            jAdvice.setArgumentNamesFromStringArray(argNames);
        }
        //springAdvice.calculateArgumentBindings();

        return jAdvice;
    }



}
