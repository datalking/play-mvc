package com.github.datalking.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.github.datalking.beans.PropertyDescriptorUtils.copyNonMethodProperties;
import static com.github.datalking.beans.PropertyDescriptorUtils.compareMethods;
import static com.github.datalking.beans.PropertyDescriptorUtils.findPropertyType;
import static com.github.datalking.beans.PropertyDescriptorUtils.findIndexedPropertyType;

/**
 * @author yaoo on 5/28/18
 */
public class ExtendedBeanInfo implements BeanInfo {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedBeanInfo.class);

    private final BeanInfo delegate;

    private final Set<PropertyDescriptor> propertyDescriptors = new TreeSet<>(new PropertyDescriptorComparator());

    public ExtendedBeanInfo(BeanInfo delegate) throws IntrospectionException {
        this.delegate = delegate;

        /// 遍历PropertyDescriptor
        for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
            try {

                this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor ?
                        new SimpleIndexedPropertyDescriptor((IndexedPropertyDescriptor) pd) :
                        new SimplePropertyDescriptor(pd));
            } catch (IntrospectionException ex) {
                // Probably simply a method that wasn't meant to follow the JavaBeans pattern...
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring invalid bean property '" + pd.getName() + "': " + ex.getMessage());
                }
            }
        }

        MethodDescriptor[] methodDescriptors = delegate.getMethodDescriptors();
        if (methodDescriptors != null) {
            /// 遍历Method
            for (Method method : findCandidateWriteMethods(methodDescriptors)) {
                try {
                    handleCandidateWriteMethod(method);
                } catch (IntrospectionException ex) {
                    // We're only trying to find candidates, can easily ignore extra ones here...
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring candidate write method [" + method + "]: " + ex.getMessage());
                    }
                }
            }
        }
    }


    private List<Method> findCandidateWriteMethods(MethodDescriptor[] methodDescriptors) {
        List<Method> matches = new ArrayList<>();
        for (MethodDescriptor methodDescriptor : methodDescriptors) {
            Method method = methodDescriptor.getMethod();
            if (isCandidateWriteMethod(method)) {
                matches.add(method);
            }
        }
        // Sort non-void returning write methods to guard against the ill effects of
        // non-deterministic sorting of methods returned from Class#getDeclaredMethods
        // under JDK 7. See http://bugs.sun.com/view_bug.do?bug_id=7023180
        Collections.sort(matches, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m2.toString().compareTo(m1.toString());
            }
        });
        return matches;
    }

    public static boolean isCandidateWriteMethod(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        int nParams = parameterTypes.length;
        return (methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers()) &&
                (!void.class.isAssignableFrom(method.getReturnType()) || Modifier.isStatic(method.getModifiers())) &&
                (nParams == 1 || (nParams == 2 && parameterTypes[0].equals(int.class))));
    }

    private void handleCandidateWriteMethod(Method method) throws IntrospectionException {
        int nParams = method.getParameterTypes().length;
        String propertyName = propertyNameFor(method);
        Class<?> propertyType = method.getParameterTypes()[nParams - 1];
        PropertyDescriptor existingPd = findExistingPropertyDescriptor(propertyName, propertyType);
        if (nParams == 1) {
            if (existingPd == null) {
                this.propertyDescriptors.add(new SimplePropertyDescriptor(propertyName, null, method));
            } else {
                existingPd.setWriteMethod(method);
            }
        } else if (nParams == 2) {
            if (existingPd == null) {
                this.propertyDescriptors.add(
                        new SimpleIndexedPropertyDescriptor(propertyName, null, null, null, method));
            } else if (existingPd instanceof IndexedPropertyDescriptor) {
                ((IndexedPropertyDescriptor) existingPd).setIndexedWriteMethod(method);
            } else {
                this.propertyDescriptors.remove(existingPd);
                this.propertyDescriptors.add(new SimpleIndexedPropertyDescriptor(
                        propertyName, existingPd.getReadMethod(), existingPd.getWriteMethod(), null, method));
            }
        } else {
            throw new IllegalArgumentException("Write method must have exactly 1 or 2 parameters: " + method);
        }
    }

    private PropertyDescriptor findExistingPropertyDescriptor(String propertyName, Class<?> propertyType) {
        for (PropertyDescriptor pd : this.propertyDescriptors) {
            final Class<?> candidateType;
            final String candidateName = pd.getName();
            if (pd instanceof IndexedPropertyDescriptor) {
                IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
                candidateType = ipd.getIndexedPropertyType();
                if (candidateName.equals(propertyName) &&
                        (candidateType.equals(propertyType) || candidateType.equals(propertyType.getComponentType()))) {
                    return pd;
                }
            } else {
                candidateType = pd.getPropertyType();
                if (candidateName.equals(propertyName) &&
                        (candidateType.equals(propertyType) || propertyType.equals(candidateType.getComponentType()))) {
                    return pd;
                }
            }
        }
        return null;
    }

    private String propertyNameFor(Method method) {
        return Introspector.decapitalize(method.getName().substring(3, method.getName().length()));
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptors.toArray(new PropertyDescriptor[this.propertyDescriptors.size()]);
    }

    public BeanInfo[] getAdditionalBeanInfo() {
        return this.delegate.getAdditionalBeanInfo();
    }

    public BeanDescriptor getBeanDescriptor() {
        return this.delegate.getBeanDescriptor();
    }

    public int getDefaultEventIndex() {
        return this.delegate.getDefaultEventIndex();
    }

    public int getDefaultPropertyIndex() {
        return this.delegate.getDefaultPropertyIndex();
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
        return this.delegate.getEventSetDescriptors();
    }

    public Image getIcon(int iconKind) {
        return this.delegate.getIcon(iconKind);
    }

    public MethodDescriptor[] getMethodDescriptors() {
        return this.delegate.getMethodDescriptors();
    }
}


class SimplePropertyDescriptor extends PropertyDescriptor {

    private Method readMethod;

    private Method writeMethod;

    private Class<?> propertyType;

    private Class<?> propertyEditorClass;

    public SimplePropertyDescriptor(PropertyDescriptor original) throws IntrospectionException {
        this(original.getName(), original.getReadMethod(), original.getWriteMethod());
        copyNonMethodProperties(original, this);
    }

    public SimplePropertyDescriptor(String propertyName, Method readMethod, Method writeMethod) throws IntrospectionException {
        super(propertyName, null, null);
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.propertyType = findPropertyType(readMethod, writeMethod);
    }

    @Override
    public Method getReadMethod() {
        return this.readMethod;
    }

    @Override
    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    @Override
    public Method getWriteMethod() {
        return this.writeMethod;
    }

    @Override
    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    @Override
    public Class<?> getPropertyType() {
        if (this.propertyType == null) {
            try {
                this.propertyType = findPropertyType(this.readMethod, this.writeMethod);
            } catch (IntrospectionException ex) {
                // Ignore, as does PropertyDescriptor#getPropertyType
            }
        }
        return this.propertyType;
    }

    @Override
    public Class<?> getPropertyEditorClass() {
        return this.propertyEditorClass;
    }

    @Override
    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
        this.propertyEditorClass = propertyEditorClass;
    }

    @Override
    public boolean equals(Object obj) {
        return PropertyDescriptorUtils.equals(this, obj);
    }

    @Override
    public String toString() {
        return String.format("%s[name=%s, propertyType=%s, readMethod=%s, writeMethod=%s]",
                getClass().getSimpleName(), getName(), getPropertyType(), this.readMethod, this.writeMethod);
    }
}


class SimpleIndexedPropertyDescriptor extends IndexedPropertyDescriptor {

    private Method readMethod;

    private Method writeMethod;

    private Class<?> propertyType;

    private Method indexedReadMethod;

    private Method indexedWriteMethod;

    private Class<?> indexedPropertyType;

    private Class<?> propertyEditorClass;

    public SimpleIndexedPropertyDescriptor(IndexedPropertyDescriptor original) throws IntrospectionException {
        this(original.getName(), original.getReadMethod(), original.getWriteMethod(),
                original.getIndexedReadMethod(), original.getIndexedWriteMethod());
        copyNonMethodProperties(original, this);
    }

    public SimpleIndexedPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod,
                                           Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {

        super(propertyName, null, null, null, null);
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.propertyType = findPropertyType(readMethod, writeMethod);
        this.indexedReadMethod = indexedReadMethod;
        this.indexedWriteMethod = indexedWriteMethod;
        this.indexedPropertyType = findIndexedPropertyType(propertyName, this.propertyType, indexedReadMethod, indexedWriteMethod);
    }

    @Override
    public Method getReadMethod() {
        return this.readMethod;
    }

    @Override
    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
    }

    @Override
    public Method getWriteMethod() {
        return this.writeMethod;
    }

    @Override
    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

    @Override
    public Class<?> getPropertyType() {
        if (this.propertyType == null) {
            try {
                this.propertyType = findPropertyType(this.readMethod, this.writeMethod);
            } catch (IntrospectionException ex) {
                // Ignore, as does IndexedPropertyDescriptor#getPropertyType
            }
        }
        return this.propertyType;
    }

    @Override
    public Method getIndexedReadMethod() {
        return this.indexedReadMethod;
    }

    @Override
    public void setIndexedReadMethod(Method indexedReadMethod) throws IntrospectionException {
        this.indexedReadMethod = indexedReadMethod;
    }

    @Override
    public Method getIndexedWriteMethod() {
        return this.indexedWriteMethod;
    }

    @Override
    public void setIndexedWriteMethod(Method indexedWriteMethod) throws IntrospectionException {
        this.indexedWriteMethod = indexedWriteMethod;
    }

    @Override
    public Class<?> getIndexedPropertyType() {
        if (this.indexedPropertyType == null) {
            try {
                this.indexedPropertyType = findIndexedPropertyType(
                        getName(), getPropertyType(), this.indexedReadMethod, this.indexedWriteMethod);
            } catch (IntrospectionException ex) {
                // Ignore, as does IndexedPropertyDescriptor#getIndexedPropertyType
            }
        }
        return this.indexedPropertyType;
    }

    @Override
    public Class<?> getPropertyEditorClass() {
        return this.propertyEditorClass;
    }

    @Override
    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
        this.propertyEditorClass = propertyEditorClass;
    }

    /*
     * See java.beans.IndexedPropertyDescriptor#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IndexedPropertyDescriptor)) {
            return false;
        }
        IndexedPropertyDescriptor otherPd = (IndexedPropertyDescriptor) other;
        if (!compareMethods(getIndexedReadMethod(), otherPd.getIndexedReadMethod())) {
            return false;
        }
        if (!compareMethods(getIndexedWriteMethod(), otherPd.getIndexedWriteMethod())) {
            return false;
        }
        if (getIndexedPropertyType() != otherPd.getIndexedPropertyType()) {
            return false;
        }
        return PropertyDescriptorUtils.equals(this, other);
    }

    @Override
    public String toString() {
        return String.format("%s[name=%s, propertyType=%s, indexedPropertyType=%s, " +
                        "readMethod=%s, writeMethod=%s, indexedReadMethod=%s, indexedWriteMethod=%s]",
                getClass().getSimpleName(), getName(), getPropertyType(), getIndexedPropertyType(),
                this.readMethod, this.writeMethod, this.indexedReadMethod, this.indexedWriteMethod);
    }
}


class PropertyDescriptorUtils {

    /*
     * See java.beans.FeatureDescriptor#FeatureDescriptor(FeatureDescriptor)
     */
    public static void copyNonMethodProperties(PropertyDescriptor source, PropertyDescriptor target)
            throws IntrospectionException {

        target.setExpert(source.isExpert());
        target.setHidden(source.isHidden());
        target.setPreferred(source.isPreferred());
        target.setName(source.getName());
        target.setShortDescription(source.getShortDescription());
        target.setDisplayName(source.getDisplayName());

        // Copy all attributes (emulating behavior of private FeatureDescriptor#addTable)
        Enumeration<String> keys = source.attributeNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            target.setValue(key, source.getValue(key));
        }

        // See java.beans.PropertyDescriptor#PropertyDescriptor(PropertyDescriptor)
        target.setPropertyEditorClass(source.getPropertyEditorClass());
        target.setBound(source.isBound());
        target.setConstrained(source.isConstrained());
    }

    /*
     * See PropertyDescriptor#findPropertyType
     */
    public static Class<?> findPropertyType(Method readMethod, Method writeMethod) throws IntrospectionException {
        Class<?> propertyType = null;
        if (readMethod != null) {
            Class<?>[] params = readMethod.getParameterTypes();
            if (params.length != 0) {
                throw new IntrospectionException("Bad read method arg count: " + readMethod);
            }
            propertyType = readMethod.getReturnType();
            if (propertyType == Void.TYPE) {
                throw new IntrospectionException("Read method returns void: " + readMethod);
            }
        }
        if (writeMethod != null) {
            Class<?> params[] = writeMethod.getParameterTypes();
            if (params.length != 1) {
                throw new IntrospectionException("Bad write method arg count: " + writeMethod);
            }
            if (propertyType != null) {
                if (propertyType.isAssignableFrom(params[0])) {
                    // Write method's property type potentially more specific
                    propertyType = params[0];
                } else if (params[0].isAssignableFrom(propertyType)) {
                    // Proceed with read method's property type
                } else {
                    throw new IntrospectionException(
                            "Type mismatch between read and write methods: " + readMethod + " - " + writeMethod);
                }
            } else {
                propertyType = params[0];
            }
        }
        return propertyType;
    }

    /*
     * See IndexedPropertyDescriptor#findIndexedPropertyType
     */
    public static Class<?> findIndexedPropertyType(String name, Class<?> propertyType,
                                                   Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {

        Class<?> indexedPropertyType = null;
        if (indexedReadMethod != null) {
            Class<?> params[] = indexedReadMethod.getParameterTypes();
            if (params.length != 1) {
                throw new IntrospectionException("Bad indexed read method arg count: " + indexedReadMethod);
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("Non int index to indexed read method: " + indexedReadMethod);
            }
            indexedPropertyType = indexedReadMethod.getReturnType();
            if (indexedPropertyType == Void.TYPE) {
                throw new IntrospectionException("Indexed read method returns void: " + indexedReadMethod);
            }
        }
        if (indexedWriteMethod != null) {
            Class<?> params[] = indexedWriteMethod.getParameterTypes();
            if (params.length != 2) {
                throw new IntrospectionException("Bad indexed write method arg count: " + indexedWriteMethod);
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("Non int index to indexed write method: " + indexedWriteMethod);
            }
            if (indexedPropertyType != null) {
                if (indexedPropertyType.isAssignableFrom(params[1])) {
                    // Write method's property type potentially more specific
                    indexedPropertyType = params[1];
                } else if (params[1].isAssignableFrom(indexedPropertyType)) {
                    // Proceed with read method's property type
                } else {
                    throw new IntrospectionException("Type mismatch between indexed read and write methods: " +
                            indexedReadMethod + " - " + indexedWriteMethod);
                }
            } else {
                indexedPropertyType = params[1];
            }
        }
        if (propertyType != null && (!propertyType.isArray() ||
                propertyType.getComponentType() != indexedPropertyType)) {
            throw new IntrospectionException("Type mismatch between indexed and non-indexed methods: " +
                    indexedReadMethod + " - " + indexedWriteMethod);
        }
        return indexedPropertyType;
    }

    /**
     * Compare the given {@link PropertyDescriptor} against the given {@link Object} and
     * return {@code true} if they are objects are equivalent, i.e. both are {@code
     * PropertyDescriptor}s whose read method, write method, property types, property
     * editor and flags are equivalent.
     *
     * @see PropertyDescriptor#equals(Object)
     */
    public static boolean equals(PropertyDescriptor pd, Object other) {
        if (pd == other) {
            return true;
        }
        if (!(other instanceof PropertyDescriptor)) {
            return false;
        }
        PropertyDescriptor otherPd = (PropertyDescriptor) other;
        if (!compareMethods(pd.getReadMethod(), otherPd.getReadMethod())) {
            return false;
        }
        if (!compareMethods(pd.getWriteMethod(), otherPd.getWriteMethod())) {
            return false;
        }
        return (pd.getPropertyType() == otherPd.getPropertyType() &&
                pd.getPropertyEditorClass() == otherPd.getPropertyEditorClass() &&
                pd.isBound() == otherPd.isBound() && pd.isConstrained() == otherPd.isConstrained());
    }

    /*
     * See PropertyDescriptor#compareMethods
     */
    public static boolean compareMethods(Method a, Method b) {
        if ((a == null) != (b == null)) {
            return false;
        }
        if (a != null) {
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }
}

class PropertyDescriptorComparator implements Comparator<PropertyDescriptor> {

    public int compare(PropertyDescriptor desc1, PropertyDescriptor desc2) {
        String left = desc1.getName();
        String right = desc2.getName();
        for (int i = 0; i < left.length(); i++) {
            if (right.length() == i) {
                return 1;
            }
            int result = left.getBytes()[i] - right.getBytes()[i];
            if (result != 0) {
                return result;
            }
        }
        return left.length() - right.length();
    }
}