package com.github.datalking.web.http.accept;

import com.github.datalking.util.StringUtils;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.MediaType;

import java.util.Collections;
import java.util.List;

public class HeaderContentNegotiationStrategy implements ContentNegotiationStrategy {

    private static final String ACCEPT_HEADER = "Accept";

    public List<MediaType> resolveMediaTypes(WebRequest webRequest) {
        String acceptHeader = webRequest.getHeader(ACCEPT_HEADER);
        try {
            if (StringUtils.hasText(acceptHeader)) {
                List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
//                MediaType.sortBySpecificityAndQuality(mediaTypes);
                return mediaTypes;
            }
        } catch (IllegalArgumentException ex) {
//            throw new HttpMediaTypeNotAcceptableException("Could not parse accept header [" + acceptHeader + "]: " + ex.getMessage());
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

}
