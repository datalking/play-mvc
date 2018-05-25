package com.github.datalking.web.support;

import com.github.datalking.annotation.web.SessionAttributes;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 4/29/18
 */
public class SessionAttributesHandler {

    private final Set<String> attributeNames = new HashSet<>();

    private final Set<Class<?>> attributeTypes = new HashSet<>();

    private final Map<String, Boolean> knownAttributeNames = new ConcurrentHashMap<>(4);

    private final SessionAttributeStore sessionAttributeStore;

    public SessionAttributesHandler(Class<?> handlerType, SessionAttributeStore sessionAttributeStore) {
        Assert.notNull(sessionAttributeStore, "SessionAttributeStore may not be null.");
        this.sessionAttributeStore = sessionAttributeStore;

        SessionAttributes annotation = AnnotationUtils.findAnnotation(handlerType, SessionAttributes.class);
        if (annotation != null) {
            this.attributeNames.addAll(Arrays.asList(annotation.value()));
            this.attributeTypes.addAll(Arrays.<Class<?>>asList(annotation.types()));
        }

        for (String attributeName : this.attributeNames) {
            this.knownAttributeNames.put(attributeName, Boolean.TRUE);
        }
    }

    public boolean hasSessionAttributes() {
        return ((this.attributeNames.size() > 0) || (this.attributeTypes.size() > 0));
    }

    public boolean isHandlerSessionAttribute(String attributeName, Class<?> attributeType) {
        Assert.notNull(attributeName, "Attribute name must not be null");
        if (this.attributeNames.contains(attributeName) || this.attributeTypes.contains(attributeType)) {
            this.knownAttributeNames.put(attributeName, Boolean.TRUE);
            return true;
        } else {
            return false;
        }
    }

    public void storeAttributes(WebRequest request, Map<String, ?> attributes) {
        for (String name : attributes.keySet()) {
            Object value = attributes.get(name);
            Class<?> attrType = (value != null) ? value.getClass() : null;

            if (isHandlerSessionAttribute(name, attrType)) {
                this.sessionAttributeStore.storeAttribute(request, name, value);
            }
        }
    }

    public Map<String, Object> retrieveAttributes(WebRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        for (String name : this.knownAttributeNames.keySet()) {
            Object value = this.sessionAttributeStore.retrieveAttribute(request, name);
            if (value != null) {
                attributes.put(name, value);
            }
        }
        return attributes;
    }

    public void cleanupAttributes(WebRequest request) {
        for (String attributeName : this.knownAttributeNames.keySet()) {
            this.sessionAttributeStore.cleanupAttribute(request, attributeName);
        }
    }

    public Object retrieveAttribute(WebRequest request, String attributeName) {
        return this.sessionAttributeStore.retrieveAttribute(request, attributeName);
    }

}
