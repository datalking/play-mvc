package com.github.datalking.web.http.accept;

import com.github.datalking.util.StringUtils;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 存放json > application/json的映射
 */
public abstract class AbstractMappingContentNegotiationStrategy extends MappingMediaTypeFileExtensionResolver
        implements ContentNegotiationStrategy, MediaTypeFileExtensionResolver {

    public AbstractMappingContentNegotiationStrategy(Map<String, MediaType> mediaTypes) {
        super(mediaTypes);
    }

    public List<MediaType> resolveMediaTypes(WebRequest webRequest) {
        String key = getMediaTypeKey(webRequest);
        if (StringUtils.hasText(key)) {
            MediaType mediaType = lookupMediaType(key);
            if (mediaType != null) {
                handleMatch(key, mediaType);
                return Collections.singletonList(mediaType);
            }
            mediaType = handleNoMatch(webRequest, key);
            if (mediaType != null) {
                addMapping(key, mediaType);
                return Collections.singletonList(mediaType);
            }
        }
        return Collections.emptyList();
    }

    protected abstract String getMediaTypeKey(WebRequest request);

    /**
     * Invoked when a matching media type is found in the lookup map.
     */
    protected void handleMatch(String mappingKey, MediaType mediaType) {
    }

    /**
     * Invoked when no matching media type is found in the lookup map.
     * Sub-classes can take further steps to determine the media type.
     */
    protected MediaType handleNoMatch(WebRequest request, String mappingKey) {
        return null;
    }

}
