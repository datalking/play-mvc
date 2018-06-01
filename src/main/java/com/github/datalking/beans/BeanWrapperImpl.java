package com.github.datalking.beans;

import com.github.datalking.common.convert.TypeConverterDelegate;
import com.github.datalking.common.convert.descriptor.Property;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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

    public void setPropertyValue(PropertyValue pv) {

        Field declaredField = null;

        try {

            declaredField = this.wrappedObject.getClass().getDeclaredField(pv.getName());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        declaredField.setAccessible(true);
        Object value = pv.getValue();

        Class clazz = declaredField.getType();
        String typeName = clazz.getName();

        try {
            switch (typeName) {


                /// 8种基本类型 + string
                case "byte":
                case "java.lang.Byte":
                    declaredField.set(this.wrappedObject, Byte.valueOf(value.toString()));
                    break;
                case "short":
                case "java.lang.Short":
                    declaredField.set(this.wrappedObject, Short.valueOf(value.toString()));
                    break;
                case "int":
                case "java.lang.Integer":
                    declaredField.set(this.wrappedObject, Integer.valueOf(value.toString()));
                    break;
                case "long":
                case "java.lang.Long":
                    declaredField.set(this.wrappedObject, Long.valueOf(value.toString()));
                case "float":
                case "java.lang.Float":
                    declaredField.set(this.wrappedObject, Float.valueOf(value.toString()));
                    break;
                case "double":
                case "java.lang.Double":
                    declaredField.set(this.wrappedObject, Double.valueOf(value.toString()));
                    break;
                case "char":
                case "java.lang.Character":
                    declaredField.set(this.wrappedObject, value);
                    break;
                case "boolean":
                case "java.lang.Boolean":
                    declaredField.set(this.wrappedObject, Boolean.valueOf(value.toString()));
                    break;
                case "java.lang.String":
                    declaredField.set(this.wrappedObject, String.valueOf(value.toString()));
                    break;

                ///默认处理引用类型
                default:
                    declaredField.set(this.wrappedObject, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
