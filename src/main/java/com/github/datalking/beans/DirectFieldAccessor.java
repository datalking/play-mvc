package com.github.datalking.beans;

import com.github.datalking.common.convert.TypeConverterDelegate;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ReflectionUtils;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoo on 5/10/18
 */
public class DirectFieldAccessor extends AbstractPropertyAccessor {

    private final Object target;

    private final Map<String, Field> fieldMap = new HashMap<>();

    public DirectFieldAccessor(final Object target) {
        Assert.notNull(target, "Target object must not be null");
        this.target = target;
        ReflectionUtils.doWithFields(this.target.getClass(), new ReflectionUtils.FieldCallback() {
            public void doWith(Field field) {
                if (fieldMap.containsKey(field.getName())) {
                    // ignore superclass declarations of fields already found in a subclass
                } else {
                    fieldMap.put(field.getName(), field);
                }
            }
        });
        this.typeConverterDelegate = new TypeConverterDelegate(this, target);
        registerDefaultEditors();
        setExtractOldValueForEditor(true);
    }


    public boolean isReadableProperty(String propertyName) {
        return this.fieldMap.containsKey(propertyName);
    }

    public boolean isWritableProperty(String propertyName) {
        return this.fieldMap.containsKey(propertyName);
    }

    @Override
    public Class<?> getPropertyType(String propertyName) {
        Field field = this.fieldMap.get(propertyName);
        if (field != null) {
            return field.getType();
        }
        return null;
    }

    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) {
        Field field = this.fieldMap.get(propertyName);
        if (field != null) {

            return new TypeDescriptor(field);
        }
        return null;
    }

    @Override
    public Object getPropertyValue(String propertyName) {
        Field field = this.fieldMap.get(propertyName);
        if (field == null) {
//            throw new NotReadablePropertyException(this.target.getClass(), propertyName, "Field '" + propertyName + "' does not exist");
            try {
                throw new Exception("Field '" + propertyName + "' does not exist");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            ReflectionUtils.makeAccessible(field);

            return field.get(this.target);
        } catch (IllegalAccessException ex) {
//            throw new InvalidPropertyException(this.target.getClass(), propertyName, "Field is not accessible", ex);
            try {
                throw new Exception("Field is not accessible", ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void setPropertyValue(String propertyName, Object newValue) {

        Field field = this.fieldMap.get(propertyName);

        if (field == null) {
//            throw new NotWritablePropertyException(this.target.getClass(), propertyName, "Field '" + propertyName + "' does not exist");
            try {
                throw new Exception("Field '" + propertyName + "' does not exist");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Object oldValue = null;
        try {

            ReflectionUtils.makeAccessible(field);
            oldValue = field.get(this.target);

            Object convertedValue = this.typeConverterDelegate.convertIfNecessary(
                    field.getName(), oldValue, newValue, field.getType(), new TypeDescriptor(field));

            field.set(this.target, convertedValue);

//        } catch (ConverterNotFoundException ex) {
        } catch (Exception ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
            ex.printStackTrace();
        }

//        catch (ConversionException ex) {
//            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
//            throw new TypeMismatchException(pce, field.getType(), ex);
//        } catch (IllegalStateException ex) {
//            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
//            throw new ConversionNotSupportedException(pce, field.getType(), ex);
//        } catch (IllegalArgumentException ex) {
//            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, oldValue, newValue);
//            throw new TypeMismatchException(pce, field.getType(), ex);
//        } catch (IllegalAccessException ex) {
//            throw new InvalidPropertyException(this.target.getClass(), propertyName, "Field is not accessible", ex);
//        }
    }


}
