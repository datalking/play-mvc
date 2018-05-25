package com.github.datalking.web.mvc;

import com.github.datalking.context.MessageSource;
import com.github.datalking.util.web.JstlUtils;
import com.github.datalking.web.support.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author yaoo on 5/7/18
 */
public class JstlView extends InternalResourceView {

    private MessageSource messageSource;

    public JstlView() {
    }

    public JstlView(String url) {
        super(url);
    }

    public JstlView(String url, MessageSource messageSource) {
        this(url);
        this.messageSource = messageSource;
    }

    @Override
    protected void initServletContext(ServletContext servletContext) {
        if (this.messageSource != null) {
            this.messageSource = JstlUtils.getJstlAwareMessageSource(servletContext, this.messageSource);
        }
        super.initServletContext(servletContext);
    }

    @Override
    protected void exposeHelpers(HttpServletRequest request) {
        if (this.messageSource != null) {
            JstlUtils.exposeLocalizationContext(request, this.messageSource);
        } else {
            // 默认执行这里
            JstlUtils.exposeLocalizationContext(new RequestContext(request, getServletContext()));
        }
    }

}
