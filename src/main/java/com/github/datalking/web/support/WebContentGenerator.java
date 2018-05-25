package com.github.datalking.web.support;

import com.github.datalking.util.StringUtils;
import com.github.datalking.web.context.WebApplicationObjectSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yaoo on 4/29/18
 */
public abstract class WebContentGenerator extends WebApplicationObjectSupport {

    public static final String METHOD_GET = "GET";

    public static final String METHOD_HEAD = "HEAD";

    public static final String METHOD_POST = "POST";

    private static final String HEADER_PRAGMA = "Pragma";

    private static final String HEADER_EXPIRES = "Expires";

    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    private Set<String> supportedMethods;

    private boolean requireSession = false;

    // Use HTTP 1.0 expires header?
    private boolean useExpiresHeader = true;

    // Use HTTP 1.1 cache-control header?
    private boolean useCacheControlHeader = true;

    // Use HTTP 1.1 cache-control header value "no-store"?
    private boolean useCacheControlNoStore = true;

    private int cacheSeconds = -1;

    private boolean alwaysMustRevalidate = false;

    public WebContentGenerator() {
        this(true);
    }

    public WebContentGenerator(boolean restrictDefaultSupportedMethods) {
        if (restrictDefaultSupportedMethods) {
            this.supportedMethods = new HashSet<>(4);
            this.supportedMethods.add(METHOD_GET);
            this.supportedMethods.add(METHOD_HEAD);
            this.supportedMethods.add(METHOD_POST);
        }
    }

    public WebContentGenerator(String... supportedMethods) {
        this.supportedMethods = new HashSet<>(Arrays.asList(supportedMethods));
    }

    public final void setSupportedMethods(String... methods) {
        if (methods != null) {
            this.supportedMethods = new HashSet<>(Arrays.asList(methods));
        } else {
            this.supportedMethods = null;
        }
    }

    public final String[] getSupportedMethods() {
        return StringUtils.toStringArray(this.supportedMethods);
    }

    public final void setRequireSession(boolean requireSession) {
        this.requireSession = requireSession;
    }

    public final boolean isRequireSession() {
        return this.requireSession;
    }

    public final void setUseExpiresHeader(boolean useExpiresHeader) {
        this.useExpiresHeader = useExpiresHeader;
    }

    public final boolean isUseExpiresHeader() {
        return this.useExpiresHeader;
    }

    public final void setUseCacheControlHeader(boolean useCacheControlHeader) {
        this.useCacheControlHeader = useCacheControlHeader;
    }

    public final boolean isUseCacheControlHeader() {
        return this.useCacheControlHeader;
    }

    public final void setUseCacheControlNoStore(boolean useCacheControlNoStore) {
        this.useCacheControlNoStore = useCacheControlNoStore;
    }

    public final boolean isUseCacheControlNoStore() {
        return this.useCacheControlNoStore;
    }

    public void setAlwaysMustRevalidate(boolean mustRevalidate) {
        this.alwaysMustRevalidate = mustRevalidate;
    }

    public boolean isAlwaysMustRevalidate() {
        return alwaysMustRevalidate;
    }

    public final void setCacheSeconds(int seconds) {
        this.cacheSeconds = seconds;
    }

    public final int getCacheSeconds() {
        return this.cacheSeconds;
    }

    protected final void checkAndPrepare(HttpServletRequest request, HttpServletResponse response, boolean lastModified)
            throws ServletException {

        checkAndPrepare(request, response, this.cacheSeconds, lastModified);
    }

    protected final void checkAndPrepare(
            HttpServletRequest request, HttpServletResponse response, int cacheSeconds, boolean lastModified) throws ServletException {

        // Check whether we should support the request method.
        String method = request.getMethod();
        if (this.supportedMethods != null && !this.supportedMethods.contains(method)) {

//            throw new HttpRequestMethodNotSupportedException(method, StringUtils.toStringArray(this.supportedMethods));
            try {
                throw new Exception(method.toString() + StringUtils.toStringArray(this.supportedMethods));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Check whether a session is required.
        if (this.requireSession) {
            if (request.getSession(false) == null) {

//                throw new HttpSessionRequiredException("Pre-existing session required but none found");
                try {
                    throw new Exception("Pre-existing session required but none found");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Do declarative cache control. Revalidate if the controller supports last-modified.
        applyCacheSeconds(response, cacheSeconds, lastModified);
    }

    protected final void preventCaching(HttpServletResponse response) {
        response.setHeader(HEADER_PRAGMA, "no-cache");
        if (this.useExpiresHeader) {
            // HTTP 1.0 header
            response.setDateHeader(HEADER_EXPIRES, 1L);
        }
        if (this.useCacheControlHeader) {
            // HTTP 1.1 header: "no-cache" is the standard value,
            // "no-store" is necessary to prevent caching on FireFox.
            response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
            if (this.useCacheControlNoStore) {
                response.addHeader(HEADER_CACHE_CONTROL, "no-store");
            }
        }
    }

    protected final void cacheForSeconds(HttpServletResponse response, int seconds) {
        cacheForSeconds(response, seconds, false);
    }

    protected final void cacheForSeconds(HttpServletResponse response, int seconds, boolean mustRevalidate) {
        if (this.useExpiresHeader) {
            // HTTP 1.0 header
            response.setDateHeader(HEADER_EXPIRES, System.currentTimeMillis() + seconds * 1000L);
        }
        if (this.useCacheControlHeader) {
            // HTTP 1.1 header
            String headerValue = "max-age=" + seconds;
            if (mustRevalidate || this.alwaysMustRevalidate) {
                headerValue += ", must-revalidate";
            }
            response.setHeader(HEADER_CACHE_CONTROL, headerValue);
        }
    }

    protected final void applyCacheSeconds(HttpServletResponse response, int seconds) {
        applyCacheSeconds(response, seconds, false);
    }

    protected final void applyCacheSeconds(HttpServletResponse response, int seconds, boolean mustRevalidate) {
        if (seconds > 0) {
            cacheForSeconds(response, seconds, mustRevalidate);
        } else if (seconds == 0) {
            preventCaching(response);
        }
        // Leave caching to the client otherwise.
    }

}
