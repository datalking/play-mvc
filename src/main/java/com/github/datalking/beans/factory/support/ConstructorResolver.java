package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.BeanMetadataAttributeAccessor;
import com.github.datalking.beans.BeanWrapper;
import com.github.datalking.beans.BeanWrapperImpl;
import com.github.datalking.beans.TypeConverter;
import com.github.datalking.beans.factory.config.ConstructorArgumentValues;
import com.github.datalking.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import com.github.datalking.beans.factory.config.DependencyDescriptor;
import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.ParameterNameDiscoverer;
import com.github.datalking.exception.BeanCreationException;
import com.github.datalking.exception.BeanDefinitionStoreException;
import com.github.datalking.exception.BeansException;
import com.github.datalking.exception.TypeMismatchException;
import com.github.datalking.exception.UnsatisfiedDependencyException;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.MethodInvoker;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.util.StringUtils;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 根据参数匹配构造方法或工厂方法
 *
 * @author yaoo on 5/29/18
 */
public class ConstructorResolver {

    private static final String CONSTRUCTOR_PROPERTIES_CLASS_NAME = "java.beans.ConstructorProperties";

    private static final boolean constructorPropertiesAnnotationAvailable =
            ClassUtils.isPresent(CONSTRUCTOR_PROPERTIES_CLASS_NAME, ConstructorResolver.class.getClassLoader());

    private final AbstractAutowireCapableBeanFactory beanFactory;

    public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 根据参数匹配构造方法，创建bean实例
     * 特殊版 针对mybatis dao实例的创建
     * todo 支持可变长参数的构造函数
     */
    public BeanWrapper autowireConstructor(final String beanName,
                                           final RootBeanDefinition mbd,
                                           Constructor<?>[] chosenCtors,
                                           final Object[] explicitArgs) {

        BeanWrapperImpl bw = new BeanWrapperImpl();
        this.beanFactory.initBeanWrapper(bw);

        Constructor<?> constructorToUse = null;
        Object beanInstance = null;

        // 记录实参数量
        int minNrOfArgs;
        ConstructorArgumentValues resolvedValues = null;
        if (explicitArgs != null) {
            minNrOfArgs = explicitArgs.length;
        } else {
            ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
            resolvedValues = new ConstructorArgumentValues();
            minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
        }

        /// 获取实参类型，todo 实参为class时才能这么处理
        List<ValueHolder> genericArgumentValues = resolvedValues.getGenericArgumentValues();
        List<Class<?>> actualParamTypes = new ArrayList<>();
        for (ValueHolder genericArgumentValue : genericArgumentValues) {
            Object o = genericArgumentValue.getValue();
            String s = (String) o;
            Class<?> c = null;
            try {
                c = Class.forName(s);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            actualParamTypes.add(c);
        }

        Class<?> beanClass = mbd.getBeanClass();
        Constructor<?>[] candidates = beanClass.getDeclaredConstructors();

        /// todo 这里只针对mybatis的dao进行了处理，构造方法参数直接为class
        for (Constructor<?> candidate : candidates) {
            // 泛型擦除了，若要得到泛型，应使用getGenericParameterTypes()
            Class<?>[] paramTypes = candidate.getParameterTypes();

            /// 若该构造方法参数数量少于实参数量，则跳过
//            if (paramTypes.length < minNrOfArgs) {
            if (paramTypes.length < minNrOfArgs || paramTypes.length != actualParamTypes.size()) {
                continue;
            }

            /// 若该构造方法参数数量与实参数量相等，则逐个参数比较 todo
            for (int i = 0; i < paramTypes.length; i++) {
//                Class<?> c1 = actualParamTypes.get(i);
                Class<?> c1 = Class.class;
                Class<?> c2 = paramTypes[i];
                if (!c1.getName().equals(c2.getName())) {
                    continue;
                }
            }

            constructorToUse = candidate;
            constructorToUse.setAccessible(true);
        }
        try {
            /// 若找到了合适的构造函数，则基于反射调用构造方法
            if (constructorToUse != null) {

                beanInstance = constructorToUse.newInstance(actualParamTypes.toArray());
            }
            /// 若没找到构造函数，则调用默认的无参构造函数方法
            else {

                beanInstance = beanClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        bw.setWrappedInstance(beanInstance);
        return bw;
    }

    /**
     * 根据参数匹配构造方法，创建bean实例
     */
    public BeanWrapper autowireConstructorBak(final String beanName,
                                              final RootBeanDefinition mbd,
                                              Constructor<?>[] chosenCtors,
                                              final Object[] explicitArgs) {

        BeanWrapperImpl bw = new BeanWrapperImpl();
        this.beanFactory.initBeanWrapper(bw);

        Constructor<?> constructorToUse = null;
        ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;

        if (explicitArgs != null) {
            argsToUse = explicitArgs;
        } else {
            Object[] argsToResolve = null;

//            synchronized (mbd.constructorArgumentLock) {
            constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
            if (constructorToUse != null && mbd.constructorArgumentsResolved) {
                // Found a cached constructor...
                argsToUse = mbd.resolvedConstructorArguments;
                if (argsToUse == null) {
                    argsToResolve = mbd.preparedConstructorArguments;
                }
            }
//            }abst

            if (argsToResolve != null) {
                argsToUse = resolvePreparedArguments(beanName, mbd, bw, constructorToUse, argsToResolve);
            }
        }

        if (constructorToUse == null) {
            // Need to resolve the constructor.
            boolean autowiring = (chosenCtors != null ||
                    mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
            ConstructorArgumentValues resolvedValues = null;

            int minNrOfArgs;

            /// 若传入的参数非空，一般为空
            if (explicitArgs != null) {
                minNrOfArgs = explicitArgs.length;
            }
            /// 若为空
            else {
                ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
                resolvedValues = new ConstructorArgumentValues();

                // 将cargs拷贝到resolvedValues
                minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
            }

            // Take specified constructors, if any.
            Constructor<?>[] candidates = chosenCtors;
            if (candidates == null) {
                Class<?> beanClass = mbd.getBeanClass();
                try {
                    // 直接获取所有构造方法
                    candidates = (mbd.isNonPublicAccessAllowed() ?
                            beanClass.getDeclaredConstructors() :
                            beanClass.getConstructors());
                } catch (Throwable ex) {
                    throw new BeanCreationException(mbd.getBeanClass().getName(), beanName, "Resolution of declared constructors on bean Class [" + beanClass.getName() + "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
                }
            }

            AutowireUtils.sortConstructors(candidates);
            int minTypeDiffWeight = Integer.MAX_VALUE;
            Set<Constructor<?>> ambiguousConstructors = null;
            List<Exception> causes = null;

            /// 遍历所有构造方法
            for (int i = 0; i < candidates.length; i++) {
                Constructor<?> candidate = candidates[i];
                Class<?>[] paramTypes = candidate.getParameterTypes();

                /// 若非空，一般为空，不执行
                if (constructorToUse != null && argsToUse.length > paramTypes.length) {
                    // Already found greedy constructor that can be satisfied ->
                    // do not look any further, there are only less greedy constructors left.
                    break;
                }
                if (paramTypes.length < minNrOfArgs) {
                    continue;
                }

                ArgumentsHolder argsHolder = null;

                if (resolvedValues != null) {
                    try {
                        String[] paramNames = null;
                        if (constructorPropertiesAnnotationAvailable) {
                            paramNames = ConstructorPropertiesChecker.evaluate(candidate, paramTypes.length);
                        }
                        if (paramNames == null) {
                            ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                            if (pnd != null) {
                                // 获取构造方法参数名称
                                paramNames = pnd.getParameterNames(candidate);
                            }
                        }

                        // ==== 遍历构造方法参数并进行类型转换
                        argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, paramTypes, paramNames, candidate, autowiring);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    // Explicit arguments given -> arguments length must match exactly.
                    if (paramTypes.length != explicitArgs.length) {
                        continue;
                    }

                    argsHolder = new ArgumentsHolder(explicitArgs);
                }

                // 一般为0
                int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
                        argsHolder.getTypeDifferenceWeight(paramTypes) :
                        argsHolder.getAssignabilityWeight(paramTypes));

                // Choose this constructor if it represents the closest match.
                /// 若能找到最匹配的构造方法
                if (typeDiffWeight < minTypeDiffWeight) {

                    constructorToUse = candidate;
                    argsHolderToUse = argsHolder;
                    argsToUse = argsHolder.arguments;
                    minTypeDiffWeight = typeDiffWeight;
                    ambiguousConstructors = null;
                }
                /// 若找到多个匹配的构造方法
                else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
                    if (ambiguousConstructors == null) {
                        ambiguousConstructors = new LinkedHashSet<>();
                        ambiguousConstructors.add(constructorToUse);
                    }
                    ambiguousConstructors.add(candidate);
                }
            }

            /// 此时非空
            if (constructorToUse == null) {
                throw new BeanCreationException(mbd.getBeanClassName(), beanName, "Could not resolve matching constructor " +
                        "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities)");
            } else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
                throw new BeanCreationException(mbd.getBeanClassName(), beanName, "Ambiguous constructor matches found in bean '" + beanName + "' " +
                        "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
                        ambiguousConstructors);
            }

            if (explicitArgs == null) {
                argsHolderToUse.storeCache(mbd, constructorToUse);
            }
        }

        try {
            Object beanInstance;

            InstantiationStrategy strategy = this.beanFactory.getInstantiationStrategy();

            // 原理是基于java反射调用构造方法创建对象
            beanInstance = strategy.instantiate(mbd, beanName, this.beanFactory, constructorToUse, argsToUse);

            bw.setWrappedInstance(beanInstance);
            return bw;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getBeanClassName(), beanName, "Instantiation of bean failed", ex);
        }
    }

    /**
     * Resolve the factory method in the specified bean definition, if possible.
     * {@link RootBeanDefinition#getResolvedFactoryMethod()} can be checked for the result.
     */
    public void resolveFactoryMethodIfPossible(RootBeanDefinition mbd) {
        Class<?> factoryClass;
        boolean isStatic;
        if (mbd.getFactoryBeanName() != null) {
            factoryClass = this.beanFactory.getType(mbd.getFactoryBeanName());
            isStatic = false;
        } else {
            factoryClass = mbd.getBeanClass();
            isStatic = true;
        }
        factoryClass = ClassUtils.getUserClass(factoryClass);

        Method[] candidates = getCandidateMethods(factoryClass, mbd);
        Method uniqueCandidate = null;
        for (Method candidate : candidates) {
            if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
                if (uniqueCandidate == null) {
                    uniqueCandidate = candidate;
                } else if (!Arrays.equals(uniqueCandidate.getParameterTypes(), candidate.getParameterTypes())) {
                    uniqueCandidate = null;
                    break;
                }
            }
        }
//        synchronized (mbd.constructorArgumentLock) {
        mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
//        }
    }

    /**
     * Retrieve all candidate methods for the given class, considering
     * the {@link RootBeanDefinition#isNonPublicAccessAllowed()} flag.
     * Called as the starting point for factory method determination.
     */
    private Method[] getCandidateMethods(final Class<?> factoryClass, final RootBeanDefinition mbd) {

        return (mbd.isNonPublicAccessAllowed() ?
                ReflectionUtils.getAllDeclaredMethods(factoryClass) : factoryClass.getMethods());
    }

    public BeanWrapper instantiateUsingFactoryMethod(final String beanName, final RootBeanDefinition mbd, final Object[] explicitArgs) {

        BeanWrapperImpl bw = new BeanWrapperImpl();
        this.beanFactory.initBeanWrapper(bw);

        Object factoryBean;
        Class<?> factoryClass;
        boolean isStatic;

        String factoryBeanName = mbd.getFactoryBeanName();
        if (factoryBeanName != null) {
            if (factoryBeanName.equals(beanName)) {
                throw new BeanDefinitionStoreException(mbd.getBeanClassName(), beanName,
                        "factory-bean reference points back to the same bean definition");
            }
            factoryBean = this.beanFactory.getBean(factoryBeanName);
            if (factoryBean == null) {
                throw new BeanCreationException(mbd.getBeanClassName(), beanName,
                        "factory-bean '" + factoryBeanName + "' (or a BeanPostProcessor involved) returned null");
            }
            factoryClass = factoryBean.getClass();
            isStatic = false;
        } else {
            // It's a static factory method on the bean class.
            if (!mbd.hasBeanClass()) {
                throw new BeanDefinitionStoreException(mbd.getBeanClassName(), beanName,
                        "bean definition declares neither a bean class nor a factory-bean reference");
            }
            factoryBean = null;
            factoryClass = mbd.getBeanClass();
            isStatic = true;
        }

        Method factoryMethodToUse = null;
        ArgumentsHolder argsHolderToUse = null;
        Object[] argsToUse = null;

        if (explicitArgs != null) {
            argsToUse = explicitArgs;
        } else {
            Object[] argsToResolve = null;
//            synchronized (mbd.constructorArgumentLock) {
            factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
            if (factoryMethodToUse != null && mbd.constructorArgumentsResolved) {
                // Found a cached factory method...
                argsToUse = mbd.resolvedConstructorArguments;
                if (argsToUse == null) {
                    argsToResolve = mbd.preparedConstructorArguments;
                }
            }
//            }
            if (argsToResolve != null) {
                argsToUse = resolvePreparedArguments(beanName, mbd, bw, factoryMethodToUse, argsToResolve);
            }
        }

        if (factoryMethodToUse == null || argsToUse == null) {
            // Need to determine the factory method...
            // Try all methods with this name to see if they match the given arguments.
            factoryClass = ClassUtils.getUserClass(factoryClass);

            Method[] rawCandidates = getCandidateMethods(factoryClass, mbd);
            List<Method> candidateSet = new ArrayList<>();
            for (Method candidate : rawCandidates) {
                if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
                    candidateSet.add(candidate);
                }
            }
            Method[] candidates = candidateSet.toArray(new Method[candidateSet.size()]);
            AutowireUtils.sortFactoryMethods(candidates);

            ConstructorArgumentValues resolvedValues = null;
            boolean autowiring = (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
            int minTypeDiffWeight = Integer.MAX_VALUE;
            Set<Method> ambiguousFactoryMethods = null;

            int minNrOfArgs;
            if (explicitArgs != null) {
                minNrOfArgs = explicitArgs.length;
            } else {
                // We don't have arguments passed in programmatically, so we need to resolve the
                // arguments specified in the constructor arguments held in the bean definition.
                ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
                resolvedValues = new ConstructorArgumentValues();
                minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
            }

            List<Exception> causes = null;

            for (int i = 0; i < candidates.length; i++) {
                Method candidate = candidates[i];
                Class<?>[] paramTypes = candidate.getParameterTypes();

                if (paramTypes.length >= minNrOfArgs) {
                    ArgumentsHolder argsHolder = null;

                    if (resolvedValues != null) {
                        // Resolved constructor arguments: type conversion and/or autowiring necessary.
                        try {
                            String[] paramNames = null;
                            ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                            if (pnd != null) {
                                paramNames = pnd.getParameterNames(candidate);
                            }
                            argsHolder = createArgumentArray(
                                    beanName, mbd, resolvedValues, bw, paramTypes, paramNames, candidate, autowiring);
                        } catch (Exception ex) {

                            ex.printStackTrace();
//                            if (i == candidates.length - 1 && argsHolderToUse == null) {
//                                if (causes != null) {
//                                    for (Exception cause : causes) {
//                                        this.beanFactory.onSuppressedException(cause);
//                                    }
//                                }
//                                throw ex;
//                            } else {
//                                // Swallow and try next overloaded factory method.
//                                if (causes == null) {
//                                    causes = new LinkedList<>();
//                                }
//                                causes.add(ex);
//                                continue;
//                            }
                        }
                    } else {
                        // Explicit arguments given -> arguments length must match exactly.
                        if (paramTypes.length != explicitArgs.length) {
                            continue;
                        }
                        argsHolder = new ArgumentsHolder(explicitArgs);
                    }

                    int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
                            argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
                    // Choose this factory method if it represents the closest match.
                    if (typeDiffWeight < minTypeDiffWeight) {
                        factoryMethodToUse = candidate;
                        argsHolderToUse = argsHolder;
                        argsToUse = argsHolder.arguments;
                        minTypeDiffWeight = typeDiffWeight;
                        ambiguousFactoryMethods = null;
                    }
                    // Find out about ambiguity: In case of the same type difference weight
                    // for methods with the same number of parameters, collect such candidates
                    // and eventually raise an ambiguity exception.
                    // However, only perform that check in non-lenient constructor resolution mode,
                    // and explicitly ignore overridden methods (with the same parameter signature).
                    else if (factoryMethodToUse != null && typeDiffWeight == minTypeDiffWeight &&
                            !mbd.isLenientConstructorResolution() &&
                            paramTypes.length == factoryMethodToUse.getParameterTypes().length &&
                            !Arrays.equals(paramTypes, factoryMethodToUse.getParameterTypes())) {
                        if (ambiguousFactoryMethods == null) {
                            ambiguousFactoryMethods = new LinkedHashSet<Method>();
                            ambiguousFactoryMethods.add(factoryMethodToUse);
                        }
                        ambiguousFactoryMethods.add(candidate);
                    }
                }
            }

            if (factoryMethodToUse == null) {
                List<String> argTypes = new ArrayList<>(minNrOfArgs);
                if (explicitArgs != null) {
                    for (Object arg : explicitArgs) {
                        argTypes.add(arg != null ? arg.getClass().getSimpleName() : "null");
                    }
                } else {
                    Set<ValueHolder> valueHolders = new LinkedHashSet<>(resolvedValues.getArgumentCount());
                    valueHolders.addAll(resolvedValues.getIndexedArgumentValues().values());
                    valueHolders.addAll(resolvedValues.getGenericArgumentValues());
                    for (ValueHolder value : valueHolders) {
                        String argType = (value.getType() != null ? ClassUtils.getShortName(value.getType()) :
                                (value.getValue() != null ? value.getValue().getClass().getSimpleName() : "null"));
                        argTypes.add(argType);
                    }
                }
                String argDesc = StringUtils.collectionToCommaDelimitedString(argTypes);
                throw new BeanCreationException(mbd.getBeanClassName(), beanName,
                        "No matching factory method found: " +
                                (mbd.getFactoryBeanName() != null ?
                                        "factory bean '" + mbd.getFactoryBeanName() + "'; " : "") +
                                "factory method '" + mbd.getFactoryMethodName() + "(" + argDesc + ")'. " +
                                "Check that a method with the specified name " +
                                (minNrOfArgs > 0 ? "and arguments " : "") +
                                "exists and that it is " +
                                (isStatic ? "static" : "non-static") + ".");
            } else if (void.class.equals(factoryMethodToUse.getReturnType())) {
                throw new BeanCreationException(mbd.getBeanClassName(), beanName,
                        "Invalid factory method '" + mbd.getFactoryMethodName() +
                                "': needs to have a non-void return type!");
            } else if (ambiguousFactoryMethods != null) {
                throw new BeanCreationException(mbd.getBeanClassName(), beanName,
                        "Ambiguous factory method matches found in bean '" + beanName + "' " +
                                "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
                                ambiguousFactoryMethods);
            }

            if (explicitArgs == null && argsHolderToUse != null) {
                argsHolderToUse.storeCache(mbd, factoryMethodToUse);
            }
        }

        try {
            Object beanInstance;

            beanInstance = beanFactory.getInstantiationStrategy().instantiate(
                    mbd, beanName, beanFactory, factoryBean, factoryMethodToUse, argsToUse);

            if (beanInstance == null) {
                return null;
            }
            bw.setWrappedInstance(beanInstance);
            return bw;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getBeanClassName(), beanName, "Instantiation of bean failed", ex);
        }
    }

    /**
     * Resolve the constructor arguments for this bean into the resolvedValues object.
     * This may involve looking up other beans.
     * This method is also used for handling invocations of static factory methods.
     */
    private int resolveConstructorArguments(String beanName,
                                            RootBeanDefinition mbd,
                                            BeanWrapper bw,
                                            ConstructorArgumentValues cargs,
                                            ConstructorArgumentValues resolvedValues) {

//        TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
        TypeConverter customConverter = null;
        TypeConverter converter = (customConverter != null ? customConverter : bw);

        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);

        int minNrOfArgs = cargs.getArgumentCount();

        for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : cargs.getIndexedArgumentValues().entrySet()) {

            int index = entry.getKey();
            if (index < 0) {
                throw new BeanCreationException(mbd.getBeanClassName(), beanName, "Invalid constructor argument index: " + index);
            }

            if (index > minNrOfArgs) {
                minNrOfArgs = index + 1;
            }

            ConstructorArgumentValues.ValueHolder valueHolder = entry.getValue();

            if (valueHolder.isConverted()) {

                resolvedValues.addIndexedArgumentValue(index, valueHolder);
            } else {

                Object resolvedValue = valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());

                ValueHolder resolvedValueHolder = new ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
                resolvedValueHolder.setSource(valueHolder);
                resolvedValues.addIndexedArgumentValue(index, resolvedValueHolder);
            }
        }

        for (ConstructorArgumentValues.ValueHolder valueHolder : cargs.getGenericArgumentValues()) {

            if (valueHolder.isConverted()) {

                resolvedValues.addGenericArgumentValue(valueHolder);
            } else {

                Object resolvedValue = valueResolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());

                ValueHolder resolvedValueHolder = new ValueHolder(resolvedValue, valueHolder.getType(), valueHolder.getName());
                resolvedValueHolder.setSource(valueHolder);
                resolvedValues.addGenericArgumentValue(resolvedValueHolder);
            }
        }

        return minNrOfArgs;
    }

    /**
     * 构造方法参数获取及类型转换
     * <p>
     * Create an array of arguments to invoke a constructor or factory method,
     * given the resolved constructor argument values.
     */
    private ArgumentsHolder createArgumentArray(String beanName,
                                                RootBeanDefinition mbd,
                                                ConstructorArgumentValues resolvedValues,
                                                BeanWrapper bw,
                                                Class<?>[] paramTypes,
                                                String[] paramNames,
                                                Object methodOrCtor,
                                                boolean autowiring) {

        String methodType = (methodOrCtor instanceof Constructor ? "constructor" : "factory method");
//        TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
        TypeConverter customConverter = null;
        TypeConverter converter = (customConverter != null ? customConverter : bw);

        ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
        Set<ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
        Set<String> autowiredBeanNames = new LinkedHashSet<>(4);

        for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
            Class<?> paramType = paramTypes[paramIndex];
            String paramName = (paramNames != null ? paramNames[paramIndex] : null);
            // Try to find matching constructor argument value, either indexed or generic.
            ConstructorArgumentValues.ValueHolder valueHolder =
                    resolvedValues.getArgumentValue(paramIndex, paramType, paramName, usedValueHolders);
            // If we couldn't find a direct match and are not supposed to autowire,
            // let's try the next generic, untyped argument value as fallback:
            // it could match after type conversion (for example, String -> int).
            if (valueHolder == null && !autowiring) {
                valueHolder = resolvedValues.getGenericArgumentValue(null, null, usedValueHolders);
            }
            if (valueHolder != null) {
                // We found a potential match - let's give it a try.
                // Do not consider the same value definition multiple times!
                usedValueHolders.add(valueHolder);
                Object originalValue = valueHolder.getValue();
                Object convertedValue = null;
                if (valueHolder.isConverted()) {
                    convertedValue = valueHolder.getConvertedValue();
                    args.preparedArguments[paramIndex] = convertedValue;
                } else {
                    ConstructorArgumentValues.ValueHolder sourceHolder =
                            (ConstructorArgumentValues.ValueHolder) valueHolder.getSource();
                    Object sourceValue = sourceHolder.getValue();
                    try {
                        convertedValue = converter.convertIfNecessary(originalValue, paramType,
                                MethodParameter.forMethodOrConstructor(methodOrCtor, paramIndex));
                        // TODO re-enable once race condition has been found (SPR-7423)
						/*
						if (originalValue == sourceValue || sourceValue instanceof TypedStringValue) {
							// Either a converted value or still the original one: store converted value.
							sourceHolder.setConvertedValue(convertedValue);
							args.preparedArguments[paramIndex] = convertedValue;
						}
						else {
						*/
                        args.resolveNecessary = true;
                        args.preparedArguments[paramIndex] = sourceValue;
                        // }
//                    } catch (TypeMismatchException ex) {
//                        throw new UnsatisfiedDependencyException(mbd.getBeanClassName(), beanName, paramIndex, paramType, "Could not convert " + methodType + " argument value of type [" + ObjectUtils.nullSafeClassName(valueHolder.getValue()) + "] to required type [" + paramType.getName() + "]: " + ex.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                args.arguments[paramIndex] = convertedValue;
                args.rawArguments[paramIndex] = originalValue;
            } else {
                // No explicit match found: we're either supposed to autowire or
                // have to fail creating an argument array for the given constructor.
                if (!autowiring) {
                    throw new UnsatisfiedDependencyException(mbd.getBeanClassName(), beanName, paramIndex, paramType, "Ambiguous " + methodType + " argument types - " +
                            "did you specify the correct bean references as " + methodType + " arguments?");
                }
                try {
                    MethodParameter param = MethodParameter.forMethodOrConstructor(methodOrCtor, paramIndex);

                    Object autowiredArgument = resolveAutowiredArgument(param, beanName, autowiredBeanNames, converter);

                    args.rawArguments[paramIndex] = autowiredArgument;
                    args.arguments[paramIndex] = autowiredArgument;
                    args.preparedArguments[paramIndex] = new AutowiredArgumentMarker();
                    args.resolveNecessary = true;
                } catch (BeansException ex) {
                    throw new UnsatisfiedDependencyException(mbd.getBeanClassName(), beanName, paramIndex, paramType, ex);
                }
            }
        }

        for (String autowiredBeanName : autowiredBeanNames) {
            this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
//            if (this.beanFactory.logger.isDebugEnabled()) {
//                this.beanFactory.logger.debug("Autowiring by type from bean name '" + beanName +
//                        "' via " + methodType + " to bean named '" + autowiredBeanName + "'");
//            }
        }

        return args;
    }

    /**
     * Resolve the prepared arguments stored in the given bean definition.
     */
    private Object[] resolvePreparedArguments(String beanName,
                                              RootBeanDefinition mbd,
                                              BeanWrapper bw,
                                              Member methodOrCtor,
                                              Object[] argsToResolve) {

//        TypeConverter customConverter = this.beanFactory.getCustomTypeConverter();
        TypeConverter customConverter = null;
        TypeConverter converter = (customConverter != null ? customConverter : bw);
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, converter);
        Class<?>[] paramTypes = (methodOrCtor instanceof Method ?
                ((Method) methodOrCtor).getParameterTypes() :
                ((Constructor<?>) methodOrCtor).getParameterTypes());

        Object[] resolvedArgs = new Object[argsToResolve.length];
        for (int argIndex = 0; argIndex < argsToResolve.length; argIndex++) {
            Object argValue = argsToResolve[argIndex];
            MethodParameter methodParam = MethodParameter.forMethodOrConstructor(methodOrCtor, argIndex);
            GenericTypeResolver.resolveParameterType(methodParam, methodOrCtor.getDeclaringClass());
            if (argValue instanceof AutowiredArgumentMarker) {
                argValue = resolveAutowiredArgument(methodParam, beanName, null, converter);
//            } else if (argValue instanceof BeanMetadataElement) {
            } else if (argValue instanceof BeanMetadataAttributeAccessor) {
                argValue = valueResolver.resolveValueIfNecessary("constructor argument", argValue);
            } else if (argValue instanceof String) {
                argValue = this.beanFactory.evaluateBeanDefinitionString((String) argValue, mbd);
            }
            Class<?> paramType = paramTypes[argIndex];
            try {
                resolvedArgs[argIndex] = converter.convertIfNecessary(argValue, paramType, methodParam);
            } catch (TypeMismatchException ex) {
                String methodType = (methodOrCtor instanceof Constructor ? "constructor" : "factory method");
                throw new UnsatisfiedDependencyException(
                        mbd.getBeanClassName(), beanName, argIndex, paramType,
                        "Could not convert " + methodType + " argument value of type [" +
                                ObjectUtils.nullSafeClassName(argValue) +
                                "] to required type [" + paramType.getName() + "]: " + ex.getMessage());
            }
        }
        return resolvedArgs;
    }

    /**
     * Template method for resolving the specified argument which is supposed to be autowired.
     */
    protected Object resolveAutowiredArgument(
            MethodParameter param, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) {

        return this.beanFactory.resolveDependency(
                new DependencyDescriptor(param, true), beanName, autowiredBeanNames, typeConverter);
    }


    /**
     * Private inner class for holding argument combinations.
     */
    private static class ArgumentsHolder {

        public final Object rawArguments[];

        public final Object arguments[];

        public final Object preparedArguments[];

        public boolean resolveNecessary = false;

        public ArgumentsHolder(int size) {
            this.rawArguments = new Object[size];
            this.arguments = new Object[size];
            this.preparedArguments = new Object[size];
        }

        public ArgumentsHolder(Object[] args) {
            this.rawArguments = args;
            this.arguments = args;
            this.preparedArguments = args;
        }

        public int getTypeDifferenceWeight(Class<?>[] paramTypes) {
            // If valid arguments found, determine type difference weight.
            // Try type difference weight on both the converted arguments and
            // the raw arguments. If the raw weight is better, use it.
            // Decrease raw weight by 1024 to prefer it over equal converted weight.
            int typeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.arguments);
            int rawTypeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.rawArguments) - 1024;
            return (rawTypeDiffWeight < typeDiffWeight ? rawTypeDiffWeight : typeDiffWeight);
        }

        public int getAssignabilityWeight(Class<?>[] paramTypes) {
            for (int i = 0; i < paramTypes.length; i++) {
                if (!ClassUtils.isAssignableValue(paramTypes[i], this.arguments[i])) {
                    return Integer.MAX_VALUE;
                }
            }
            for (int i = 0; i < paramTypes.length; i++) {
                if (!ClassUtils.isAssignableValue(paramTypes[i], this.rawArguments[i])) {
                    return Integer.MAX_VALUE - 512;
                }
            }
            return Integer.MAX_VALUE - 1024;
        }

        public void storeCache(RootBeanDefinition mbd, Object constructorOrFactoryMethod) {
//            synchronized (mbd.constructorArgumentLock) {
            mbd.resolvedConstructorOrFactoryMethod = constructorOrFactoryMethod;
            mbd.constructorArgumentsResolved = true;
            if (this.resolveNecessary) {
                mbd.preparedConstructorArguments = this.preparedArguments;
            } else {
                mbd.resolvedConstructorArguments = this.arguments;
            }
//            }
        }
    }


    /**
     * Marker for autowired arguments in a cached argument array.
     */
    private static class AutowiredArgumentMarker {
    }


    /**
     * Inner class to avoid a Java 6 dependency.
     */
    private static class ConstructorPropertiesChecker {

        public static String[] evaluate(Constructor<?> candidate, int paramCount) {
            ConstructorProperties cp = candidate.getAnnotation(ConstructorProperties.class);
            if (cp != null) {
                String[] names = cp.value();
                if (names.length != paramCount) {
                    throw new IllegalStateException("Constructor annotated with @ConstructorProperties but not " +
                            "corresponding to actual number of parameters (" + paramCount + "): " + candidate);
                }
                return names;
            } else {
                return null;
            }
        }
    }
}
