package com.github.datalking.context.support;

import com.github.datalking.context.ApplicationContext;
import com.github.datalking.context.ApplicationContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 便于操作applicationContext的超类
 *
 * @author yaoo on 4/26/18
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    @Override
    public final void setApplicationContext(ApplicationContext context) {
        if (context == null && !isContextRequired()) {
            this.applicationContext = null;
        } else if (this.applicationContext == null) {
            if (!requiredContextClass().isInstance(context)) {
                try {
                    throw new Exception("Invalid application context: needs to be of type [" + requiredContextClass().getName() + "]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.applicationContext = context;
            initApplicationContext(context);
        } else {
            if (this.applicationContext != context) {
                try {
                    throw new Exception("Cannot reinitialize with different application context: current one is [" + this.applicationContext + "], passed-in one is [" + context + "]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected boolean isContextRequired() {
        return false;
    }

    protected Class requiredContextClass() {
        return ApplicationContext.class;
    }

    protected void initApplicationContext(ApplicationContext context) {
        initApplicationContext();
    }


    protected void initApplicationContext() {
    }

    public final ApplicationContext getApplicationContext() throws IllegalStateException {
        if (this.applicationContext == null && isContextRequired()) {
            throw new IllegalStateException("ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
        }
        return this.applicationContext;
    }


}
