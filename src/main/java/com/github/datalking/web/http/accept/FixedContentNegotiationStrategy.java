package com.github.datalking.web.http.accept;

import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class FixedContentNegotiationStrategy implements ContentNegotiationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(FixedContentNegotiationStrategy.class);

    private final MediaType defaultContentType;

    public FixedContentNegotiationStrategy(MediaType defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public List<MediaType> resolveMediaTypes(WebRequest webRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Requested media types is " + this.defaultContentType + " (based on default MediaType)");
        }
        return Collections.singletonList(this.defaultContentType);
    }

}
