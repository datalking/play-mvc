package com.github.datalking.web.context;

import com.github.datalking.web.WebApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author yaoo on 4/25/18
 */
public abstract class AbstractContextLoaderInitializer implements WebApplicationInitializer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract WebApplicationContext createRootApplicationContext();

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        registerContextLoaderListener(servletContext);
    }

    protected void registerContextLoaderListener(ServletContext servletContext) {
        WebApplicationContext rootAppContext = createRootApplicationContext();
        if (rootAppContext != null) {
            servletContext.addListener(new ContextLoaderListener(rootAppContext));

        } else {
            logger.debug("No ContextLoaderListener registered");
        }
    }

//    protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
//        return null;
//    }

}
