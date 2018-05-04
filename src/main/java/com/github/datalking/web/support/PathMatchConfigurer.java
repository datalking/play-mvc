package com.github.datalking.web.support;

import com.github.datalking.util.PathMatcher;
import com.github.datalking.util.web.UrlPathHelper;

/**
 * @author yaoo on 5/4/18
 */
public class PathMatchConfigurer {

    private Boolean suffixPatternMatch;

    private Boolean trailingSlashMatch;

    private Boolean registeredSuffixPatternMatch;

    private UrlPathHelper urlPathHelper;

    private PathMatcher pathMatcher;

    public PathMatchConfigurer setUseSuffixPatternMatch(Boolean suffixPatternMatch) {
        this.suffixPatternMatch = suffixPatternMatch;
        return this;
    }

    public PathMatchConfigurer setUseTrailingSlashMatch(Boolean trailingSlashMatch) {
        this.trailingSlashMatch = trailingSlashMatch;
        return this;
    }

    public PathMatchConfigurer setUseRegisteredSuffixPatternMatch(
            Boolean registeredSuffixPatternMatch) {

        this.registeredSuffixPatternMatch = registeredSuffixPatternMatch;
        return this;
    }

    public PathMatchConfigurer setUrlPathHelper(UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
        return this;
    }

    public PathMatchConfigurer setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
        return this;
    }

    public Boolean isUseSuffixPatternMatch() {
        return this.suffixPatternMatch;
    }

    public Boolean isUseTrailingSlashMatch() {
        return this.trailingSlashMatch;
    }

    public Boolean isUseRegisteredSuffixPatternMatch() {
        return this.registeredSuffixPatternMatch;
    }

    public UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

}

