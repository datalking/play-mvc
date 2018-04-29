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

    private static final ThreadLocal<RequestAttributes> inheritableRequestAttributesHolder = new NamedInheritableThreadLocal<>("Request context");

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
                throw new IllegalStateException("No thread-bound request found: " +
                        "Are you referring to request attributes outside of an actual web request, " +
                        "or processing a request outside of the originally receiving thread? " +
                        "If you are actually operating within a web request and still receive this message, " +
                        "your code is probably running outside of DispatcherServlet/DispatcherPortlet: " +
                        "In this case, use RequestContextListener or RequestContextFilter to expose the current request.");
            }
        }
        return attributes;
    }


}
