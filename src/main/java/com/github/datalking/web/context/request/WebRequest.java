package com.github.datalking.web.context.request;

import com.github.datalking.web.http.RequestAttributes;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

/**
 * @author yaoo on 4/28/18
 */
public interface WebRequest extends RequestAttributes {

    String getHeader(String headerName);

    String[] getHeaderValues(String headerName);

    Iterator<String> getHeaderNames();

    String getParameter(String paramName);


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
