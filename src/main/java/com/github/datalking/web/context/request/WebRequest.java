package com.github.datalking.web.context.request;

import com.github.datalking.web.http.RequestAttributes;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

/**
 * @author yaoo on 4/28/18
 */
public interface WebRequest extends RequestAttributes {

    Object getNativeRequest();

    <T> T getNativeRequest(Class<T> requiredType);

    Object getNativeResponse();

    <T> T getNativeResponse(Class<T> requiredType);

    String getHeader(String headerName);

    String[] getHeaderValues(String headerName);

    Iterator<String> getHeaderNames();

    String getParameter(String paramName);

    String[] getParameterValues(String paramName);

    Iterator<String> getParameterNames();

    Map<String, String[]> getParameterMap();

    String getContextPath();

    String getRemoteUser();

    Principal getUserPrincipal();

    boolean isUserInRole(String role);

    boolean isSecure();

    boolean checkNotModified(long lastModifiedTimestamp);

    boolean checkNotModified(String etag);

    String getDescription(boolean includeClientInfo);

}
