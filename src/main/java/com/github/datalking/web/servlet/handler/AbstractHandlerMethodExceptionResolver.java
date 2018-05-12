package com.github.datalking.web.servlet.handler;

import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 5/3/18
 */
public abstract class AbstractHandlerMethodExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        if (handler == null) {
            return super.shouldApplyTo(request, handler);
        } else if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            handler = handlerMethod.getBean();
            return super.shouldApplyTo(request, handler);
        } else {
            return false;
        }
    }

    @Override
    protected final ModelAndView doResolveException(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    Object handler, Exception ex) {

        return doResolveHandlerMethodException(request, response, (HandlerMethod) handler, ex);
    }

    protected abstract ModelAndView doResolveHandlerMethodException(HttpServletRequest request,
                                                                    HttpServletResponse response,
                                                                    HandlerMethod handlerMethod,
                                                                    Exception ex);

}
