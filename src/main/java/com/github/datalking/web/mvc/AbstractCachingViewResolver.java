package com.github.datalking.web.mvc;

import com.github.datalking.web.context.WebApplicationObjectSupport;
import com.github.datalking.web.servlet.ViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 4/26/18
 */
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

    public static final int DEFAULT_CACHE_LIMIT = 1024;

    private static final View UNRESOLVED_VIEW = new View() {
        public String getContentType() {
            return null;
        }

        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
        }
    };

    private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

    private boolean cacheUnresolved = true;

    private final Map<Object, View> viewAccessCache = new ConcurrentHashMap<>(DEFAULT_CACHE_LIMIT);

    private final Map<Object, View> viewCreationCache = new LinkedHashMap<Object, View>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Object, View> eldest) {
            if (size() > getCacheLimit()) {
                viewAccessCache.remove(eldest.getKey());
                return true;
            } else {
                return false;
            }
        }
    };

    public int getCacheLimit() {
        return this.cacheLimit;
    }

    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }

    public void setCache(boolean cache) {
        this.cacheLimit = (cache ? DEFAULT_CACHE_LIMIT : 0);
    }

    // 缓存是否开启
    public boolean isCache() {
        return (this.cacheLimit > 0);
    }

    public void setCacheUnresolved(boolean cacheUnresolved) {
        this.cacheUnresolved = cacheUnresolved;
    }

    public boolean isCacheUnresolved() {
        return this.cacheUnresolved;
    }

    @Override
    public View resolveViewName(String viewName) {
        if (!isCache()) {
            return createView(viewName);
        } else {
            Object cacheKey = getCacheKey(viewName);
            View view = this.viewAccessCache.get(cacheKey);
            if (view == null) {
                synchronized (this.viewCreationCache) {
                    view = this.viewCreationCache.get(cacheKey);
                    if (view == null) {
                        // 调用子类的方法创建JstlView
                        view = createView(viewName);
                        if (view == null && this.cacheUnresolved) {
                            view = UNRESOLVED_VIEW;
                        }
                        if (view != null) {
                            this.viewAccessCache.put(cacheKey, view);
                            this.viewCreationCache.put(cacheKey, view);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Cached view [" + cacheKey + "]");
                            }
                        }
                    }
                }
            }
            return (view != UNRESOLVED_VIEW ? view : null);
        }
    }


    protected Object getCacheKey(String viewName) {
        return viewName + "_" + "locale";
    }

    protected View createView(String viewName) {
        return loadView(viewName);
    }

    protected abstract View loadView(String viewName);


    public void removeFromCache(String viewName) {
        if (!isCache()) {
            logger.warn("View caching is SWITCHED OFF -- removal not necessary");
        } else {
            Object cacheKey = getCacheKey(viewName);
            Object cachedView;
            synchronized (this.viewCreationCache) {
                this.viewAccessCache.remove(cacheKey);
                cachedView = this.viewCreationCache.remove(cacheKey);
            }
            if (logger.isDebugEnabled()) {
                // Some debug output might be useful...
                if (cachedView == null) {
                    logger.debug("No cached instance for view '" + cacheKey + "' was found");
                } else {
                    logger.debug("Cache for view " + cacheKey + " has been cleared");
                }
            }
        }
    }

    public void clearCache() {
        logger.debug("Clearing entire view cache");
        synchronized (this.viewCreationCache) {
            this.viewAccessCache.clear();
            this.viewCreationCache.clear();
        }
    }


}
