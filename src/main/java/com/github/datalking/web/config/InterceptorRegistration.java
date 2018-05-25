package com.github.datalking.web.config;

import com.github.datalking.util.Assert;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.web.servlet.HandlerInterceptor;
import com.github.datalking.web.servlet.handler.MappedInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yaoo on 5/4/18
 */
public class InterceptorRegistration {

    private final HandlerInterceptor interceptor;

    private final List<String> includePatterns = new ArrayList<>();

    private final List<String> excludePatterns = new ArrayList<>();

    public InterceptorRegistration(HandlerInterceptor interceptor) {
        Assert.notNull(interceptor, "Interceptor is required");
        this.interceptor = interceptor;
    }

    public InterceptorRegistration addPathPatterns(String... patterns) {
        this.includePatterns.addAll(Arrays.asList(patterns));
        return this;
    }

    public InterceptorRegistration excludePathPatterns(String... patterns) {
        this.excludePatterns.addAll(Arrays.asList(patterns));
        return this;
    }

    protected Object getInterceptor() {
        if (this.includePatterns.isEmpty()) {
            return this.interceptor;
        }
        return new MappedInterceptor(toArray(this.includePatterns), toArray(this.excludePatterns), interceptor);
    }

    private static String[] toArray(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.toArray(new String[list.size()]);
        }
    }

}
