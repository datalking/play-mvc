package com.github.datalking.web.mvc;

import com.github.datalking.common.Ordered;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.PatternMatchUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author yaoo on 4/26/18
 */
public class UrlBasedViewResolver extends AbstractCachingViewResolver implements Ordered {

    public static final String REDIRECT_URL_PREFIX = "redirect:";

    public static final String FORWARD_URL_PREFIX = "forward:";

    private Class<?> viewClass;

    private String prefix = "";

    private String suffix = "";

    private String contentType;

    private boolean redirectContextRelative = true;

    private boolean redirectHttp10Compatible = true;

    private String requestContextAttribute;

    private final Map<String, Object> staticAttributes = new HashMap<>();

    private Boolean exposePathVariables;

    private String[] viewNames;

    private int order = Integer.MAX_VALUE;

    public void setViewClass(Class<?> viewClass) {
        if (viewClass == null || !requiredViewClass().isAssignableFrom(viewClass)) {
            throw new IllegalArgumentException("Given view class type Mismatch");
        }
        this.viewClass = viewClass;
    }

    protected Class<?> getViewClass() {
        return this.viewClass;
    }

    protected Class<?> requiredViewClass() {
        return AbstractUrlBasedView.class;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isRedirectContextRelative() {
        return redirectContextRelative;
    }

    public void setRedirectContextRelative(boolean redirectContextRelative) {
        this.redirectContextRelative = redirectContextRelative;
    }


    public boolean isRedirectHttp10Compatible() {
        return redirectHttp10Compatible;
    }

    public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
        this.redirectHttp10Compatible = redirectHttp10Compatible;
    }

    public String getRequestContextAttribute() {
        return requestContextAttribute;
    }

    public void setRequestContextAttribute(String requestContextAttribute) {
        this.requestContextAttribute = requestContextAttribute;
    }

    public void setAttributes(Properties props) {
        CollectionUtils.mergePropertiesIntoMap(props, this.staticAttributes);
    }

    public void setAttributesMap(Map<String, ?> attributes) {
        if (attributes != null) {
            this.staticAttributes.putAll(attributes);
        }
    }

    public Map<String, Object> getAttributesMap() {
        return this.staticAttributes;
    }

    public void setExposePathVariables(Boolean exposePathVariables) {
        this.exposePathVariables = exposePathVariables;
    }

    protected Boolean getExposePathVariables() {
        return this.exposePathVariables;
    }

    public void setViewNames(String... viewNames) {
        this.viewNames = viewNames;
    }

    protected String[] getViewNames() {
        return this.viewNames;
    }


    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    @Override
    protected void initApplicationContext() {
        super.initApplicationContext();
        if (getViewClass() == null) {
            throw new IllegalArgumentException("Property 'viewClass' is required");
        }
    }

    @Override
    protected Object getCacheKey(String viewName) {
        return viewName;
    }

    // todo redirect
    @Override
    protected View createView(String viewName) {
        // If this resolver is not supposed to handle the given view,
        // return null to pass on to the next resolver in the chain.
        if (!canHandle(viewName)) {
            return null;
        }
        // Check for special "redirect:" prefix.
        if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
//            String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
//            RedirectView view = new RedirectView(redirectUrl, isRedirectContextRelative(), isRedirectHttp10Compatible());
//            return applyLifecycleMethods(viewName, view);
        }
        // Check for special "forward:" prefix.
        if (viewName.startsWith(FORWARD_URL_PREFIX)) {
            String forwardUrl = viewName.substring(FORWARD_URL_PREFIX.length());
            return new InternalResourceView(forwardUrl);
        }
        // Else fall back to superclass implementation: calling loadView.
        return super.createView(viewName);
    }


    protected boolean canHandle(String viewName) {
        String[] viewNames = getViewNames();
        return (viewNames == null || PatternMatchUtils.simpleMatch(viewNames, viewName));
    }

    @Override
    protected View loadView(String viewName) {
        AbstractUrlBasedView view = buildView(viewName);
        View result = applyLifecycleMethods(viewName, view);
        return result;
    }

    private View applyLifecycleMethods(String viewName, AbstractView view) {
        return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
    }

    protected AbstractUrlBasedView buildView(String viewName) {

        Object obj = null;
        try {
            obj = getViewClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        AbstractUrlBasedView view = (AbstractUrlBasedView) obj;
        if (view != null) {
            view.setUrl(getPrefix() + viewName + getSuffix());
            String contentType = getContentType();
            if (contentType != null) {
                view.setContentType(contentType);
            }
            view.setRequestContextAttribute(getRequestContextAttribute());
            view.setAttributesMap(getAttributesMap());
            Boolean exposePathVariables = getExposePathVariables();
            if (exposePathVariables != null) {
                view.setExposePathVariables(exposePathVariables);
            }
        }

        return view;
    }


}
