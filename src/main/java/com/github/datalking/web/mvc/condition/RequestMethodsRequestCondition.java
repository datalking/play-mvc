package com.github.datalking.web.mvc.condition;

import com.github.datalking.web.http.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yaoo on 4/28/18
 */
public class RequestMethodsRequestCondition extends AbstractRequestCondition<RequestMethodsRequestCondition> {

    private final Set<RequestMethod> methods;

    public RequestMethodsRequestCondition(RequestMethod... requestMethods) {
        this(asList(requestMethods));
    }

    private RequestMethodsRequestCondition(Collection<RequestMethod> requestMethods) {
        this.methods = Collections.unmodifiableSet(new LinkedHashSet<RequestMethod>(requestMethods));
    }

    private static List<RequestMethod> asList(RequestMethod... requestMethods) {
        return (requestMethods != null ? Arrays.asList(requestMethods) : Collections.<RequestMethod>emptyList());
    }

    public Set<RequestMethod> getMethods() {
        return this.methods;
    }

    @Override
    protected Collection<RequestMethod> getContent() {
        return this.methods;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    public RequestMethodsRequestCondition combine(RequestMethodsRequestCondition other) {
        Set<RequestMethod> set = new LinkedHashSet<>(this.methods);
        set.addAll(other.methods);
        return new RequestMethodsRequestCondition(set);
    }

    public RequestMethodsRequestCondition getMatchingCondition(HttpServletRequest request) {
        if (this.methods.isEmpty()) {
            return this;
        }
        RequestMethod incomingRequestMethod = getRequestMethod(request);
        if (incomingRequestMethod != null) {
            for (RequestMethod method : this.methods) {
                if (method.equals(incomingRequestMethod)) {
                    return new RequestMethodsRequestCondition(method);
                }
            }
        }
        return null;
    }

    private RequestMethod getRequestMethod(HttpServletRequest request) {
        try {
            return RequestMethod.valueOf(request.getMethod());
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public int compareTo(RequestMethodsRequestCondition other, HttpServletRequest request) {
        return (other.methods.size() - this.methods.size());
    }

}
