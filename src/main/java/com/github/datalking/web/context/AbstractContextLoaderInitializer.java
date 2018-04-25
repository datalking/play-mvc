package com.github.datalking.web.context;

import com.github.datalking.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author yaoo on 4/25/18
 */
public abstract class AbstractContextLoaderInitializer implements WebApplicationInitializer {

    protected abstract WebApplicationContext createRootApplicationContext();

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        registerContextLoaderListener(servletContext);
    }

    protected void registerContextLoaderListener(ServletContext servletContext) {
        WebApplicationContext rootAppContext = createRootApplicationContext();
        if (rootAppContext != null) {
            servletContext.addListener(new ContextLoaderListener(rootAppContext));

        }
        else {
            //logger.debug("No ContextLoaderListener registered, as createRootApplicationContext() did not return an application context");
        }
    }

//    protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
//        return null;
//    }

}
