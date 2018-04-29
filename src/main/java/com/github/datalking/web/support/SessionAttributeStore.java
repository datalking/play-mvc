package com.github.datalking.web.support;

import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 4/29/18
 */
public interface SessionAttributeStore {

    void storeAttribute(WebRequest request, String attributeName, Object attributeValue);

    Object retrieveAttribute(WebRequest request, String attributeName);

    void cleanupAttribute(WebRequest request, String attributeName);

}
