package com.github.datalking.web.mvc;

import java.util.Collection;
import java.util.Map;

/**
 * @author yaoo on 4/29/18
 */
public class ExtendedModelMap extends ModelMap implements Model {

    @Override
    public ExtendedModelMap addAttribute(String attributeName, Object attributeValue) {
        super.addAttribute(attributeName, attributeValue);
        return this;
    }

    @Override
    public ExtendedModelMap addAttribute(Object attributeValue) {
        super.addAttribute(attributeValue);
        return this;
    }

    @Override
    public ExtendedModelMap addAllAttributes(Collection<?> attributeValues) {
        super.addAllAttributes(attributeValues);
        return this;
    }

    @Override
    public ExtendedModelMap addAllAttributes(Map<String, ?> attributes) {
        super.addAllAttributes(attributes);
        return this;
    }

    @Override
    public ExtendedModelMap mergeAttributes(Map<String, ?> attributes) {
        super.mergeAttributes(attributes);
        return this;
    }

    // 返回自身的Map
    @Override
    public Map<String, Object> asMap() {
        return this;
    }

}
