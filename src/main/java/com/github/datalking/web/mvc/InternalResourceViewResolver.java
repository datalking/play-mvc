package com.github.datalking.web.mvc;

import com.github.datalking.util.ClassUtils;

/**
 * @author yaoo on 4/26/18
 */
public class InternalResourceViewResolver extends UrlBasedViewResolver {

    private static final boolean jstlPresent = ClassUtils.isPresent(
            "javax.servlet.jsp.jstl.core.Config",
            InternalResourceViewResolver.class.getClassLoader());

    private Boolean alwaysInclude;

    public InternalResourceViewResolver() {
        Class<?> viewClass = requiredViewClass();
        if (viewClass.equals(InternalResourceView.class) && jstlPresent) {
            viewClass = JstlView.class;
        }
        setViewClass(viewClass);
    }

    public InternalResourceViewResolver(String prefix, String suffix) {
        this();
        setPrefix(prefix);
        setSuffix(suffix);
    }

    @Override
    protected Class<?> requiredViewClass() {
        return InternalResourceView.class;
    }

    public void setAlwaysInclude(boolean alwaysInclude) {
        this.alwaysInclude = alwaysInclude;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) {
        InternalResourceView view = (InternalResourceView) super.buildView(viewName);
        if (this.alwaysInclude != null) {
            view.setAlwaysInclude(this.alwaysInclude);
        }
        view.setPreventDispatchLoop(true);
        return view;
    }

}
