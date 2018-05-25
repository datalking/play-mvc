package com.github.datalking.web.support;

import com.github.datalking.util.StringUtils;
import com.github.datalking.web.HttpRequestHandler;
import com.github.datalking.web.context.ServletContextAware;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * servlet容器处理静态资源使用的默认servlet名称
 *
 * @author yaoo on 5/4/18
 */
public class DefaultServletHttpRequestHandler implements HttpRequestHandler, ServletContextAware {

    // Tomcat, Jetty, JBoss,GlassFish 使用的默认servlet名称
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    // Google App Engine 使用的默认Default Servlet name
    private static final String GAE_DEFAULT_SERVLET_NAME = "_ah_default";

    private String defaultServletName;

    private ServletContext servletContext;

    public void setDefaultServletName(String defaultServletName) {
        this.defaultServletName = defaultServletName;
    }

    // 根据容器自动选择默认servlet名称
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        if (!StringUtils.hasText(this.defaultServletName)) {

            if (this.servletContext.getNamedDispatcher(COMMON_DEFAULT_SERVLET_NAME) != null) {

                this.defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
            } else if (this.servletContext.getNamedDispatcher(GAE_DEFAULT_SERVLET_NAME) != null) {

                this.defaultServletName = GAE_DEFAULT_SERVLET_NAME;
            }
//            else if (this.servletContext.getNamedDispatcher(RESIN_DEFAULT_SERVLET_NAME) != null) {
//                this.defaultServletName = RESIN_DEFAULT_SERVLET_NAME;
//            }
//            else if (this.servletContext.getNamedDispatcher(WEBLOGIC_DEFAULT_SERVLET_NAME) != null) {
//                this.defaultServletName = WEBLOGIC_DEFAULT_SERVLET_NAME;
//            }
//            else if (this.servletContext.getNamedDispatcher(WEBSPHERE_DEFAULT_SERVLET_NAME) != null) {
//                this.defaultServletName = WEBSPHERE_DEFAULT_SERVLET_NAME;
//            }
            else {
                throw new IllegalStateException("Unable to locate the default servlet for serving static content.");
            }
        }
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        RequestDispatcher rd = this.servletContext.getNamedDispatcher(this.defaultServletName);

        if (rd == null) {
            throw new IllegalStateException("RequestDispatcher cannot be located for default servlet '" + this.defaultServletName + "'");
        }

        rd.forward(request, response);
    }

}
