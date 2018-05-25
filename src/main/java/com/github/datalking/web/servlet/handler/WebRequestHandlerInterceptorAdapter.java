package com.github.datalking.web.servlet.handler;

import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequestInterceptor;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.servlet.DispatcherServletWebRequest;
import com.github.datalking.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 4/28/18
 */
public class WebRequestHandlerInterceptorAdapter implements HandlerInterceptor {

    private final WebRequestInterceptor requestInterceptor;

    public WebRequestHandlerInterceptorAdapter(WebRequestInterceptor requestInterceptor) {
        Assert.notNull(requestInterceptor, "WebRequestInterceptor must not be null");
        this.requestInterceptor = requestInterceptor;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        this.requestInterceptor.preHandle(new DispatcherServletWebRequest(request, response));
        return true;
    }

    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

        this.requestInterceptor.postHandle(new DispatcherServletWebRequest(request, response),
                (modelAndView != null && !modelAndView.wasCleared() ? modelAndView.getModelMap() : null));
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        this.requestInterceptor.afterCompletion(new DispatcherServletWebRequest(request, response), ex);
    }

    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (this.requestInterceptor instanceof WebRequestInterceptor) {
            WebRequestInterceptor asyncInterceptor = this.requestInterceptor;
            DispatcherServletWebRequest webRequest = new DispatcherServletWebRequest(request, response);
            asyncInterceptor.afterConcurrentHandlingStarted(webRequest);
        }
    }


}
