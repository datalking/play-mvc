package com.github.datalking.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PropertyValues 默认实现类
 *
 * @author yaoo on 4/3/18
 */
public class MutablePropertyValues implements PropertyValues, Serializable {

    private final List<PropertyValue> propertyValueList;

    private Set<String> processedProperties;

    public MutablePropertyValues() {
        this.propertyValueList = new ArrayList<>(0);
    }

    public MutablePropertyValues(PropertyValues original) {

        if (original != null) {
            PropertyValue[] kvArr = original.getPropertyValues();
            this.propertyValueList = new ArrayList<>(kvArr.length);
            Collections.addAll(this.propertyValueList, kvArr);
//            for (PropertyValue kv : kvArr) {
//                this.propertyValueList.add(kv);
//            }
        } else {
            this.propertyValueList = new ArrayList<>(0);
        }
    }

    public MutablePropertyValues(List<PropertyValue> propertyValueList) {
        this.propertyValueList = (propertyValueList != null ? propertyValueList : new ArrayList<>());
    }

    public MutablePropertyValues(Map<?, ?> original) {
        if (original != null) {
            this.propertyValueList = new ArrayList<>(original.size());
            for (Map.Entry<?, ?> entry : original.entrySet()) {
                this.propertyValueList.add(new PropertyValue(entry.getKey().toString(), entry.getValue()));
            }
        } else {
            this.propertyValueList = new ArrayList<>(0);
        }
    }

    public List<PropertyValue> getPropertyValueList() {
        return this.propertyValueList;
    }

    public int size() {
        return this.propertyValueList.size();
    }

    public MutablePropertyValues addPropertyValues(PropertyValues other) {
        if (other != null) {
            PropertyValue[] pvs = other.getPropertyValues();
            for (PropertyValue pv : pvs) {
                addPropertyValue(new PropertyValue(pv));
            }
        }
        return this;
    }

    public MutablePropertyValues addPropertyValue(String propertyName, Object propertyValue) {
        return addPropertyValue(new PropertyValue(propertyName, propertyValue));
    }

    public MutablePropertyValues add(String propertyName, Object propertyValue) {
        this.addPropertyValue(new PropertyValue(propertyName, propertyValue));
        return this;
    }

    public void registerProcessedProperty(String propertyName) {
        if (this.processedProperties == null) {
            this.processedProperties = new HashSet<>();
        }
        this.processedProperties.add(propertyName);
    }

    public void clearProcessedProperty(String propertyName) {
        if (this.processedProperties != null) {
            this.processedProperties.remove(propertyName);
        }
    }

    public MutablePropertyValues addPropertyValue(PropertyValue kv) {

        //若存在同名，则覆盖
        for (int i = 0, len = this.propertyValueList.size(); i < len; i++) {
            PropertyValue cur = this.propertyValueList.get(i);

            if (cur.getName().equals(kv.getName())) {
                this.propertyValueList.set(i, kv);
                return this;
            }
        }
        //若不存在同名，添加kv
        this.propertyValueList.add(kv);
        return this;
    }

    public void removePropertyValue(PropertyValue pv) {
        this.propertyValueList.remove(pv);
    }

    public void removePropertyValue(String propertyName) {
        this.propertyValueList.remove(getPropertyValue(propertyName));
    }

    @Override
    public PropertyValue[] getPropertyValues() {
        return this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
    }

    @Override
    public PropertyValue getPropertyValue(String propertyName) {

        for (PropertyValue kv : this.propertyValueList) {
            if (kv.getName().equals(propertyName)) {
                return kv;
            }
        }

        return null;
    }

    @Override
    public boolean contains(String propertyName) {
        return getPropertyValue(propertyName) != null;
    }

    @Override
    public boolean isEmpty() {
        return this.propertyValueList.isEmpty();
    }

    @Override
    public String toString() {
        PropertyValue[] pvs = getPropertyValues();
        StringBuilder sb = new StringBuilder("PropertyValues: length=").append(pvs.length);
        return String.valueOf(sb);
    }
}
