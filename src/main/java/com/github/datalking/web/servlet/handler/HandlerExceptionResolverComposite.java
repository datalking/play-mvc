package com.github.datalking.web.servlet.handler;

import com.github.datalking.common.Ordered;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * @author yaoo on 5/4/18
 */
public class HandlerExceptionResolverComposite implements HandlerExceptionResolver, Ordered {

    private List<HandlerExceptionResolver> resolvers;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public void setExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        this.resolvers = exceptionResolvers;
    }

    public List<HandlerExceptionResolver> getExceptionResolvers() {
        return Collections.unmodifiableList(resolvers);
    }

    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        if (resolvers != null) {
            for (HandlerExceptionResolver handlerExceptionResolver : resolvers) {
                ModelAndView mav = handlerExceptionResolver.resolveException(request, response, handler, ex);
                if (mav != null) {
                    return mav;
                }
            }
        }
        return null;
    }

}
