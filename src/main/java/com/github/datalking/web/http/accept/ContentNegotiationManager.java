package com.github.datalking.web.http.accept;

import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 视图解析器集合
 */
public class ContentNegotiationManager implements ContentNegotiationStrategy, MediaTypeFileExtensionResolver {

    private static final List<MediaType> MEDIA_TYPE_ALL = Arrays.asList(MediaType.ALL);

    private final List<ContentNegotiationStrategy> contentNegotiationStrategies = new ArrayList<>();

    private final Set<MediaTypeFileExtensionResolver> fileExtensionResolvers = new LinkedHashSet<>();

    public ContentNegotiationManager(ContentNegotiationStrategy... strategies) {
        Assert.notEmpty(strategies, "At least one ContentNegotiationStrategy is expected");
        this.contentNegotiationStrategies.addAll(Arrays.asList(strategies));
        for (ContentNegotiationStrategy strategy : this.contentNegotiationStrategies) {
            if (strategy instanceof MediaTypeFileExtensionResolver) {
                this.fileExtensionResolvers.add((MediaTypeFileExtensionResolver) strategy);
            }
        }
    }

    public ContentNegotiationManager(Collection<ContentNegotiationStrategy> strategies) {
        Assert.notEmpty(strategies.toArray(), "At least one ContentNegotiationStrategy is expected");
        this.contentNegotiationStrategies.addAll(strategies);
        for (ContentNegotiationStrategy strategy : this.contentNegotiationStrategies) {
            if (strategy instanceof MediaTypeFileExtensionResolver) {
                this.fileExtensionResolvers.add((MediaTypeFileExtensionResolver) strategy);
            }
        }
    }

    public ContentNegotiationManager() {
        this(new HeaderContentNegotiationStrategy());
    }

    public void addFileExtensionResolvers(MediaTypeFileExtensionResolver... resolvers) {
        this.fileExtensionResolvers.addAll(Arrays.asList(resolvers));
    }

    public List<MediaType> resolveMediaTypes(WebRequest webRequest) {
        for (ContentNegotiationStrategy strategy : this.contentNegotiationStrategies) {
            List<MediaType> mediaTypes = strategy.resolveMediaTypes(webRequest);
            if (mediaTypes.isEmpty() || mediaTypes.equals(MEDIA_TYPE_ALL)) {
                continue;
            }
            return mediaTypes;
        }
        return Collections.emptyList();
    }

    public List<String> resolveFileExtensions(MediaType mediaType) {
        Set<String> result = new LinkedHashSet<>();
        for (MediaTypeFileExtensionResolver resolver : this.fileExtensionResolvers) {
            result.addAll(resolver.resolveFileExtensions(mediaType));
        }
        return new ArrayList<>(result);
    }

    public List<String> getAllFileExtensions() {
        Set<String> result = new LinkedHashSet<>();
        for (MediaTypeFileExtensionResolver resolver : this.fileExtensionResolvers) {
            result.addAll(resolver.getAllFileExtensions());
        }
        return new ArrayList<>(result);
    }

}
