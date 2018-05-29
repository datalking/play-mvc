package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.exception.BeanDefinitionStoreException;
import com.github.datalking.exception.BeanInstantiationException;
import com.github.datalking.util.BeanUtils;
import com.github.datalking.util.ReflectionUtils;
import com.github.datalking.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * @author yaoo on 5/29/18
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {

    private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<>();

    public static Method getCurrentlyInvokedFactoryMethod() {
        return currentlyInvokedFactoryMethod.get();
    }


    public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {

        // Don't override the class with CGLIB if no overrides.
        if (beanDefinition.getMethodOverrides().isEmpty()) {
            Constructor<?> constructorToUse;
//            synchronized (beanDefinition.constructorArgumentLock) {
            constructorToUse = (Constructor<?>) beanDefinition.resolvedConstructorOrFactoryMethod;
            if (constructorToUse == null) {
                final Class<?> clazz = beanDefinition.getBeanClass();
                if (clazz.isInterface()) {
                    throw new BeanInstantiationException(clazz, "Specified class is an interface");
                }
                try {
                    if (System.getSecurityManager() != null) {
                        constructorToUse = AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor>() {
                            public Constructor<?> run() throws Exception {
                                return clazz.getDeclaredConstructor((Class[]) null);
                            }
                        });
                    } else {
                        constructorToUse = clazz.getDeclaredConstructor((Class[]) null);
                    }
                    beanDefinition.resolvedConstructorOrFactoryMethod = constructorToUse;
                } catch (Exception ex) {
                    throw new BeanInstantiationException(clazz, "No default constructor found", ex);
                }
            }
//            }
            return BeanUtils.instantiateClass(constructorToUse);
        } else {
            // Must generate CGLIB subclass.
            return instantiateWithMethodInjection(beanDefinition, beanName, owner);
        }
    }


    protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {

        throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
    }

    public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
                              final Constructor<?> ctor, Object[] args) {

        if (beanDefinition.getMethodOverrides().isEmpty()) {

//            if (System.getSecurityManager() != null) {
//                // use own privileged to change accessibility (when security is on)
//                AccessController.doPrivileged(new PrivilegedAction<Object>() {
//                    public Object run() {
//                        ReflectionUtils.makeAccessible(ctor);
//                        return null;
//                    }
//                });
//            }

            return BeanUtils.instantiateClass(ctor, args);
        } else {
            return instantiateWithMethodInjection(beanDefinition, beanName, owner, ctor, args);
        }
    }

    protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition,
                                                    String beanName,
                                                    BeanFactory owner,
                                                    Constructor<?> ctor,
                                                    Object[] args) {

        throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
    }

    public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
                              Object factoryBean, final Method factoryMethod, Object[] args) {

        try {
//            if (System.getSecurityManager() != null) {
//                AccessController.doPrivileged(new PrivilegedAction<Object>() {
//                    public Object run() {
//                        ReflectionUtils.makeAccessible(factoryMethod);
//                        return null;
//                    }
//                });
//            }
//            else {
            ReflectionUtils.makeAccessible(factoryMethod);
//            }

            Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
            try {
                currentlyInvokedFactoryMethod.set(factoryMethod);
                return factoryMethod.invoke(factoryBean, args);
            } finally {
                if (priorInvokedFactoryMethod != null) {
                    currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
                } else {
                    currentlyInvokedFactoryMethod.remove();
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new BeanDefinitionStoreException("Illegal arguments to factory method [" + factoryMethod + "]; " + "args: " + StringUtils.arrayToCommaDelimitedString(args));
        } catch (IllegalAccessException ex) {
            throw new BeanDefinitionStoreException("Cannot access factory method [" + factoryMethod + "]; is it public?");
        } catch (InvocationTargetException ex) {
            throw new BeanDefinitionStoreException("Factory method [" + factoryMethod + "] threw exception", ex.getTargetException());
        }
    }

}
