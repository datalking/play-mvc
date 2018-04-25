package com.github.datalking.web.servlet;

import com.github.datalking.beans.BeanWrapper;
import com.github.datalking.beans.BeanWrapperImpl;
import com.github.datalking.beans.PropertyValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.HashSet;
import java.util.Set;

/**
 * 获取初始化参数
 *
 * @author yaoo on 4/25/18
 */
public abstract class HttpServletBean extends HttpServlet {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // 作为参数传入servlet的必要属性
    private final Set<String> requiredProperties = new HashSet<>();

    @Override
    public final void init() throws ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing servlet '" + getServletName() + "'");
        }

        // Set bean properties from init parameters.
        try {
            PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
            //BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            BeanWrapper bw = new BeanWrapperImpl(this);
            //ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
            //bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
            //initBeanWrapper(bw);
            bw.setPropertyValues(pvs);
        } catch (Exception ex) {
            logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
            ex.printStackTrace();
        }

        // Let subclasses do whatever initialization they like.
        initServletBean();

        if (logger.isDebugEnabled()) {
            logger.debug("Servlet '" + getServletName() + "' configured successfully");
        }
    }

    protected final void addRequiredProperty(String property) {
        this.requiredProperties.add(property);
    }

    protected void initBeanWrapper(BeanWrapper bw) throws Exception {
    }

    @Override
    public final String getServletName() {
        return (getServletConfig() != null ? getServletConfig().getServletName() : null);
    }

    @Override
    public final ServletContext getServletContext() {
        return (getServletConfig() != null ? getServletConfig().getServletContext() : null);
    }

    protected void initServletBean() throws ServletException {
    }


}
