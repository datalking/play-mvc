package com.github.datalking.beans;

import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * PropertyDescriptor的子类，重写了getPropertyType()
 *
 * @author yaoo on 5/10/18
 */
public class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor {

    private final Class<?> beanClass;

    private final Method readMethod;

    private final Method writeMethod;

    private volatile Set<Method> ambiguousWriteMethods;

    private MethodParameter writeMethodParameter;

    private Class<?> propertyType;

    private final Class<?> propertyEditorClass;

    public GenericTypeAwarePropertyDescriptor(Class<?> beanClass,
                                              String propertyName,
                                              Method readMethod,
                                              Method writeMethod,
                                              Class<?> propertyEditorClass) throws IntrospectionException {

        super(propertyName, null, null);

        if (beanClass == null) {
            throw new IntrospectionException("Bean class must not be null");
        }
        this.beanClass = beanClass;

//        Method readMethodToUse = BridgeMethodResolver.findBridgedMethod(readMethod);
//        Method writeMethodToUse = BridgeMethodResolver.findBridgedMethod(writeMethod);
        Method readMethodToUse = readMethod;
        Method writeMethodToUse = writeMethod;
        if (writeMethodToUse == null && readMethodToUse != null) {
            // Fallback: Original JavaBeans introspection might not have found matching setter
            // method due to lack of bridge method resolution, in case of the getter using a
            // covariant return type whereas the setter is defined for the concrete property type.

            String mName = "set" + StringUtils.capitalize(getName());
            Method candidate = ClassUtils.getMethodIfAvailable(this.beanClass, mName, (Class<?>[]) null);

            if (candidate != null && candidate.getParameterTypes().length == 1) {
                writeMethodToUse = candidate;
            }
        }
        this.readMethod = readMethodToUse;
        this.writeMethod = writeMethodToUse;

        if (this.writeMethod != null && this.readMethod == null) {
            // Write method not matched against read method: potentially ambiguous through
            // several overloaded variants, in which case an arbitrary winner has been chosen
            // by the JDK's JavaBeans Introspector...
            Set<Method> ambiguousCandidates = new HashSet<>();
            for (Method method : beanClass.getMethods()) {
                if (method.getName().equals(writeMethodToUse.getName()) &&
                        !method.equals(writeMethodToUse) && !method.isBridge() &&
                        method.getParameterTypes().length == writeMethodToUse.getParameterTypes().length) {
                    ambiguousCandidates.add(method);
                }
            }
            if (!ambiguousCandidates.isEmpty()) {
                this.ambiguousWriteMethods = ambiguousCandidates;
            }
        }

        this.propertyEditorClass = propertyEditorClass;
    }


    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Override
    public Method getReadMethod() {
        return this.readMethod;
    }

    @Override
    public Method getWriteMethod() {
        return this.writeMethod;
    }

    public Method getWriteMethodForActualAccess() {
        Set<Method> ambiguousCandidates = this.ambiguousWriteMethods;
        if (ambiguousCandidates != null) {
            this.ambiguousWriteMethods = null;
        }
        return this.writeMethod;
    }

    public synchronized MethodParameter getWriteMethodParameter() {
        if (this.writeMethod == null) {
            return null;
        }
        if (this.writeMethodParameter == null) {
            this.writeMethodParameter = new MethodParameter(this.writeMethod, 0);
            GenericTypeResolver.resolveParameterType(this.writeMethodParameter, this.beanClass);
        }
        return this.writeMethodParameter;
    }

    @Override
    public synchronized Class<?> getPropertyType() {

        if (this.propertyType == null) {
            if (this.readMethod != null) {

                this.propertyType = GenericTypeResolver.resolveReturnType(this.readMethod, this.beanClass);

            } else {

                MethodParameter writeMethodParam = getWriteMethodParameter();

                if (writeMethodParam != null) {

                    this.propertyType = writeMethodParam.getParameterType();
                } else {

                    this.propertyType = super.getPropertyType();
                }
            }
        }
        return this.propertyType;
    }

    @Override
    public Class<?> getPropertyEditorClass() {
        return this.propertyEditorClass;
    }

}
