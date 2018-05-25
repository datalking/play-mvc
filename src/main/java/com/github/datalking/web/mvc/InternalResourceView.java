package com.github.datalking.web.mvc;

import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.WebUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author yaoo on 4/26/18
 */
public class InternalResourceView extends AbstractUrlBasedView {

    private boolean alwaysInclude = false;

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

    public void setPreventDispatchLoop(boolean preventDispatchLoop) {
        this.preventDispatchLoop = preventDispatchLoop;
    }

    @Override
    protected boolean isContextRequired() {
        return false;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

        // Expose the model object as request attributes.
        exposeModelAsRequestAttributes(model, request);

        // Expose helpers as request attributes, if any.
        exposeHelpers(request);

        // 计算响应文件的路径
        String dispatcherPath = prepareForRendering(request, response);

        // 获取RequestDispatcher
        RequestDispatcher rd = getRequestDispatcher(request, dispatcherPath);
        try {

            if (rd == null) {
                throw new ServletException("Could not get RequestDispatcher for [" + getUrl() +
                        "]: Check that the corresponding file exists within your web application archive!");

            }

            // If already included or response already committed, perform include, else forward.
            if (useInclude(request, response)) {
                response.setContentType(getContentType());
                if (logger.isDebugEnabled()) {
                    logger.debug("Including resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
                }
                rd.include(request, response);
            } else {
                // Note: The forwarded resource is supposed to determine the content type itself.
                if (logger.isDebugEnabled()) {
                    logger.debug("Forwarding to resource [" + getUrl() + "] in InternalResourceView '" + getBeanName() + "'");
                }
                rd.forward(request, response);
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    protected void exposeHelpers(HttpServletRequest request) {
    }

    protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response) {

        String path = getUrl();

        if (this.preventDispatchLoop) {
            String uri = request.getRequestURI();
            if (path.startsWith("/") ? uri.equals(path) : uri.equals(StringUtils.applyRelativePath(uri, path))) {

                try {
                    throw new ServletException("Circular view path [" + path + "]: would dispatch back " +
                            "to the current handler URL [" + uri + "] again. Check your ViewResolver setup! " +
                            "(Hint: This may be the result of an unspecified view, due to default view name generation.)");
                } catch (ServletException e) {
                    e.printStackTrace();
                }
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

}
