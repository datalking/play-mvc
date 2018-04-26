package com.github.datalking.web.mvc;

import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.support.ContextExposingHttpServletRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author yaoo on 4/26/18
 */
public class InternalResourceView extends AbstractUrlBasedView {

    private boolean alwaysInclude = false;

    private volatile Boolean exposeForwardAttributes;

    private boolean exposeContextBeansAsAttributes = false;

    private Set<String> exposedContextBeanNames;

    private boolean preventDispatchLoop = false;


    public InternalResourceView() {
    }

    public InternalResourceView(String url) {
        super(url);
    }

    public InternalResourceView(String url, boolean alwaysInclude) {
        super(url);
        this.alwaysInclude = alwaysInclude;
    }


    public void setAlwaysInclude(boolean alwaysInclude) {
        this.alwaysInclude = alwaysInclude;
    }

    public void setExposeForwardAttributes(boolean exposeForwardAttributes) {
        this.exposeForwardAttributes = exposeForwardAttributes;
    }


    public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) {
        this.exposeContextBeansAsAttributes = exposeContextBeansAsAttributes;
    }


    public void setExposedContextBeanNames(String... exposedContextBeanNames) {
        this.exposedContextBeanNames = new HashSet<String>(Arrays.asList(exposedContextBeanNames));
    }

    public void setPreventDispatchLoop(boolean preventDispatchLoop) {
        this.preventDispatchLoop = preventDispatchLoop;
    }


    @Override
    protected boolean isContextRequired() {
        return false;
    }


    @Override
    protected void initServletContext(ServletContext sc) {
        if (this.exposeForwardAttributes == null && sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
            this.exposeForwardAttributes = Boolean.TRUE;
        }
    }


    @Override
    protected void renderMergedOutputModel(            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Determine which request handle to expose to the RequestDispatcher.
        HttpServletRequest requestToExpose = getRequestToExpose(request);

        // Expose the model object as request attributes.
        exposeModelAsRequestAttributes(model, requestToExpose);

        // Expose helpers as request attributes, if any.
        exposeHelpers(requestToExpose);

        // Determine the path for the request dispatcher.
        String dispatcherPath = prepareForRendering(requestToExpose, response);

        // Obtain a RequestDispatcher for the target resource (typically a JSP).
        RequestDispatcher rd = getRequestDispatcher(requestToExpose, dispatcherPath);
        if (rd == null) {
            throw new ServletException("Could not get RequestDispatcher for [" + getUrl() +
                    "]: Check that the corresponding file exists within your web application archive!");
        }

        // If already included or response already committed, perform include, else forward.
        if (useInclude(requestToExpose, response)) {
            response.setContentType(getContentType());
            if (logger.isDebugEnabled()) {
                logger.debug("Including resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
            }
            rd.include(requestToExpose, response);
        } else {
            // Note: The forwarded resource is supposed to determine the content type itself.
            exposeForwardRequestAttributes(requestToExpose);
            if (logger.isDebugEnabled()) {
                logger.debug("Forwarding to resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
            }
            rd.forward(requestToExpose, response);
        }
    }


    protected HttpServletRequest getRequestToExpose(HttpServletRequest originalRequest) {
        if (this.exposeContextBeansAsAttributes || this.exposedContextBeanNames != null) {
            return new ContextExposingHttpServletRequest(originalRequest, getWebApplicationContext(), this.exposedContextBeanNames);
        }
        return originalRequest;
    }

    protected void exposeHelpers(HttpServletRequest request) throws Exception {
    }

    protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String path = getUrl();
        if (this.preventDispatchLoop) {
            String uri = request.getRequestURI();
            if (path.startsWith("/") ? uri.equals(path) : uri.equals(StringUtils.applyRelativePath(uri, path))) {
                throw new ServletException("Circular view path [" + path + "]: would dispatch back " + "to the current handler URL [" + uri + "] again. Check your ViewResolver setup! " + "(Hint: This may be the result of an unspecified view, due to default view name generation.)");
            }
        }
        return path;
    }


    protected RequestDispatcher getRequestDispatcher(HttpServletRequest request, String path) {
        return request.getRequestDispatcher(path);
    }


    protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
        return (this.alwaysInclude || WebUtils.isIncludeRequest(request) || response.isCommitted());
    }

    protected void exposeForwardRequestAttributes(HttpServletRequest request) {
        if (this.exposeForwardAttributes != null && this.exposeForwardAttributes) {
            try {
                WebUtils.exposeForwardRequestAttributes(request);
            } catch (Exception ex) {
                // Servlet container rejected to set internal attributes, e.g. on TriFork.
                this.exposeForwardAttributes = Boolean.FALSE;
            }
        }
    }

}
