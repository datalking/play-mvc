package com.github.datalking.aop.aspectj.jadvice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.Pointcut;
import com.github.datalking.aop.ProxyMethodInvocation;
import com.github.datalking.aop.aspectj.AspectInstanceFactory;
import com.github.datalking.aop.aspectj.AspectJAdviceParameterNameDiscoverer;
import com.github.datalking.aop.aspectj.AspectJExpressionPointcut;
import com.github.datalking.aop.aspectj.AspectJPrecedenceInformation;
import com.github.datalking.aop.aspectj.MethodInvocationProceedingJoinPoint;
import com.github.datalking.aop.interceptor.ExposeInvocationInterceptor;
import com.github.datalking.aop.support.ComposablePointcut;
import com.github.datalking.aop.support.MethodMatchers;
import com.github.datalking.aop.support.StaticMethodMatcher;
import com.github.datalking.common.DefaultParameterNameDiscoverer;
import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;

/**
 * AOP Alliance Advice 抽象类
 */
public abstract class AbstractAspectJAdvice implements Advice, AspectJPrecedenceInformation, Serializable {


    protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();


    public static JoinPoint currentJoinPoint() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        if (!(mi instanceof ProxyMethodInvocation)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
        JoinPoint jp = (JoinPoint) pmi.getUserAttribute(JOIN_POINT_KEY);
        if (jp == null) {
            jp = new MethodInvocationProceedingJoinPoint(pmi);
            pmi.setUserAttribute(JOIN_POINT_KEY, jp);
        }
        return jp;
    }

    private final Class<?> declaringClass;

    private final String methodName;

    private final Class<?>[] parameterTypes;

    protected transient Method aspectJAdviceMethod;

    private final AspectJExpressionPointcut pointcut;

    private final AspectInstanceFactory aspectInstanceFactory;

    // aspect bean名称，用来区分advice是否来自同一个aspect
    private String aspectName;

    // aspect中advice的声明顺序
    private int declarationOrder;

    private String[] argumentNames = null;

    private String returningName = null;

    private Class<?> discoveredReturningType = Object.class;

    private String throwingName = null;

    private Class<?> discoveredThrowingType = Object.class;

    private Type discoveredReturningGenericType;

    private int joinPointArgumentIndex = -1;

    private int joinPointStaticPartArgumentIndex = -1;

    private Map<String, Integer> argumentBindings;

    private boolean argumentsIntrospected = false;

    public AbstractAspectJAdvice(Method aspectJAdviceMethod,
                                 AspectJExpressionPointcut pointcut,
                                 AspectInstanceFactory aspectInstanceFactory) {

        Assert.notNull(aspectJAdviceMethod, "Advice method must not be null");
        this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
        this.methodName = aspectJAdviceMethod.getName();
        this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.pointcut = pointcut;
        this.aspectInstanceFactory = aspectInstanceFactory;
    }


    public final Method getAspectJAdviceMethod() {
        return this.aspectJAdviceMethod;
    }

    public final AspectJExpressionPointcut getPointcut() {
        calculateArgumentBindings();
        return this.pointcut;
    }

    public final Pointcut buildSafePointcut() {
        Pointcut pc = getPointcut();
        MethodMatcher safeMethodMatcher = MethodMatchers.intersection(new AdviceExcludingMethodMatcher(this.aspectJAdviceMethod), pc.getMethodMatcher());
        return new ComposablePointcut(pc.getClassFilter(), safeMethodMatcher);
    }


    public final AspectInstanceFactory getAspectInstanceFactory() {
        return this.aspectInstanceFactory;
    }


//    public final ClassLoader getAspectClassLoader() {
//        return this.aspectInstanceFactory.getAspectClassLoader();
//    }

    //@Override
    public int getOrder() {
        return this.aspectInstanceFactory.getOrder();
    }


    public void setAspectName(String name) {
        this.aspectName = name;
    }

    @Override
    public String getAspectName() {
        return this.aspectName;
    }

    public void setDeclarationOrder(int order) {
        this.declarationOrder = order;
    }

    @Override
    public int getDeclarationOrder() {
        return this.declarationOrder;
    }


    public void setArgumentNames(String argNames) {
        String[] tokens = StringUtils.commaDelimitedListToStringArray(argNames);
        setArgumentNamesFromStringArray(tokens);
    }

    public void setArgumentNamesFromStringArray(String... args) {
        this.argumentNames = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            this.argumentNames[i] = StringUtils.trimWhitespace(args[i]);
            if (!isVariableName(this.argumentNames[i])) {
                throw new IllegalArgumentException("'argumentNames' property of AbstractAspectJAdvice contains an argument name '" +
                        this.argumentNames[i] + "' that is not a valid Java identifier");
            }
        }
        if (this.argumentNames != null) {
            if (this.aspectJAdviceMethod.getParameterTypes().length == this.argumentNames.length + 1) {
                // May need to add implicit join point arg name...
                Class<?> firstArgType = this.aspectJAdviceMethod.getParameterTypes()[0];
                if (firstArgType == JoinPoint.class ||
                        firstArgType == ProceedingJoinPoint.class ||
                        firstArgType == JoinPoint.StaticPart.class) {
                    String[] oldNames = this.argumentNames;
                    this.argumentNames = new String[oldNames.length + 1];
                    this.argumentNames[0] = "THIS_JOIN_POINT";
                    System.arraycopy(oldNames, 0, this.argumentNames, 1, oldNames.length);
                }
            }
        }
    }

    public void setReturningName(String name) {
        throw new UnsupportedOperationException("Only afterReturning advice can be used to bind a return value");
    }


    protected void setReturningNameNoCheck(String name) {
        // name could be a variable or a type...
        if (isVariableName(name)) {
            this.returningName = name;
        } else {
            // assume a type
            try {
                this.discoveredReturningType = Class.forName(name);
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Returning name '" + name +
                        "' is neither a valid argument name nor the fully-qualified name of a Java type on the classpath. " +
                        "Root cause: " + ex);
            }
        }
    }

    protected Class<?> getDiscoveredReturningType() {
        return this.discoveredReturningType;
    }

    protected Type getDiscoveredReturningGenericType() {
        return this.discoveredReturningGenericType;
    }

    public void setThrowingName(String name) {
        throw new UnsupportedOperationException("Only afterThrowing advice can be used to bind a thrown exception");
    }

    protected void setThrowingNameNoCheck(String name) {
        // name could be a variable or a type...
        if (isVariableName(name)) {
            this.throwingName = name;
        } else {
            // assume a type
            try {
                this.discoveredReturningType = Class.forName(name);
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Throwing name '" + name +
                        "' is neither a valid argument name nor the fully-qualified name of a Java type on the classpath. " +
                        "Root cause: " + ex);
            }
        }
    }

    protected Class<?> getDiscoveredThrowingType() {
        return this.discoveredThrowingType;
    }

    private boolean isVariableName(String name) {
        char[] chars = name.toCharArray();
        if (!Character.isJavaIdentifierStart(chars[0])) {
            return false;
        }
        for (int i = 1; i < chars.length; i++) {
            if (!Character.isJavaIdentifierPart(chars[i])) {
                return false;
            }
        }
        return true;
    }


    public synchronized final void calculateArgumentBindings() {
        // The simple case... nothing to bind.
        if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
            return;
        }

        int numUnboundArgs = this.parameterTypes.length;
        Class<?>[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
        if (maybeBindJoinPoint(parameterTypes[0]) || maybeBindProceedingJoinPoint(parameterTypes[0])) {
            numUnboundArgs--;
        } else if (maybeBindJoinPointStaticPart(parameterTypes[0])) {
            numUnboundArgs--;
        }

        if (numUnboundArgs > 0) {
            // need to bind arguments by name as returned from the pointcut match
            bindArgumentsByName(numUnboundArgs);
        }

        this.argumentsIntrospected = true;
    }

    private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
        if (JoinPoint.class == candidateParameterType) {
            this.joinPointArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
        if (ProceedingJoinPoint.class == candidateParameterType) {
            if (!supportsProceedingJoinPoint()) {
                throw new IllegalArgumentException("ProceedingJoinPoint is only supported for around advice");
            }
            this.joinPointArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    protected boolean supportsProceedingJoinPoint() {
        return false;
    }

    private boolean maybeBindJoinPointStaticPart(Class<?> candidateParameterType) {
        if (JoinPoint.StaticPart.class == candidateParameterType) {
            this.joinPointStaticPartArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private void bindArgumentsByName(int numArgumentsExpectingToBind) {
        if (this.argumentNames == null) {
            this.argumentNames = createParameterNameDiscoverer().getParameterNames(this.aspectJAdviceMethod);
        }
        if (this.argumentNames != null) {
            // We have been able to determine the arg names.
            bindExplicitArguments(numArgumentsExpectingToBind);
        } else {
            throw new IllegalStateException("Advice method [" + this.aspectJAdviceMethod.getName() + "] " +
                    "requires " + numArgumentsExpectingToBind + " arguments to be bound by name, but " +
                    "the argument names were not specified and could not be discovered.");
        }
    }

    protected ParameterNameDiscoverer createParameterNameDiscoverer() {
        // We need to discover them, or if that fails, guess, and if we can't guess with 100% accuracy, fail.
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer = new AspectJAdviceParameterNameDiscoverer(this.pointcut.getExpression());
        adviceParameterNameDiscoverer.setReturningName(this.returningName);
        adviceParameterNameDiscoverer.setThrowingName(this.throwingName);
        // Last in chain, so if we're called and we fail, that's bad...
        adviceParameterNameDiscoverer.setRaiseExceptions(true);
        discoverer.addDiscoverer(adviceParameterNameDiscoverer);
        return discoverer;
    }

    private void bindExplicitArguments(int numArgumentsLeftToBind) {
        this.argumentBindings = new HashMap<>();

        int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterTypes().length;
        if (this.argumentNames.length != numExpectedArgumentNames) {
            throw new IllegalStateException("Expecting to find " + numExpectedArgumentNames +
                    " arguments to bind by name in advice, but actually found " +
                    this.argumentNames.length + " arguments.");
        }

        // So we match in number...
        int argumentIndexOffset = this.parameterTypes.length - numArgumentsLeftToBind;
        for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
            this.argumentBindings.put(this.argumentNames[i], i);
        }

        // Check that returning and throwing were in the argument names list if specified, and find the discovered argument types.
        if (this.returningName != null) {
            if (!this.argumentBindings.containsKey(this.returningName)) {
                throw new IllegalStateException("Returning argument name '" + this.returningName +
                        "' was not bound in advice arguments");
            } else {
                Integer index = this.argumentBindings.get(this.returningName);
                this.discoveredReturningType = this.aspectJAdviceMethod.getParameterTypes()[index];
                this.discoveredReturningGenericType = this.aspectJAdviceMethod.getGenericParameterTypes()[index];
            }
        }
        if (this.throwingName != null) {
            if (!this.argumentBindings.containsKey(this.throwingName)) {
                throw new IllegalStateException("Throwing argument name '" + this.throwingName +
                        "' was not bound in advice arguments");
            } else {
                Integer index = this.argumentBindings.get(this.throwingName);
                this.discoveredThrowingType = this.aspectJAdviceMethod.getParameterTypes()[index];
            }
        }

        // configure the pointcut expression accordingly.
        configurePointcutParameters(argumentIndexOffset);
    }

    private void configurePointcutParameters(int argumentIndexOffset) {
        int numParametersToRemove = argumentIndexOffset;
        if (this.returningName != null) {
            numParametersToRemove++;
        }
        if (this.throwingName != null) {
            numParametersToRemove++;
        }
        String[] pointcutParameterNames = new String[this.argumentNames.length - numParametersToRemove];
        Class<?>[] pointcutParameterTypes = new Class<?>[pointcutParameterNames.length];
        Class<?>[] methodParameterTypes = this.aspectJAdviceMethod.getParameterTypes();

        int index = 0;
        for (int i = 0; i < this.argumentNames.length; i++) {
            if (i < argumentIndexOffset) {
                continue;
            }
            if (this.argumentNames[i].equals(this.returningName) ||
                    this.argumentNames[i].equals(this.throwingName)) {
                continue;
            }
            pointcutParameterNames[index] = this.argumentNames[i];
            pointcutParameterTypes[index] = methodParameterTypes[i];
            index++;
        }

        this.pointcut.setParameterNames(pointcutParameterNames);
        this.pointcut.setParameterTypes(pointcutParameterTypes);
    }


    protected Object[] argBinding(JoinPoint jp, JoinPointMatch jpMatch, Object returnValue, Throwable ex) {
        calculateArgumentBindings();

        // AMC start
        Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
        int numBound = 0;

        if (this.joinPointArgumentIndex != -1) {
            adviceInvocationArgs[this.joinPointArgumentIndex] = jp;
            numBound++;
        } else if (this.joinPointStaticPartArgumentIndex != -1) {
            adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = jp.getStaticPart();
            numBound++;
        }

        if (this.argumentBindings != null && !this.argumentBindings.isEmpty()) {
            // binding from pointcut match
            if (jpMatch != null) {
                PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
                for (PointcutParameter parameter : parameterBindings) {
                    String name = parameter.getName();
                    Integer index = this.argumentBindings.get(name);
                    adviceInvocationArgs[index] = parameter.getBinding();
                    numBound++;
                }
            }
            // binding from returning clause
            if (this.returningName != null) {
                Integer index = this.argumentBindings.get(this.returningName);
                adviceInvocationArgs[index] = returnValue;
                numBound++;
            }
            // binding from thrown exception
            if (this.throwingName != null) {
                Integer index = this.argumentBindings.get(this.throwingName);
                adviceInvocationArgs[index] = ex;
                numBound++;
            }
        }

        if (numBound != this.parameterTypes.length) {
            throw new IllegalStateException("Required to bind " + this.parameterTypes.length +
                    " arguments, but only bound " + numBound + " (JoinPointMatch " +
                    (jpMatch == null ? "was NOT" : "WAS") + " bound in invocation)");
        }

        return adviceInvocationArgs;
    }


    protected Object invokeAdviceMethod(JoinPointMatch jpMatch, Object returnValue, Throwable ex) throws Throwable {
        return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
    }

    // As above, but in this case we are given the join point.
    protected Object invokeAdviceMethod(JoinPoint jp, JoinPointMatch jpMatch, Object returnValue, Throwable t) throws Throwable {

        return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
    }

    protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
        Object[] actualArgs = args;
        if (this.aspectJAdviceMethod.getParameterTypes().length == 0) {
            actualArgs = null;
        }
        try {
            //ReflectionUtils.makeAccessible(this.aspectJAdviceMethod);
            // TODO AopUtils.invokeJoinpointUsingReflection
            return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
        } catch (IllegalArgumentException ex) {
            throw new Exception("Mismatch on arguments to advice method [" +
                    this.aspectJAdviceMethod + "]; pointcut expression [" +
                    this.pointcut.getPointcutExpression() + "]", ex);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    /**
     * Overridden in around advice to return proceeding join point.
     */
    protected JoinPoint getJoinPoint() {
        return currentJoinPoint();
    }

    /**
     * Get the current join point match at the join point we are being dispatched on.
     */
    protected JoinPointMatch getJoinPointMatch() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        if (!(mi instanceof ProxyMethodInvocation)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        return getJoinPointMatch((ProxyMethodInvocation) mi);
    }

    // 注意不能使用JoinPointMatch.getClass().getName()作为key
    // 因为 Spring AOP does all the matching at a join point, and then all the invocations.
    // 应该使用 JoinPointMatch as the key
    protected JoinPointMatch getJoinPointMatch(ProxyMethodInvocation pmi) {
        return (JoinPointMatch) pmi.getUserAttribute(this.pointcut.getExpression());
    }


    @Override
    public String toString() {
        return getClass().getName() + ": advice method [" + this.aspectJAdviceMethod + "]; " +
                "aspect name '" + this.aspectName + "'";
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
     * 用来排除指定方法的MethodMatcher，如排除静态方法
     */
    private static class AdviceExcludingMethodMatcher extends StaticMethodMatcher {

        private final Method adviceMethod;

        public AdviceExcludingMethodMatcher(Method adviceMethod) {
            this.adviceMethod = adviceMethod;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return !this.adviceMethod.equals(method);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AdviceExcludingMethodMatcher)) {
                return false;
            }
            AdviceExcludingMethodMatcher otherMm = (AdviceExcludingMethodMatcher) other;
            return this.adviceMethod.equals(otherMm.adviceMethod);
        }

        @Override
        public int hashCode() {
            return this.adviceMethod.hashCode();
        }
    }

}
