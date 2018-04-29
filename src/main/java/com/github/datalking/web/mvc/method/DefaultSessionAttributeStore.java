package com.github.datalking.web.mvc.method;

import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.support.SessionAttributeStore;

/**
 * @author yaoo on 4/29/18
 */
public class DefaultSessionAttributeStore implements SessionAttributeStore {

    private String attributeNamePrefix = "";

    public void setAttributeNamePrefix(String attributeNamePrefix) {
        this.attributeNamePrefix = (attributeNamePrefix != null ? attributeNamePrefix : "");
    }

    public void storeAttribute(WebRequest request, String attributeName, Object attributeValue) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        Assert.notNull(attributeValue, "Attribute value must not be null");
        String storeAttributeName = getAttributeNameInSession(request, attributeName);
        request.setAttribute(storeAttributeName, attributeValue, WebRequest.SCOPE_SESSION);
    }

    public Object retrieveAttribute(WebRequest request, String attributeName) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        String storeAttributeName = getAttributeNameInSession(request, attributeName);
        return request.getAttribute(storeAttributeName, WebRequest.SCOPE_SESSION);
    }

    public void cleanupAttribute(WebRequest request, String attributeName) {
        Assert.notNull(request, "WebRequest must not be null");
        Assert.notNull(attributeName, "Attribute name must not be null");
        String storeAttributeName = getAttributeNameInSession(request, attributeName);
        request.removeAttribute(storeAttributeName, WebRequest.SCOPE_SESSION);
    }

    protected String getAttributeNameInSession(WebRequest request, String attributeName) {
        return this.attributeNamePrefix + attributeName;
    }

}
