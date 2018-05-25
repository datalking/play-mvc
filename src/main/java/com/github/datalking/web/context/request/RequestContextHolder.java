package com.github.datalking.web.context.request;

import com.github.datalking.common.NamedInheritableThreadLocal;
import com.github.datalking.common.NamedThreadLocal;
import com.github.datalking.web.http.RequestAttributes;

/**
 * 工具类
 *
 * @author yaoo on 4/25/18
 */
public abstract class RequestContextHolder {

    private static final ThreadLocal<RequestAttributes> requestAttributesHolder = new NamedThreadLocal<>("Request attributes");

    private static final ThreadLocal<RequestAttributes> inheritableRequestAttributesHolder =
            new NamedInheritableThreadLocal<>("Request context");

    public static void resetRequestAttributes() {
        requestAttributesHolder.remove();
        inheritableRequestAttributesHolder.remove();
    }

    public static void setRequestAttributes(RequestAttributes attributes) {
        setRequestAttributes(attributes, false);
    }

    public static void setRequestAttributes(RequestAttributes attributes, boolean inheritable) {
        if (attributes == null) {
            resetRequestAttributes();
        } else {
            if (inheritable) {
                inheritableRequestAttributesHolder.set(attributes);
                requestAttributesHolder.remove();
            } else {
                requestAttributesHolder.set(attributes);
                inheritableRequestAttributesHolder.remove();
            }
        }
    }

    public static RequestAttributes getRequestAttributes() {
        RequestAttributes attributes = requestAttributesHolder.get();
        if (attributes == null) {
            attributes = inheritableRequestAttributesHolder.get();
        }
        return attributes;
    }

    public static RequestAttributes currentRequestAttributes() throws IllegalStateException {
        RequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            if (attributes == null) {
                throw new IllegalStateException("No thread-bound request found: ");
            }
        }
        return attributes;
    }


}
