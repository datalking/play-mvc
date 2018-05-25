package com.github.datalking.web.http.accept;


import com.github.datalking.common.LinkedMultiValueMap;
import com.github.datalking.common.MultiValueMap;
import com.github.datalking.web.http.MediaType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 */
public class MappingMediaTypeFileExtensionResolver implements MediaTypeFileExtensionResolver {

    private final ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<>(64);

    private final MultiValueMap<MediaType, String> fileExtensions = new LinkedMultiValueMap<>();

    private final List<String> allFileExtensions = new LinkedList<>();

    public MappingMediaTypeFileExtensionResolver(Map<String, MediaType> mediaTypes) {
        if (mediaTypes != null) {
            for (Entry<String, MediaType> entries : mediaTypes.entrySet()) {
                String extension = entries.getKey().toLowerCase(Locale.ENGLISH);
                MediaType mediaType = entries.getValue();
                addMapping(extension, mediaType);
            }
        }
    }

    public List<String> resolveFileExtensions(MediaType mediaType) {
        List<String> fileExtensions = this.fileExtensions.get(mediaType);
        return (fileExtensions != null) ? fileExtensions : Collections.emptyList();
    }

    public List<String> getAllFileExtensions() {
        return Collections.unmodifiableList(this.allFileExtensions);
    }

    protected MediaType lookupMediaType(String extension) {
        return this.mediaTypes.get(extension);
    }

    protected void addMapping(String extension, MediaType mediaType) {
        MediaType previous = this.mediaTypes.putIfAbsent(extension, mediaType);
        if (previous == null) {
            this.fileExtensions.add(mediaType, extension);
            this.allFileExtensions.add(extension);
        }
    }

}
