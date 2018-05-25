package com.github.datalking.web.mvc.method;

import com.github.datalking.common.Ordered;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.servlet.HandlerAdapter;
import com.github.datalking.web.support.WebContentGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 4/29/18
 */
public abstract class AbstractHandlerMethodAdapter extends WebContentGenerator implements HandlerAdapter, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE;

    public AbstractHandlerMethodAdapter() {
        // no restriction of HTTP methods by default
        super(false);
    }

    public final boolean supports(Object handler) {
        return (handler instanceof HandlerMethod && supportsInternal((HandlerMethod) handler));
    }

    protected abstract boolean supportsInternal(HandlerMethod handlerMethod);

    public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        return handleInternal(request, response, (HandlerMethod) handler);
    }

    protected abstract ModelAndView handleInternal(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   HandlerMethod handlerMethod)throws Exception;

    public final long getLastModified(HttpServletRequest request, Object handler) {
        return getLastModifiedInternal(request, (HandlerMethod) handler);
    }

    protected abstract long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod);

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

}
