package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.ClassFilter;
import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.ProxyMethodInvocation;
import com.github.datalking.aop.framework.ProxyCreationContext;
import com.github.datalking.aop.interceptor.ExposeInvocationInterceptor;
import com.github.datalking.aop.support.AbstractExpressionPointcut;
import com.github.datalking.aop.support.AopUtils;
import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.weaver.patterns.NamePattern;
import org.aspectj.weaver.reflect.ReflectionWorld;
import org.aspectj.weaver.reflect.ShadowMatchImpl;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行基于AspectJ的切入点表达式的匹配
 *
 * @author yaoo on 4/19/18
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut
        implements ClassFilter, MethodMatcher, BeanFactoryAware {

    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
    }

    private Class<?> pointcutDeclarationScope;

    private String[] pointcutParameterNames = new String[0];

    private Class<?>[] pointcutParameterTypes = new Class<?>[0];

    private BeanFactory beanFactory;

    private transient ClassLoader pointcutClassLoader;

    private transient PointcutExpression pointcutExpression;

    private transient Map<Method, ShadowMatch> shadowMatchCache = new ConcurrentHashMap<>(32);

    public AspectJExpressionPointcut() {
        //this.pointcutExpression = buildPointcutExpression(this.getClass().getClassLoader());
    }

    public AspectJExpressionPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes) {
        this.pointcutDeclarationScope = declarationScope;
        if (paramNames.length != paramTypes.length) {
            throw new IllegalStateException("Number of pointcut parameter names must match number of pointcut parameter types");
        }
        this.pointcutParameterNames = paramNames;
        this.pointcutParameterTypes = paramTypes;

        //this.pointcutExpression = buildPointcutExpression(this.getClass().getClassLoader());
    }

    /**
     * 创建切入点表达式
     */
    private PointcutExpression buildPointcutExpression(ClassLoader classLoader) {

        //初始化一个PointcutParser的实例 PointcutParser
        PointcutParser parser = initializePointcutParser(classLoader);
        PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
        for (int i = 0; i < pointcutParameters.length; i++) {
            pointcutParameters[i] = parser.createPointcutParameter(this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
        }

        //解析切点表达式 我们的切点表示有可能会这样写：在切面中定义一个专门的切面表达式方法
        //在不同的通知类型中引入这个切点表达式的方法名
        return parser.parsePointcutExpression(
                replaceBooleanOperators(getExpression()),
                this.pointcutDeclarationScope,
                pointcutParameters);
    }

    private void checkReadyToMatch() {
        if (getExpression() == null) {
            throw new IllegalStateException("Must set property 'expression' before attempting to match");
        }
        if (this.pointcutExpression == null) {
            this.pointcutClassLoader = this.getClass().getClassLoader();
            this.pointcutExpression = buildPointcutExpression(this.pointcutClassLoader);
        }
    }

    private PointcutParser initializePointcutParser(ClassLoader cl) {
        PointcutParser parser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
                SUPPORTED_PRIMITIVES, cl);
        parser.registerPointcutDesignatorHandler(new BeanNamePointcutDesignatorHandler());
        return parser;
    }

    private String replaceBooleanOperators(String pcExpr) {
        String result = StringUtils.replace(pcExpr, " and ", " && ");
        result = StringUtils.replace(result, " or ", " || ");
        result = StringUtils.replace(result, " not ", " ! ");
        return result;
    }

    public PointcutExpression getPointcutExpression() {
        checkReadyToMatch();
        return this.pointcutExpression;
    }

    @Override
    public boolean matches(Class<?> targetClass) {
        Assert.notNull(targetClass, "targetClass cannot be null!");
        checkReadyToMatch();
        return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        checkReadyToMatch();
        Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        ShadowMatch shadowMatch = getShadowMatch(targetMethod, method);

        if (shadowMatch.alwaysMatches()) {
            return true;
        } else if (shadowMatch.neverMatches()) {
            return false;
        } else {
            RuntimeTestWalker walker = getRuntimeTestWalker(shadowMatch);
            return (!walker.testsSubtypeSensitiveVars() || walker.testTargetInstanceOfResidue(targetClass));
        }
    }

    @Override
    public boolean isRuntime() {
        checkReadyToMatch();
        return this.pointcutExpression.mayNeedDynamicTest();
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        checkReadyToMatch();
        ShadowMatch shadowMatch = getShadowMatch(AopUtils.getMostSpecificMethod(method, targetClass), method);
        ShadowMatch originalShadowMatch = getShadowMatch(method, method);

        ProxyMethodInvocation pmi = null;
        Object targetObject = null;
        Object thisObject = null;
        try {
            MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
            targetObject = mi.getThis();
            if (!(mi instanceof ProxyMethodInvocation)) {
                throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
            }
            pmi = (ProxyMethodInvocation) mi;
            thisObject = pmi.getProxy();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }

        try {
            JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);

            return joinPointMatch.matches();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public void setPointcutDeclarationScope(Class<?> pointcutDeclarationScope) {
        this.pointcutDeclarationScope = pointcutDeclarationScope;
    }

    // Set the parameter names for the pointcut.
    public void setParameterNames(String... names) {
        this.pointcutParameterNames = names;
    }

    // Set the parameter types for the pointcut.
    public void setParameterTypes(Class<?>... types) {
        this.pointcutParameterTypes = types;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    @Override
    public ClassFilter getClassFilter() {
        checkReadyToMatch();
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        checkReadyToMatch();
        return this;
    }

    private String getCurrentProxiedBeanName() {
        return ProxyCreationContext.getCurrentProxiedBeanName();
    }

    private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
        // Avoid lock contention for known Methods through concurrent access...
        ShadowMatch shadowMatch = this.shadowMatchCache.get(targetMethod);
        if (shadowMatch == null) {
            synchronized (this.shadowMatchCache) {
                // Not found - now check again with full lock...
                PointcutExpression fallbackExpression = null;
                Method methodToMatch = targetMethod;
                shadowMatch = this.shadowMatchCache.get(targetMethod);
                if (shadowMatch == null) {
                    try {
                        try {
                            shadowMatch = this.pointcutExpression.matchesMethodExecution(methodToMatch);
                        } catch (ReflectionWorld.ReflectionWorldException ex) {
                            // Failed to introspect target method, probably because it has been loaded
                            // in a special ClassLoader. Let's try the declaring ClassLoader instead...
                            try {
                                fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
                                if (fallbackExpression != null) {
                                    shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
                                }
                            } catch (ReflectionWorld.ReflectionWorldException ex2) {
                                fallbackExpression = null;
                            }
                        }
                        if (shadowMatch == null && targetMethod != originalMethod) {
                            methodToMatch = originalMethod;
                            try {
                                shadowMatch = this.pointcutExpression.matchesMethodExecution(methodToMatch);
                            } catch (ReflectionWorld.ReflectionWorldException ex3) {
                                // Could neither introspect the target class nor the proxy class ->
                                // let's try the original method's declaring class before we give up...
                                try {
                                    fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
                                    if (fallbackExpression != null) {
                                        shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
                                    }
                                } catch (ReflectionWorld.ReflectionWorldException ex4) {
                                    fallbackExpression = null;
                                }
                            }
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        fallbackExpression = null;
                    }
                    if (shadowMatch == null) {
                        shadowMatch = new ShadowMatchImpl(org.aspectj.util.FuzzyBoolean.NO, null, null, null);
                    } else if (shadowMatch.maybeMatches() && fallbackExpression != null) {
                        shadowMatch = new DefensiveShadowMatch(shadowMatch, fallbackExpression.matchesMethodExecution(methodToMatch));
                    }
                    this.shadowMatchCache.put(targetMethod, shadowMatch);
                }
            }
        }
        return shadowMatch;
    }

    private PointcutExpression getFallbackPointcutExpression(Class<?> targetClass) {
        try {
            ClassLoader classLoader = targetClass.getClassLoader();
            if (classLoader != null && classLoader != this.pointcutClassLoader) {
                return buildPointcutExpression(classLoader);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private RuntimeTestWalker getRuntimeTestWalker(ShadowMatch shadowMatch) {
        if (shadowMatch instanceof DefensiveShadowMatch) {
            return new RuntimeTestWalker(((DefensiveShadowMatch) shadowMatch).primary);
        }
        return new RuntimeTestWalker(shadowMatch);
    }

    private class BeanNamePointcutDesignatorHandler implements PointcutDesignatorHandler {

        private static final String BEAN_DESIGNATOR_NAME = "bean";

        @Override
        public String getDesignatorName() {
            return BEAN_DESIGNATOR_NAME;
        }

        @Override
        public ContextBasedMatcher parse(String expression) {
            return new BeanNameContextMatcher(expression);
        }
    }

    private class BeanNameContextMatcher implements ContextBasedMatcher {

        private final NamePattern expressionPattern;

        public BeanNameContextMatcher(String expression) {
            this.expressionPattern = new NamePattern(expression);
        }

        @Override
        @SuppressWarnings("rawtypes")
        @Deprecated
        public boolean couldMatchJoinPointsInType(Class someClass) {
            return (contextMatch(someClass) == FuzzyBoolean.YES);
        }

        @Override
        @SuppressWarnings("rawtypes")
        @Deprecated
        public boolean couldMatchJoinPointsInType(Class someClass, MatchingContext context) {
            return (contextMatch(someClass) == FuzzyBoolean.YES);
        }

        @Override
        public boolean matchesDynamically(MatchingContext context) {
            return true;
        }

        @Override
        public FuzzyBoolean matchesStatically(MatchingContext context) {
            return contextMatch(null);
        }

        @Override
        public boolean mayNeedDynamicTest() {
            return false;
        }

        private FuzzyBoolean contextMatch(Class<?> targetType) {
            String advisedBeanName = getCurrentProxiedBeanName();
            // no proxy creation in progress
            if (advisedBeanName == null) {
                // abstain; can't return YES, since that will make pointcut with negation fail
                return FuzzyBoolean.MAYBE;
            }
//            if (BeanFactoryUtils.isGeneratedBeanName(advisedBeanName)) {
//                return FuzzyBoolean.NO;
//            }
//            if (targetType != null) {
//                boolean isFactory = FactoryBean.class.isAssignableFrom(targetType);
//                return FuzzyBoolean.fromBoolean(
//                        matchesBeanName(isFactory ? BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName : advisedBeanName));
//            } else {
//                return FuzzyBoolean.fromBoolean(matchesBeanName(advisedBeanName) ||
//                        matchesBeanName(BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName));
//            }
            return FuzzyBoolean.NO;

        }

        private boolean matchesBeanName(String advisedBeanName) {
            if (this.expressionPattern.matches(advisedBeanName)) {
                return true;
            }
            return false;
        }
    }

    /**
     * ShadowMatch子类
     */
    private static class DefensiveShadowMatch implements ShadowMatch {

        private final ShadowMatch primary;

        private final ShadowMatch other;

        public DefensiveShadowMatch(ShadowMatch primary, ShadowMatch other) {
            this.primary = primary;
            this.other = other;
        }

        @Override
        public boolean alwaysMatches() {
            return this.primary.alwaysMatches();
        }

        @Override
        public boolean maybeMatches() {
            return this.primary.maybeMatches();
        }

        @Override
        public boolean neverMatches() {
            return this.primary.neverMatches();
        }

        @Override
        public JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args) {
            try {
                return this.primary.matchesJoinPoint(thisObject, targetObject, args);
            } catch (ReflectionWorld.ReflectionWorldException ex) {
                return this.other.matchesJoinPoint(thisObject, targetObject, args);
            }
        }

        @Override
        public void setMatchingContext(MatchingContext aMatchContext) {
            this.primary.setMatchingContext(aMatchContext);
            this.other.setMatchingContext(aMatchContext);
        }
    }


}
