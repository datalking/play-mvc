package com.github.datalking.web.context;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.support.ApplicationObjectSupport;
import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.support.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * 便于操作applicationContext和servletContext的超类
 *
 * @author yaoo on 4/26/18
 */
public abstract class WebApplicationObjectSupport extends ApplicationObjectSupport implements ServletContextAware {

    private ServletContext servletContext;

    public final void setServletContext(ServletContext servletContext) {
        if (servletContext != this.servletContext) {
            this.servletContext = servletContext;
            if (servletContext != null) {
                initServletContext(servletContext);
            }
        }
    }

    @Override
    protected boolean isContextRequired() {
        return true;
    }

    @Override
    protected void initApplicationContext(ApplicationContext context) {
        super.initApplicationContext(context);
        if (this.servletContext == null && context instanceof WebApplicationContext) {
            this.servletContext = ((WebApplicationContext) context).getServletContext();
            if (this.servletContext != null) {
                initServletContext(this.servletContext);
            }
        }
    }


    protected void initServletContext(ServletContext servletContext) {
    }

    protected final WebApplicationContext getWebApplicationContext() throws IllegalStateException {
        ApplicationContext ctx = getApplicationContext();
        if (ctx instanceof WebApplicationContext) {
            return (WebApplicationContext) getApplicationContext();
        } else if (isContextRequired()) {
            throw new IllegalStateException("WebApplicationObjectSupport instance [" + this +
                    "] does not run in a WebApplicationContext but in: " + ctx);
        } else {
            return null;
        }
    }

    protected final ServletContext getServletContext() throws IllegalStateException {
        if (this.servletContext != null) {
            return this.servletContext;
        }
        ServletContext servletContext = getWebApplicationContext().getServletContext();
        if (servletContext == null && isContextRequired()) {
            throw new IllegalStateException("WebApplicationObjectSupport instance [" + this +
                    "] does not run within a ServletContext. Make sure the object is fully configured!");
        }
        return servletContext;
    }

    protected final File getTempDir() throws IllegalStateException {
        return WebUtils.getTempDir(getServletContext());
    }

}
