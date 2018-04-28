package com.github.datalking.web.servlet.handler;

import com.github.datalking.common.Ordered;
import com.github.datalking.util.AntPathMatcher;
import com.github.datalking.util.Assert;
import com.github.datalking.util.PathMatcher;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.web.context.WebApplicationObjectSupport;
import com.github.datalking.web.context.request.WebRequestInterceptor;
import com.github.datalking.web.servlet.HandlerExecutionChain;
import com.github.datalking.web.servlet.HandlerInterceptor;
import com.github.datalking.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 4/28/18
 */
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport
        implements HandlerMapping, Ordered {

    // default: same as non-Ordered
    private int order = Integer.MAX_VALUE;

    private Object defaultHandler;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<Object> interceptors = new ArrayList<>();

    private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<>();

    private final List<MappedInterceptor> mappedInterceptors = new ArrayList<>();

    public final void setOrder(int order) {
        this.order = order;
    }

    @Override
    public final int getOrder() {
        return this.order;
    }


    public void setDefaultHandler(Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public Object getDefaultHandler() {
        return this.defaultHandler;
    }


    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
    }


    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
    }

    public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
        this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
    }


    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }


    public UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }


    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }


    public void setInterceptors(Object[] interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }


    /**
     * Initializes the interceptors.
     *
     * @see #extendInterceptors(java.util.List)
     * @see #initInterceptors()
     */
    @Override
    protected void initApplicationContext() {
        extendInterceptors(this.interceptors);
        detectMappedInterceptors(this.mappedInterceptors);
        initInterceptors();
    }


    protected void extendInterceptors(List<Object> interceptors) {
    }


    protected void detectMappedInterceptors(List<MappedInterceptor> mappedInterceptors) {

        Map<String, MappedInterceptor> result = getWebApplicationContext().getBeansOfType(MappedInterceptor.class);
//        mappedInterceptors.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), MappedInterceptor.class, true, false).values());
        mappedInterceptors.addAll(result.values());
    }


    protected void initInterceptors() {
        if (!this.interceptors.isEmpty()) {
            for (int i = 0; i < this.interceptors.size(); i++) {
                Object interceptor = this.interceptors.get(i);
                if (interceptor == null) {
                    throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
                }
                if (interceptor instanceof MappedInterceptor) {
                    this.mappedInterceptors.add((MappedInterceptor) interceptor);
                } else {
                    this.adaptedInterceptors.add(adaptInterceptor(interceptor));
                }
            }
        }
    }


    protected HandlerInterceptor adaptInterceptor(Object interceptor) {
        if (interceptor instanceof HandlerInterceptor) {
            return (HandlerInterceptor) interceptor;
        } else if (interceptor instanceof WebRequestInterceptor) {
            return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
        } else {
            throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
        }
    }

    protected final HandlerInterceptor[] getAdaptedInterceptors() {
        int count = this.adaptedInterceptors.size();
        return (count > 0 ? this.adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null);
    }


    protected final MappedInterceptor[] getMappedInterceptors() {
        int count = this.mappedInterceptors.size();
        return (count > 0 ? this.mappedInterceptors.toArray(new MappedInterceptor[count]) : null);
    }


    public final HandlerExecutionChain getHandler(HttpServletRequest request) {

        Object handler = null;
        try {
            handler = getHandlerInternal(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
        }
        return getHandlerExecutionChain(handler, request);
    }


    protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
                (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
        chain.addInterceptors(getAdaptedInterceptors());

        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
        for (MappedInterceptor mappedInterceptor : this.mappedInterceptors) {
            if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                chain.addInterceptor(mappedInterceptor.getInterceptor());
            }
        }

        return chain;
    }

}
