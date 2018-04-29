package com.github.datalking.web.http.accept;

import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;

import java.util.List;


public interface ContentNegotiationStrategy {

    List<MediaType> resolveMediaTypes(WebRequest webRequest);

}
