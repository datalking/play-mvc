package com.github.datalking.web.http.accept;

import com.github.datalking.web.http.MediaType;

import java.util.List;

public interface MediaTypeFileExtensionResolver {

    List<String> resolveFileExtensions(MediaType mediaType);

    List<String> getAllFileExtensions();

}
