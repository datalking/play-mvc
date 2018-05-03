package com.github.datalking.web.servlet.handler;

import com.github.datalking.common.Ordered;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.servlet.HandlerExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @author yaoo on 5/3/18
 */
public abstract class AbstractHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    private static final String HEADER_PRAGMA = "Pragma";

    private static final String HEADER_EXPIRES = "Expires";

    private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int order = Ordered.LOWEST_PRECEDENCE;

    private Set<?> mappedHandlers;

    private Class[] mappedHandlerClasses;

    private Logger warnLogger;

    private boolean preventResponseCaching = false;


    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public void setMappedHandlers(Set<?> mappedHandlers) {
        this.mappedHandlers = mappedHandlers;
    }

    public void setMappedHandlerClasses(Class[] mappedHandlerClasses) {
        this.mappedHandlerClasses = mappedHandlerClasses;
    }

    public void setWarnLogCategory(String loggerName) {
        this.warnLogger = LoggerFactory.getLogger(loggerName);
    }

    public void setPreventResponseCaching(boolean preventResponseCaching) {
        this.preventResponseCaching = preventResponseCaching;
    }

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {

        if (shouldApplyTo(request, handler)) {
            // Log exception, both at debug log level and at warn level, if desired.
            if (logger.isDebugEnabled()) {
                logger.debug("Resolving exception from handler [" + handler + "]: " + ex);
            }
            logException(ex, request);
            prepareResponse(ex, response);
            return doResolveException(request, response, handler, ex);
        } else {
            return null;
        }
    }

    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        if (handler != null) {
            if (this.mappedHandlers != null && this.mappedHandlers.contains(handler)) {
                return true;
            }
            if (this.mappedHandlerClasses != null) {
                for (Class handlerClass : this.mappedHandlerClasses) {
                    if (handlerClass.isInstance(handler)) {
                        return true;
                    }
                }
            }
        }
        // Else only apply if there are no explicit handler mappings.
        return (this.mappedHandlers == null && this.mappedHandlerClasses == null);
    }

    protected void logException(Exception ex, HttpServletRequest request) {
        if (this.warnLogger != null && this.warnLogger.isWarnEnabled()) {
            this.warnLogger.warn(buildLogMessage(ex, request), ex);
        }
    }

    protected String buildLogMessage(Exception ex, HttpServletRequest request) {
        return "Handler execution resulted in exception";
    }

    protected void prepareResponse(Exception ex, HttpServletResponse response) {
        if (this.preventResponseCaching) {
            preventCaching(response);
        }
    }

    protected void preventCaching(HttpServletResponse response) {
        response.setHeader(HEADER_PRAGMA, "no-cache");
        response.setDateHeader(HEADER_EXPIRES, 1L);
        response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
        response.addHeader(HEADER_CACHE_CONTROL, "no-store");
    }

    protected abstract ModelAndView doResolveException(HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       Object handler,
                                                       Exception ex);

}
