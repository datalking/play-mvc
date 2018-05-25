package com.github.datalking.web.http.accept;

import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;

import javax.servlet.ServletContext;
import java.util.Map;

public class ServletPathExtensionContentNegotiationStrategy extends PathExtensionContentNegotiationStrategy {

    private final ServletContext servletContext;

    public ServletPathExtensionContentNegotiationStrategy(
            ServletContext servletContext, Map<String, MediaType> mediaTypes) {

        super(mediaTypes);
        Assert.notNull(servletContext, "ServletContext is required!");
        this.servletContext = servletContext;
    }

    public ServletPathExtensionContentNegotiationStrategy(ServletContext servletContext) {
        this(servletContext, null);
    }

    @Override
    protected MediaType handleNoMatch(WebRequest webRequest, String extension) {
        MediaType mediaType = null;
        if (this.servletContext != null) {
            String mimeType = this.servletContext.getMimeType("file." + extension);
            if (StringUtils.hasText(mimeType)) {
                mediaType = MediaType.parseMediaType(mimeType);
            }
        }
        if (mediaType == null || MediaType.APPLICATION_OCTET_STREAM.equals(mediaType)) {
            MediaType superMediaType = super.handleNoMatch(webRequest, extension);
            if (superMediaType != null) {
                mediaType = superMediaType;
            }
        }
        return mediaType;
    }

}
