package com.github.datalking.web.config;

import com.github.datalking.beans.factory.FactoryBean;
import com.github.datalking.beans.factory.InitializingBean;
import com.github.datalking.util.Assert;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.accept.ContentNegotiationStrategy;
import com.github.datalking.web.http.accept.FixedContentNegotiationStrategy;
import com.github.datalking.web.http.accept.HeaderContentNegotiationStrategy;
import com.github.datalking.web.http.accept.ParameterContentNegotiationStrategy;
import com.github.datalking.web.http.accept.PathExtensionContentNegotiationStrategy;
import com.github.datalking.web.http.accept.ServletPathExtensionContentNegotiationStrategy;
import com.github.datalking.web.support.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author yaoo on 5/4/18
 */
public class ContentNegotiationManagerFactoryBean
        implements FactoryBean<ContentNegotiationManager>, ServletContextAware, InitializingBean {

    private boolean favorPathExtension = true;

    private boolean favorParameter = false;

    private boolean ignoreAcceptHeader = false;

    private Map<String, MediaType> mediaTypes = new HashMap<>();

    private Boolean useJaf;

    private String parameterName = "format";

    private MediaType defaultContentType;

    private ContentNegotiationManager contentNegotiationManager;

    private ServletContext servletContext;

    public void setFavorPathExtension(boolean favorPathExtension) {
        this.favorPathExtension = favorPathExtension;
    }

    public void setMediaTypes(Properties mediaTypes) {
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            for (Map.Entry<Object, Object> entry : mediaTypes.entrySet()) {
                String extension = ((String)entry.getKey()).toLowerCase(Locale.ENGLISH);
                this.mediaTypes.put(extension, MediaType.valueOf((String) entry.getValue()));
            }
        }
    }

    public void addMediaType(String fileExtension, MediaType mediaType) {
        this.mediaTypes.put(fileExtension, mediaType);
    }

    public void addMediaTypes(Map<String, MediaType> mediaTypes) {
        if (mediaTypes != null) {
            this.mediaTypes.putAll(mediaTypes);
        }
    }

    public void setUseJaf(boolean useJaf) {
        this.useJaf = useJaf;
    }

    private boolean isUseJafTurnedOff() {
        return (this.useJaf != null && !this.useJaf);
    }

    public void setFavorParameter(boolean favorParameter) {
        this.favorParameter = favorParameter;
    }

    public void setParameterName(String parameterName) {
        Assert.notNull(parameterName, "parameterName is required");
        this.parameterName = parameterName;
    }

    public void setIgnoreAcceptHeader(boolean ignoreAcceptHeader) {
        this.ignoreAcceptHeader = ignoreAcceptHeader;
    }

    public void setDefaultContentType(MediaType defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


    public void afterPropertiesSet() {
        List<ContentNegotiationStrategy> strategies = new ArrayList<>();

        if (this.favorPathExtension) {
            PathExtensionContentNegotiationStrategy strategy;
            if (this.servletContext != null && !isUseJafTurnedOff()) {
                strategy = new ServletPathExtensionContentNegotiationStrategy(this.servletContext, this.mediaTypes);
            }
            else {
                strategy = new PathExtensionContentNegotiationStrategy(this.mediaTypes);
            }
            if (this.useJaf != null) {
                strategy.setUseJaf(this.useJaf);
            }
            strategies.add(strategy);
        }

        if (this.favorParameter) {
            ParameterContentNegotiationStrategy strategy = new ParameterContentNegotiationStrategy(this.mediaTypes);
            strategy.setParameterName(this.parameterName);
            strategies.add(strategy);
        }

        if (!this.ignoreAcceptHeader) {
            strategies.add(new HeaderContentNegotiationStrategy());
        }

        if (this.defaultContentType != null) {
            strategies.add(new FixedContentNegotiationStrategy(this.defaultContentType));
        }

        this.contentNegotiationManager = new ContentNegotiationManager(strategies);
    }


    public ContentNegotiationManager getObject() {
        return this.contentNegotiationManager;
    }

    public Class<?> getObjectType() {
        return ContentNegotiationManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
