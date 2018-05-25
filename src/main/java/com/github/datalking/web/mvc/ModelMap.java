package com.github.datalking.web.mvc;

import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定制map用来存放string -> Object
 *
 * @author yaoo on 4/26/18
 */
public class ModelMap extends LinkedHashMap<String, Object> {

    public ModelMap() {
    }

    public ModelMap(String attributeName, Object attributeValue) {
        addAttribute(attributeName, attributeValue);
    }

    public ModelMap(Object attributeValue) {
        addAttribute(attributeValue);
    }

    public ModelMap addAttribute(String attributeName, Object attributeValue) {
        Assert.notNull(attributeName, "Model attribute name must not be null");
        put(attributeName, attributeValue);
        return this;
    }

    public ModelMap addAttribute(Object attributeValue) {
        Assert.notNull(attributeValue, "Model object must not be null");
        if (attributeValue instanceof Collection && ((Collection) attributeValue).isEmpty()) {
            return this;
        }
        return addAttribute(ClassUtils.getCamelCaseNameFromClass(attributeValue.getClass()), attributeValue);
    }

    public ModelMap addAllAttributes(Collection<?> attributeValues) {
        if (attributeValues != null) {
            for (Object attributeValue : attributeValues) {
                addAttribute(attributeValue);
            }
        }
        return this;
    }

    public ModelMap addAllAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            putAll(attributes);
        }
        return this;
    }

    public ModelMap mergeAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                if (!containsKey(key)) {
                    put(key, attributes.get(key));
                }
            }
        }
        return this;
    }

    public boolean containsAttribute(String attributeName) {
        return containsKey(attributeName);
    }

}


