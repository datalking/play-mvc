package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Pointcut;
import com.github.datalking.aop.framework.Pointcuts;
import com.github.datalking.aop.support.DynamicMethodMatcherPointcut;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.reflect.PerClauseKind;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author yaoo on 4/19/18
 */
public class InstantiationModelAwarePointcutAdvisorImpl
        implements InstantiationModelAwarePointcutAdvisor, AspectJPrecedenceInformation, Serializable {

    private final AspectJExpressionPointcut declaredPointcut;

    private final Class<?> declaringClass;

    private final String methodName;

    private final Class<?>[] parameterTypes;

    private transient Method aspectJAdviceMethod;

    private final AspectJAdvisorFactory aspectJAdvisorFactory;

    private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;

    private final int declarationOrder;

    private final String aspectName;

    private final Pointcut pointcut;

    private final boolean lazy;

    private Advice instantiatedAdvice;

    private Boolean isBeforeAdvice;

    private Boolean isAfterAdvice;


    public InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut,
                                                      Method aspectJAdviceMethod,
                                                      AspectJAdvisorFactory aspectJAdvisorFactory,
                                                      MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                                                      int declarationOrder, String aspectName) {

        this.declaredPointcut = declaredPointcut;
        this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
        this.methodName = aspectJAdviceMethod.getName();
        this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.aspectJAdvisorFactory = aspectJAdvisorFactory;
        this.aspectInstanceFactory = aspectInstanceFactory;
        this.declarationOrder = declarationOrder;
        this.aspectName = aspectName;

        if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {

            //  pointcut的static 部分懒加载
            Pointcut preInstantiationPointcut = Pointcuts.union(
                    aspectInstanceFactory.getAspectMetadata().getPerClausePointcut(),
                    this.declaredPointcut);

            this.pointcut = new PerTargetInstantiationModelPointcut(
                    this.declaredPointcut,
                    preInstantiationPointcut,
                    aspectInstanceFactory);

            this.lazy = true;
        } else {
            // A singleton aspect.
            this.pointcut = this.declaredPointcut;
            this.lazy = false;

            // 根据注解中的信息初始化对应的增强器
            this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
        }
    }


    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    //@Override
    public boolean isPerInstance() {
        return (getAspectMetadata().getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON);
    }

    public AspectMetadata getAspectMetadata() {
        return this.aspectInstanceFactory.getAspectMetadata();
    }

    /**
     * 延迟初始化advice
     */
    @Override
    public synchronized Advice getAdvice() {
        if (this.instantiatedAdvice == null) {
            this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
        }
        return this.instantiatedAdvice;
    }

    @Override
    public boolean isLazy() {
        return this.lazy;
    }

    @Override
    public synchronized boolean isAdviceInstantiated() {
        return (this.instantiatedAdvice != null);
    }


    private Advice instantiateAdvice(AspectJExpressionPointcut pcut) {

        return this.aspectJAdvisorFactory.getAdvice(
                this.aspectJAdviceMethod,
                pcut,
                this.aspectInstanceFactory,
                this.declarationOrder,
                this.aspectName);
    }

    public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
        return this.aspectInstanceFactory;
    }

    public AspectJExpressionPointcut getDeclaredPointcut() {
        return this.declaredPointcut;
    }

    //@Override
    public int getOrder() {
        return this.aspectInstanceFactory.getOrder();
    }

    @Override
    public String getAspectName() {
        return this.aspectName;
    }

    @Override
    public int getDeclarationOrder() {
        return this.declarationOrder;
    }

    @Override
    public boolean isBeforeAdvice() {
        if (this.isBeforeAdvice == null) {
            determineAdviceType();
        }
        return this.isBeforeAdvice;
    }

    @Override
    public boolean isAfterAdvice() {
        if (this.isAfterAdvice == null) {
            determineAdviceType();
        }
        return this.isAfterAdvice;
    }


    private void determineAdviceType() {

        AspectJAnnotation<?> aspectJAnnotation =
                AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(this.aspectJAdviceMethod);

        if (aspectJAnnotation == null) {
            this.isBeforeAdvice = false;
            this.isAfterAdvice = false;
        } else {
            switch (aspectJAnnotation.getAnnotationType()) {
                case AtAfter:
                case AtAfterReturning:
                case AtAfterThrowing:
                    this.isAfterAdvice = true;
                    this.isBeforeAdvice = false;
                    break;
                case AtAround:
                case AtPointcut:
                    this.isAfterAdvice = false;
                    this.isBeforeAdvice = false;
                    break;
                case AtBefore:
                    this.isAfterAdvice = false;
                    this.isBeforeAdvice = true;
            }
        }
    }

    @Override
    public String toString() {
        return "InstantiationModelAwarePointcutAdvisor: expression [" + getDeclaredPointcut().getExpression() +
                "]; advice method [" + this.aspectJAdviceMethod + "]; perClauseKind=" +
                this.aspectInstanceFactory.getAspectMetadata().getAjType().getPerClause().getKind();

    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        try {
            this.aspectJAdviceMethod = this.declaringClass.getMethod(this.methodName, this.parameterTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Failed to find advice method on deserialization", ex);
        }
    }


    /**
     * advice实例化时会改变状态的Pointcut
     */
    private class PerTargetInstantiationModelPointcut extends DynamicMethodMatcherPointcut {

        private final AspectJExpressionPointcut declaredPointcut;

        private final Pointcut preInstantiationPointcut;

        private LazySingletonAspectInstanceFactoryDecorator aspectInstanceFactory;

        private PerTargetInstantiationModelPointcut(AspectJExpressionPointcut declaredPointcut,
                                                    Pointcut preInstantiationPointcut,
                                                    MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
            this.declaredPointcut = declaredPointcut;
            this.preInstantiationPointcut = preInstantiationPointcut;
            if (aspectInstanceFactory instanceof LazySingletonAspectInstanceFactoryDecorator) {
                this.aspectInstanceFactory = (LazySingletonAspectInstanceFactoryDecorator) aspectInstanceFactory;
            }
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass)) ||
                    this.preInstantiationPointcut.getMethodMatcher().matches(method, targetClass);
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass, Object... args) {
            return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass));
        }

        private boolean isAspectMaterialized() {
            return (this.aspectInstanceFactory == null || this.aspectInstanceFactory.isMaterialized());
        }
    }


}
