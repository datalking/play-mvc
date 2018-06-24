package com.github.datalking.beans;

import com.github.datalking.common.convert.TypeConverterDelegate;
import com.github.datalking.common.convert.descriptor.Property;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.MethodUtils;
import com.github.datalking.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 包装bean属性 实现类
 *
 * @author yaoo on 4/3/18
 */
public class BeanWrapperImpl extends AbstractPropertyAccessor implements BeanWrapper {

    private static final Logger logger = LoggerFactory.getLogger(BeanWrapperImpl.class);

    // 封装的对象
    private Object wrappedObject;

//    Object rootObject;

    private String nestedPath = "";

    private boolean autoGrowNestedPaths = false;

    private int autoGrowCollectionLimit = Integer.MAX_VALUE;

//    private CachedIntrospectionResults cachedIntrospectionResults;

    private Map<String, BeanWrapperImpl> nestedBeanWrappers;

    public BeanWrapperImpl() {
        this(true);
    }

    public BeanWrapperImpl(boolean registerDefaultEditors) {
        if (registerDefaultEditors) {
            registerDefaultEditors();
        }
        this.typeConverterDelegate = new TypeConverterDelegate(this);
    }

    public BeanWrapperImpl(Object o) {
        registerDefaultEditors();
        this.wrappedObject = o;
    }

    public BeanWrapperImpl(Class<?> clazz) {
        registerDefaultEditors();

        Object obj = null;

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

//        setWrappedInstance(BeanUtils.instantiateClass(clazz));
        setWrappedInstance(obj);
    }

    public final Object getWrappedInstance() {
        return this.wrappedObject;
    }

    public void setWrappedInstance(Object object) {
        this.wrappedObject = object;
    }

    public void setBeanInstance(Object object) {
        this.wrappedObject = object;
        //this.rootObject = object;
        //this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
        //setIntrospectionClass(object.getClass());
    }

    public final Class<?> getWrappedClass() {
        return (this.wrappedObject != null ? this.wrappedObject.getClass() : null);
    }

    /**
     * 给bean实例设置属性
     * 调用此方法之前，要先确保属性类型已经经过正确转换
     *
     * @param pvs 类型已转换正确的键值对属性
     */
    @Override
    public void setPropertyValues(PropertyValues pvs) {
        List<PropertyValue> propertyValues = ((MutablePropertyValues) pvs).getPropertyValueList();

        for (PropertyValue pv : propertyValues) {

            setPropertyValue(pv);
        }

    }

    // ======== PropertyAccessor Interface ========
    @Override
    public boolean isReadableProperty(String propertyName) {
        return true;
    }

    @Override
    public boolean isWritableProperty(String propertyName) {
        return true;
    }

    @Override
    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) {

        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(propertyName, wrappedObject.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        if (pd != null) {
            if (pd.getReadMethod() != null || pd.getWriteMethod() != null) {
                return new TypeDescriptor(property(pd));
            }
        }

        return null;
    }

    @Override
    public Object getPropertyValue(String propertyName) {

        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(propertyName, wrappedObject.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        if (pd != null) {
            Method getMethod = pd.getReadMethod();

            Object obj = null;
            try {
                obj = getMethod.invoke(wrappedObject);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            return obj;
        }

        return null;
    }

    @Override
    public void setPropertyValue(String propertyName, Object value) {

    }

    /**
     * 设置pv
     */
    public void setPropertyValue(PropertyValue pv) {

        String pname = pv.getName();
        Object pvalue = pv.getValue();

        Field declaredField = null;
        try {

            // 获取属性名对应的字段
            declaredField = this.wrappedObject.getClass().getDeclaredField(pname);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
        }

        /// 若字段存在，则直接通过反射赋值
        if (declaredField != null) {
            declaredField.setAccessible(true);

            Class clazz = declaredField.getType();
            String typeName = clazz.getName();

            try {
                switch (typeName) {

                    /// 默认转换8种基本类型、string
                    case "byte":
                    case "java.lang.Byte":
                        declaredField.set(this.wrappedObject, Byte.valueOf(pvalue.toString()));
                        break;
                    case "short":
                    case "java.lang.Short":
                        declaredField.set(this.wrappedObject, Short.valueOf(pvalue.toString()));
                        break;
                    case "int":
                    case "java.lang.Integer":
                        declaredField.set(this.wrappedObject, Integer.valueOf(pvalue.toString()));
                        break;
                    case "long":
                    case "java.lang.Long":
                        declaredField.set(this.wrappedObject, Long.valueOf(pvalue.toString()));
                    case "float":
                    case "java.lang.Float":
                        declaredField.set(this.wrappedObject, Float.valueOf(pvalue.toString()));
                        break;
                    case "double":
                    case "java.lang.Double":
                        declaredField.set(this.wrappedObject, Double.valueOf(pvalue.toString()));
                        break;
                    case "char":
                    case "java.lang.Character":
                        declaredField.set(this.wrappedObject, pvalue);
                        break;
                    case "boolean":
                    case "java.lang.Boolean":
                        declaredField.set(this.wrappedObject, Boolean.valueOf(pvalue.toString()));
                        break;
                    case "java.lang.String":
                        declaredField.set(this.wrappedObject, String.valueOf(pvalue.toString()));
                        break;

                    ///默认处理引用类型
                    default:
                        declaredField.set(this.wrappedObject, pvalue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /// 若字段不存在，则检查set方法是否存在，若写方法存在，则反射赋值；否则字段为null
        else {

            Class<?> beanClass = this.wrappedObject.getClass();
            // 该类所有set方法
            Set<Method> setMethods = MethodUtils.getSetMethodsIncludingParent(beanClass);
            // 属性对应的set方法
            Method setMethod = null;

            for (Method m : setMethods) {
                if (pname.equals(StringUtils.getBeanNameFromSetMethod(m.getName()))) {
                    setMethod = m;
                }
            }

            if (setMethod != null) {
                try {

                    setMethod.invoke(this.wrappedObject, pvalue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

        return new PropertyDescriptor[0];
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(String propertyName) {

        PropertyDescriptor pd = null;
        try {
            pd = new PropertyDescriptor(propertyName, this.wrappedObject.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
//        Method readMethod = pd1.getReadMethod(); //获得读取属性值的方法
//        Object retVal = readMethod.invoke(bp);
        return pd;
    }

    /**
     * PropertyDescriptor 转换成 自定义的通用Property
     *
     * @param pd 原PropertyDescriptor
     * @return 转换成的Property
     */
    private Property property(PropertyDescriptor pd) {

        GenericTypeAwarePropertyDescriptor typeAware = (GenericTypeAwarePropertyDescriptor) pd;

        return new Property(typeAware.getBeanClass(), typeAware.getReadMethod(), typeAware.getWriteMethod(), typeAware.getName());
    }


}
