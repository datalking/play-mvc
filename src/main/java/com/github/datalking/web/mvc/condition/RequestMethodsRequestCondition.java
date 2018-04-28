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
public final class RequestMethodsRequestCondition extends AbstractRequestCondition<RequestMethodsRequestCondition> {

    private final Set<RequestMethod> methods;


    /**
     * Create a new instance with the given request methods.
     * @param requestMethods 0 or more HTTP request methods;
     * if, 0 the condition will match to every request
     */
    public RequestMethodsRequestCondition(RequestMethod... requestMethods) {
        this(asList(requestMethods));
    }

    private RequestMethodsRequestCondition(Collection<RequestMethod> requestMethods) {
        this.methods = Collections.unmodifiableSet(new LinkedHashSet<RequestMethod>(requestMethods));
    }


    private static List<RequestMethod> asList(RequestMethod... requestMethods) {
        return (requestMethods != null ? Arrays.asList(requestMethods) : Collections.<RequestMethod>emptyList());
    }


    /**
     * Returns all {@link RequestMethod}s contained in this condition.
     */
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

    /**
     * Returns a new instance with a union of the HTTP request methods
     * from "this" and the "other" instance.
     */
    public RequestMethodsRequestCondition combine(RequestMethodsRequestCondition other) {
        Set<RequestMethod> set = new LinkedHashSet<RequestMethod>(this.methods);
        set.addAll(other.methods);
        return new RequestMethodsRequestCondition(set);
    }

    /**
     * Check if any of the HTTP request methods match the given request and
     * return an instance that contains the matching HTTP request method only.
     * @param request the current request
     * @return the same instance if the condition is empty, a new condition with
     * the matched request method, or {@code null} if no request methods match
     */
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

    /**
     * Returns:
     * <ul>
     * <li>0 if the two conditions contain the same number of HTTP request methods
     * <li>Less than 0 if "this" instance has an HTTP request method but "other" doesn't
     * <li>Greater than 0 "other" has an HTTP request method but "this" doesn't
     * </ul>
     * <p>It is assumed that both instances have been obtained via
     * {@link #getMatchingCondition(HttpServletRequest)} and therefore each instance
     * contains the matching HTTP request method only or is otherwise empty.
     */
    public int compareTo(RequestMethodsRequestCondition other, HttpServletRequest request) {
        return (other.methods.size() - this.methods.size());
    }

}
