package com.github.datalking.web.servlet;

import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.web.mvc.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * handler执行链
 * 包含url对应的controller对象和拦截器，可以在此扩展自定义拦截方法
 *
 * @author yaoo on 4/25/18
 */
public class HandlerExecutionChain {

    private static final Logger logger = LoggerFactory.getLogger(HandlerExecutionChain.class);

    // url对应的实际执行对象，容器的任何bean都能成为handler
    private final Object handler;

    // 拦截器方法数组
    private HandlerInterceptor[] interceptors;

    private List<HandlerInterceptor> interceptorList;

    private int interceptorIndex = -1;

    public HandlerExecutionChain(Object handler) {
        this(handler, (HandlerInterceptor[]) null);
    }

    public HandlerExecutionChain(Object handler, HandlerInterceptor... interceptors) {
        if (handler instanceof HandlerExecutionChain) {
            HandlerExecutionChain originalChain = (HandlerExecutionChain) handler;
            this.handler = originalChain.getHandler();
            this.interceptorList = new ArrayList<>();
            CollectionUtils.mergeArrayIntoCollection(originalChain.getInterceptors(), this.interceptorList);
            CollectionUtils.mergeArrayIntoCollection(interceptors, this.interceptorList);
        } else {
            this.handler = handler;
            this.interceptors = interceptors;
        }
    }

    public Object getHandler() {
        return this.handler;
    }

    public void addInterceptor(HandlerInterceptor interceptor) {
        initInterceptorList().add(interceptor);
    }

    public void addInterceptors(HandlerInterceptor... interceptors) {
        if (!ObjectUtils.isEmpty(interceptors)) {
            initInterceptorList().addAll(Arrays.asList(interceptors));
        }
    }

    private List<HandlerInterceptor> initInterceptorList() {
        if (this.interceptorList == null) {
            this.interceptorList = new ArrayList<>();
            if (this.interceptors != null) {
                // An interceptor array specified through the constructor
                this.interceptorList.addAll(Arrays.asList(this.interceptors));
            }
        }
        this.interceptors = null;
        return this.interceptorList;
    }

    public HandlerInterceptor[] getInterceptors() {
        if (this.interceptors == null && this.interceptorList != null) {
            this.interceptors = this.interceptorList.toArray(new HandlerInterceptor[this.interceptorList.size()]);
        }
        return this.interceptors;
    }


    boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerInterceptor[] interceptors = getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = 0; i < interceptors.length; i++) {
                HandlerInterceptor interceptor = interceptors[i];
                if (!interceptor.preHandle(request, response, this.handler)) {
                    triggerAfterCompletion(request, response, null);
                    return false;
                }
                this.interceptorIndex = i;
            }
        }
        return true;
    }


    void applyPostHandle(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) throws Exception {
        HandlerInterceptor[] interceptors = getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = interceptors.length - 1; i >= 0; i--) {
                HandlerInterceptor interceptor = interceptors[i];
                interceptor.postHandle(request, response, this.handler, mv);
            }
        }
    }

    void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex)
            throws Exception {

        HandlerInterceptor[] interceptors = getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = this.interceptorIndex; i >= 0; i--) {
                HandlerInterceptor interceptor = interceptors[i];
                try {
                    interceptor.afterCompletion(request, response, this.handler, ex);
                } catch (Throwable ex2) {
                    logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                }
            }
        }
    }

    void applyAfterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response) {
        HandlerInterceptor[] interceptors = getInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            for (int i = interceptors.length - 1; i >= 0; i--) {

//                if (interceptors[i] instanceof AsyncHandlerInterceptor) {
//                    try {
//                        AsyncHandlerInterceptor asyncInterceptor = (AsyncHandlerInterceptor) interceptors[i];
//                        asyncInterceptor.afterConcurrentHandlingStarted(request, response, this.handler);
//                    } catch (Throwable ex) {
//                        logger.error("Interceptor [" + interceptors[i] + "] failed in afterConcurrentHandlingStarted", ex);
//                    }
//                }

            }
        }
    }

    @Override
    public String toString() {
        if (this.handler == null) {
            return "HandlerExecutionChain with no handler";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("HandlerExecutionChain with handler [").append(this.handler).append("]");
        if (!CollectionUtils.isEmpty(this.interceptorList)) {
            sb.append(" and ").append(this.interceptorList.size()).append(" interceptor");
            if (this.interceptorList.size() > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }


}
