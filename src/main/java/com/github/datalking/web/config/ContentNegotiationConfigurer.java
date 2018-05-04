package com.github.datalking.web.config;

import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.accept.ContentNegotiationManager;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yaoo on 5/4/18
 */
public class ContentNegotiationConfigurer {

    private final ContentNegotiationManagerFactoryBean factoryBean = new ContentNegotiationManagerFactoryBean();

    private final Map<String, MediaType> mediaTypes = new HashMap<>();

    public ContentNegotiationConfigurer(ServletContext servletContext) {
        this.factoryBean.setServletContext(servletContext);
    }

    public ContentNegotiationConfigurer favorPathExtension(boolean favorPathExtension) {
        this.factoryBean.setFavorPathExtension(favorPathExtension);
        return this;
    }

    public ContentNegotiationConfigurer mediaType(String extension, MediaType mediaType) {
        this.mediaTypes.put(extension, mediaType);
        return this;
    }

    public ContentNegotiationConfigurer mediaTypes(Map<String, MediaType> mediaTypes) {
        if (mediaTypes != null) {
            this.mediaTypes.putAll(mediaTypes);
        }
        return this;
    }

    public ContentNegotiationConfigurer replaceMediaTypes(Map<String, MediaType> mediaTypes) {
        this.mediaTypes.clear();
        mediaTypes(mediaTypes);
        return this;
    }

    public ContentNegotiationConfigurer useJaf(boolean useJaf) {
        this.factoryBean.setUseJaf(useJaf);
        return this;
    }

    public ContentNegotiationConfigurer favorParameter(boolean favorParameter) {
        this.factoryBean.setFavorParameter(favorParameter);
        return this;
    }

    public ContentNegotiationConfigurer parameterName(String parameterName) {
        this.factoryBean.setParameterName(parameterName);
        return this;
    }

    public ContentNegotiationConfigurer ignoreAcceptHeader(boolean ignoreAcceptHeader) {
        this.factoryBean.setIgnoreAcceptHeader(ignoreAcceptHeader);
        return this;
    }

    public ContentNegotiationConfigurer defaultContentType(MediaType defaultContentType) {
        this.factoryBean.setDefaultContentType(defaultContentType);
        return this;
    }

    protected ContentNegotiationManager getContentNegotiationManager() throws Exception {
        if (!this.mediaTypes.isEmpty()) {
            this.factoryBean.addMediaTypes(mediaTypes);
        }
        this.factoryBean.afterPropertiesSet();
        return this.factoryBean.getObject();
    }

}
