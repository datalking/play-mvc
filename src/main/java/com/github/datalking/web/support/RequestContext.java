package com.github.datalking.web.support;

import com.github.datalking.util.web.UriTemplate;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.util.web.WebApplicationContextUtils;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.context.WebApplicationContext;
import com.github.datalking.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @author yaoo on 4/26/18
 */
public class RequestContext {

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = RequestContext.class.getName() + ".CONTEXT";

    private static final String REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME = "requestDataValueProcessor";

    private HttpServletRequest request;

    private HttpServletResponse response;

    private Map<String, Object> model;

    private WebApplicationContext webApplicationContext;

    private Locale locale;

    private Boolean defaultHtmlEscape;

    private UrlPathHelper urlPathHelper;

    private RequestDataValueProcessor requestDataValueProcessor;

//    private Map<String, Errors> errorsMap;

    protected RequestContext() {
    }

    public RequestContext(HttpServletRequest request) {
        initContext(request, null, null, null);
    }

    public RequestContext(HttpServletRequest request, ServletContext servletContext) {
        initContext(request, null, servletContext, null);
    }

    public RequestContext(HttpServletRequest request, Map<String, Object> model) {
        initContext(request, null, null, model);
    }


    public RequestContext(HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext servletContext,
                          Map<String, Object> model) {
        initContext(request, response, servletContext, model);
    }

    protected void initContext(HttpServletRequest request,
                               HttpServletResponse response,
                               ServletContext servletContext,
                               Map<String, Object> model) {

        this.request = request;
        this.response = response;
        this.model = model;

        // Fetch WebApplicationContext, either from DispatcherServlet or the root context.
        // ServletContext needs to be specified to be able to fall back to the root context!
        this.webApplicationContext = (WebApplicationContext) request.getAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (this.webApplicationContext == null) {
            this.webApplicationContext = getWebApplicationContext(request, servletContext);
        }


        this.locale = getFallbackLocale();

        // Determine default HTML escape setting from the "defaultHtmlEscape"
        // context-param in web.xml, if any.
        this.defaultHtmlEscape = WebUtils.getDefaultHtmlEscape(this.webApplicationContext.getServletContext());

        this.urlPathHelper = new UrlPathHelper();

        if (this.webApplicationContext.containsBean(REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME)) {
            this.requestDataValueProcessor = (RequestDataValueProcessor) this.webApplicationContext.getBean(REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME);
        }
    }





    protected Locale getFallbackLocale() {
        return getRequest().getLocale();
    }

    protected final HttpServletRequest getRequest() {
        return this.request;
    }

    private WebApplicationContext getWebApplicationContext(ServletRequest request, ServletContext servletContext) throws IllegalStateException {

        WebApplicationContext webApplicationContext = (WebApplicationContext) request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (webApplicationContext == null) {
            if (servletContext == null) {
                throw new IllegalStateException("No WebApplicationContext found: not in a DispatcherServlet request?");
            }
            webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        }
        return webApplicationContext;
    }


    protected final ServletContext getServletContext() {
        return this.webApplicationContext.getServletContext();
    }

    public final WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    public final Map<String, Object> getModel() {
        return this.model;
    }

    public final Locale getLocale() {
        return this.locale;
    }

    public void setDefaultHtmlEscape(boolean defaultHtmlEscape) {
        this.defaultHtmlEscape = defaultHtmlEscape;
    }

    public boolean isDefaultHtmlEscape() {
        return (this.defaultHtmlEscape != null && this.defaultHtmlEscape.booleanValue());
    }

    public Boolean getDefaultHtmlEscape() {
        return this.defaultHtmlEscape;
    }

    public RequestDataValueProcessor getRequestDataValueProcessor() {
        return this.requestDataValueProcessor;
    }

    public String getContextPath() {
        return this.urlPathHelper.getOriginatingContextPath(this.request);
    }

    public String getContextUrl(String relativeUrl) {
        String url = getContextPath() + relativeUrl;
        if (this.response != null) {
            url = this.response.encodeURL(url);
        }
        return url;
    }

    public String getContextUrl(String relativeUrl, Map<String, ?> params) {
        String url = getContextPath() + relativeUrl;
        UriTemplate template = new UriTemplate(url);
        url = template.expand(params).toASCIIString();
        if (this.response != null) {
            url = this.response.encodeURL(url);
        }
        return url;
    }

    public String getRequestUri() {
        return this.urlPathHelper.getOriginatingRequestUri(this.request);
    }

    protected Object getModelObject(String modelName) {
        if (this.model != null) {
            return this.model.get(modelName);
        }
        else {
            return this.request.getAttribute(modelName);
        }
    }






}
