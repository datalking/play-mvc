package com.github.datalking.web.context.request;

import com.github.datalking.web.mvc.ModelMap;

/**
 * @author yaoo on 4/28/18
 */
public interface WebRequestInterceptor {

    void preHandle(WebRequest request) throws Exception;

    void postHandle(WebRequest request, ModelMap model) throws Exception;

    void afterCompletion(WebRequest request, Exception ex) throws Exception;

    void afterConcurrentHandlingStarted(WebRequest request);

}
