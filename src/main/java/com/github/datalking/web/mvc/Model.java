package com.github.datalking.web.mvc;

import java.util.Collection;
import java.util.Map;

/**
 * @author yaoo on 4/24/18
 */
public interface Model {

    Model addAttribute(String attributeName, Object attributeValue);

    Model addAttribute(Object attributeValue);

    Model addAllAttributes(Collection<?> attributeValues);

    Model addAllAttributes(Map<String, ?> attributes);

    Model mergeAttributes(Map<String, ?> attributes);

    boolean containsAttribute(String attributeName);

    Map<String, Object> asMap();

}
