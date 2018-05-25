package com.github.datalking.web.servlet.handler;

import com.github.datalking.util.PathMatcher;
import com.github.datalking.web.context.request.WebRequestInterceptor;
import com.github.datalking.web.servlet.HandlerInterceptor;


/**
 * @author yaoo on 4/28/18
 */
public class MappedInterceptor {

    private String[] includePatterns;

    private String[] excludePatterns;

    private HandlerInterceptor interceptor;

    public MappedInterceptor(String[] includePatterns, HandlerInterceptor interceptor) {
        this(includePatterns, null, interceptor);
    }

    public MappedInterceptor(String[] includePatterns, String[] excludePatterns, HandlerInterceptor interceptor) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
        this.interceptor = interceptor;
    }

    public MappedInterceptor(String[] includePatterns, WebRequestInterceptor interceptor) {
        this(includePatterns, null, interceptor);
    }

    public MappedInterceptor(String[] includePatterns, String[] excludePatterns, WebRequestInterceptor interceptor) {
        this(includePatterns, excludePatterns, new WebRequestHandlerInterceptorAdapter(interceptor));
    }

    public String[] getPathPatterns() {
        return this.includePatterns;
    }

    public HandlerInterceptor getInterceptor() {
        return this.interceptor;
    }

    public boolean matches(String lookupPath, PathMatcher pathMatcher) {
        if (this.excludePatterns != null) {
            for (String pattern : this.excludePatterns) {
                if (pathMatcher.match(pattern, lookupPath)) {
                    return false;
                }
            }
        }

        if (this.includePatterns == null) {
            return true;
        } else {
            for (String pattern : this.includePatterns) {
                if (pathMatcher.match(pattern, lookupPath)) {
                    return true;
                }
            }
            return false;
        }
    }
}
