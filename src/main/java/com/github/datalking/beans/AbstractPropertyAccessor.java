package com.github.datalking.beans;

import com.github.datalking.common.convert.TypeConverterSupport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 5/10/18
 */
public abstract class AbstractPropertyAccessor extends TypeConverterSupport implements ConfigurablePropertyAccessor {

    private boolean extractOldValueForEditor = false;

    public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
        this.extractOldValueForEditor = extractOldValueForEditor;
    }

    public boolean isExtractOldValueForEditor() {
        return this.extractOldValueForEditor;
    }

    public void setPropertyValue(PropertyValue pv) {
        setPropertyValue(pv.getName(), pv.getValue());
    }

    public void setPropertyValues(Map<?, ?> map) {
        setPropertyValues(new MutablePropertyValues(map));
    }

    public void setPropertyValues(PropertyValues pvs) {
        setPropertyValues(pvs, false, false);
    }

    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) {
        setPropertyValues(pvs, ignoreUnknown, false);
    }

    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) {

//        List<PropertyAccessException> propertyAccessExceptions = null;
        List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues ?
                ((MutablePropertyValues) pvs).getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));
        for (PropertyValue pv : propertyValues) {
            try {

                setPropertyValue(pv);

            } catch (Exception ex) {
                if (!ignoreUnknown) {
                    throw ex;
                }
            }
        }

    }

    @Override
    public Class<?> getPropertyType(String propertyPath) {
        return null;
    }

}
