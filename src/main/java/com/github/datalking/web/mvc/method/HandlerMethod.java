package com.github.datalking.web.mvc.method;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.common.BridgeMethodResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author yaoo on 4/28/18
 */
public class HandlerMethod {

    /**
     * Logger that is available to subclasses
     */
    protected final Logger logger = LoggerFactory.getLogger(HandlerMethod.class);

    private final Object bean;

    private final BeanFactory beanFactory;

    private final Method method;

    private final Method bridgedMethod;

    private final MethodParameter[] parameters;


    public HandlerMethod(Object bean, Method method) {
        Assert.notNull(bean, "Bean is required");
        Assert.notNull(method, "Method is required");
        this.bean = bean;
        this.beanFactory = null;
        this.method = method;
//        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        this.bridgedMethod = method;
        this.parameters = initMethodParameters();
    }

    /**
     * Create an instance from a bean instance, method name, and parameter types.
     *
     * @throws NoSuchMethodException when the method cannot be found
     */
    public HandlerMethod(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Assert.notNull(bean, "Bean is required");
        Assert.notNull(methodName, "Method name is required");
        this.bean = bean;
        this.beanFactory = null;
        this.method = bean.getClass().getMethod(methodName, parameterTypes);
//        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(this.method);
        this.bridgedMethod = this.method;
        this.parameters = initMethodParameters();
    }

    /**
     * Create an instance from a bean name, a method, and a {@code BeanFactory}.
     * The method {@link #createWithResolvedBean()} may be used later to
     * re-create the {@code HandlerMethod} with an initialized the bean.
     */
    public HandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
        Assert.hasText(beanName, "Bean name is required");
        Assert.notNull(beanFactory, "BeanFactory is required");
        Assert.notNull(method, "Method is required");
        Assert.isTrue(beanFactory.containsBean(beanName),
                "BeanFactory [" + beanFactory + "] does not contain bean [" + beanName + "]");
        this.bean = beanName;
        this.beanFactory = beanFactory;
        this.method = method;
        this.bridgedMethod = method;
        this.parameters = initMethodParameters();
    }

    /**
     * Copy constructor for use in sub-classes.
     */
    protected HandlerMethod(HandlerMethod handlerMethod) {
        Assert.notNull(handlerMethod, "HandlerMethod is required");
        this.bean = handlerMethod.bean;
        this.beanFactory = handlerMethod.beanFactory;
        this.method = handlerMethod.method;
        this.bridgedMethod = handlerMethod.bridgedMethod;
        this.parameters = handlerMethod.parameters;
    }

    /**
     * Re-create HandlerMethod with the resolved handler.
     */
    private HandlerMethod(HandlerMethod handlerMethod, Object handler) {
        Assert.notNull(handlerMethod, "HandlerMethod is required");
        Assert.notNull(handler, "Handler object is required");
        this.bean = handler;
        this.beanFactory = handlerMethod.beanFactory;
        this.method = handlerMethod.method;
        this.bridgedMethod = handlerMethod.bridgedMethod;
        this.parameters = handlerMethod.parameters;
    }


    private MethodParameter[] initMethodParameters() {
        int count = this.bridgedMethod.getParameterTypes().length;
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            result[i] = new HandlerMethodParameter(i);
        }
        return result;
    }

    /**
     * Returns the bean for this handler method.
     */
    public Object getBean() {
        return this.bean;
    }

    /**
     * Returns the method for this handler method.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Returns the type of the handler for this handler method.
     * Note that if the bean type is a CGLIB-generated class, the original, user-defined class is returned.
     */
    public Class<?> getBeanType() {
        Class<?> clazz = (this.bean instanceof String ?
                this.beanFactory.getType((String) this.bean) : this.bean.getClass());
        return ClassUtils.getUserClass(clazz);
    }

    /**
     * If the bean method is a bridge method, this method returns the bridged (user-defined) method.
     * Otherwise it returns the same method as {@link #getMethod()}.
     */
    protected Method getBridgedMethod() {
        return this.bridgedMethod;
    }

    /**
     * Returns the method parameters for this handler method.
     */
    public MethodParameter[] getMethodParameters() {
        return this.parameters;
    }

    /**
     * Return the HandlerMethod return type.
     */
    public MethodParameter getReturnType() {
        return new HandlerMethodParameter(-1);
    }

    /**
     * Return the actual return value type.
     */
    public MethodParameter getReturnValueType(Object returnValue) {
        return new ReturnValueMethodParameter(returnValue);
    }

    /**
     * Returns {@code true} if the method return type is void, {@code false} otherwise.
     */
    public boolean isVoid() {
        return Void.TYPE.equals(getReturnType().getParameterType());
    }

    /**
     * Returns a single annotation on the underlying method traversing its super methods if no
     * annotation can be found on the given method itself.
     *
     * @param annotationType the type of annotation to introspect the method for.
     * @return the annotation, or {@code null} if none found
     */
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(this.method, annotationType);
    }

    /**
     * If the provided instance contains a bean name rather than an object instance, the bean name is resolved
     * before a {@link HandlerMethod} is created and returned.
     */
    public HandlerMethod createWithResolvedBean() {
        Object handler = this.bean;
        if (this.bean instanceof String) {
            String beanName = (String) this.bean;
            handler = this.beanFactory.getBean(beanName);
        }
        return new HandlerMethod(this, handler);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof HandlerMethod) {
            HandlerMethod other = (HandlerMethod) obj;
            return (this.bean.equals(other.bean) && this.method.equals(other.method));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.bean.hashCode() * 31 + this.method.hashCode();
    }

    @Override
    public String toString() {
        return this.method.toGenericString();
    }


    /**
     * A MethodParameter with HandlerMethod-specific behavior.
     */
    private class HandlerMethodParameter extends MethodParameter {

        public HandlerMethodParameter(int index) {
            super(HandlerMethod.this.bridgedMethod, index);
        }

        @Override
        public Class<?> getDeclaringClass() {
            return HandlerMethod.this.getBeanType();
        }

        @Override
        public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
            return HandlerMethod.this.getMethodAnnotation(annotationType);
        }
    }


    /**
     * A MethodParameter for a HandlerMethod return type based on an actual return value.
     */
    private class ReturnValueMethodParameter extends HandlerMethodParameter {

        private final Object returnValue;

        public ReturnValueMethodParameter(Object returnValue) {
            super(-1);
            this.returnValue = returnValue;
        }

        @Override
        public Class<?> getParameterType() {
            return (this.returnValue != null ? this.returnValue.getClass() : super.getParameterType());
        }
    }

}
