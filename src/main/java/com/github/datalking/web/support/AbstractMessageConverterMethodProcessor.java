package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.CollectionUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.util.web.UrlPathHelper;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.HttpHeaders;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.ServletServerHttpRequest;
import com.github.datalking.web.http.ServletServerHttpResponse;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import com.github.datalking.web.servlet.HandlerMapping;
import com.github.datalking.web.servlet.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author yaoo on 4/29/18
 */
public abstract class AbstractMessageConverterMethodProcessor extends AbstractMessageConverterMethodArgumentResolver
        implements HandlerMethodReturnValueHandler {

    private static final MediaType MEDIA_TYPE_APPLICATION = new MediaType("application");

    private static final UrlPathHelper RAW_URL_PATH_HELPER = new UrlPathHelper();

    private static final UrlPathHelper DECODING_URL_PATH_HELPER = new UrlPathHelper();

    static {
        RAW_URL_PATH_HELPER.setRemoveSemicolonContent(false);
        RAW_URL_PATH_HELPER.setUrlDecode(false);
    }

    // Extensions associated with the built-in message converters
    private static final Set<String> WHITELISTED_EXTENSIONS = new HashSet<String>(Arrays.asList(
            "txt", "text", "json", "xml", "atom", "rss", "png", "jpe", "jpeg", "jpg", "gif", "wbmp", "bmp"));

    private final ContentNegotiationManager contentNegotiationManager;

    private final Set<String> safeExtensions = new HashSet<>();

    protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        this(messageConverters, null);
    }

    protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> messageConverters,
                                                      ContentNegotiationManager manager) {

        super(messageConverters);
        this.contentNegotiationManager = (manager != null ? manager : new ContentNegotiationManager());
        this.safeExtensions.addAll(this.contentNegotiationManager.getAllFileExtensions());
        this.safeExtensions.addAll(WHITELISTED_EXTENSIONS);
    }


    protected ServletServerHttpResponse createOutputMessage(WebRequest webRequest) {
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        return new ServletServerHttpResponse(response);
    }

    protected <T> void writeWithMessageConverters(T returnValue, MethodParameter returnType, WebRequest webRequest) throws IOException {

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);
        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }

    protected <T> void writeWithMessageConverters(T returnValue,
                                                  MethodParameter returnType,
                                                  ServletServerHttpRequest inputMessage,
                                                  ServletServerHttpResponse outputMessage) throws IOException {

        Class<?> returnValueClass = returnValue.getClass();
        HttpServletRequest servletRequest = inputMessage.getServletRequest();
        List<MediaType> requestedMediaTypes = getAcceptableMediaTypes(servletRequest);
        List<MediaType> producibleMediaTypes = getProducibleMediaTypes(servletRequest, returnValueClass);

        Set<MediaType> compatibleMediaTypes = new LinkedHashSet<MediaType>();
        for (MediaType requestedType : requestedMediaTypes) {
            for (MediaType producibleType : producibleMediaTypes) {
                if (requestedType.isCompatibleWith(producibleType)) {
                    compatibleMediaTypes.add(getMostSpecificMediaType(requestedType, producibleType));
                }
            }
        }

        if (compatibleMediaTypes.isEmpty()) {
//            throw new HttpMediaTypeNotAcceptableException(producibleMediaTypes);
        }

        List<MediaType> mediaTypes = new ArrayList<MediaType>(compatibleMediaTypes);
//        MediaType.sortBySpecificityAndQuality(mediaTypes);

        MediaType selectedMediaType = null;
        for (MediaType mediaType : mediaTypes) {
            if (mediaType.isConcrete()) {
                selectedMediaType = mediaType;
                break;
            } else if (mediaType.equals(MediaType.ALL) || mediaType.equals(MEDIA_TYPE_APPLICATION)) {
                selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
                break;
            }
        }

        if (selectedMediaType != null) {
            selectedMediaType = selectedMediaType.removeQualityValue();
            for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
                if (messageConverter.canWrite(returnValueClass, selectedMediaType)) {
                    addContentDispositionHeader(inputMessage, outputMessage);
                    ((HttpMessageConverter<T>) messageConverter).write(returnValue, selectedMediaType, outputMessage);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Written [" + returnValue + "] as \"" + selectedMediaType + "\" using [" +
                                messageConverter + "]");
                    }
                    return;
                }
            }
        }

//        throw new HttpMediaTypeNotAcceptableException(this.allSupportedMediaTypes);

    }

    /**
     * Returns the media types that can be produced:
     * <ul>
     * <li>The producible media types specified in the request mappings, or
     * <li>Media types of configured converters that can write the specific return value, or
     * <li>{@link MediaType#ALL}
     * </ul>
     */
    @SuppressWarnings("unchecked")
    protected List<MediaType> getProducibleMediaTypes(HttpServletRequest request, Class<?> returnValueClass) {
        Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            return new ArrayList<>(mediaTypes);
        } else if (!this.allSupportedMediaTypes.isEmpty()) {
            List<MediaType> result = new ArrayList<>();
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                if (converter.canWrite(returnValueClass, null)) {
                    result.addAll(converter.getSupportedMediaTypes());
                }
            }
            return result;
        } else {
            return Collections.singletonList(MediaType.ALL);
        }
    }

    private List<MediaType> getAcceptableMediaTypes(HttpServletRequest request) {
        List<MediaType> mediaTypes = this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
        return (mediaTypes.isEmpty() ? Collections.singletonList(MediaType.ALL) : mediaTypes);
    }

    private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
        MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);
        return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse);
    }

    private void addContentDispositionHeader(ServletServerHttpRequest request,
                                             ServletServerHttpResponse response) {

        HttpHeaders headers = response.getHeaders();
        if (headers.containsKey("Content-Disposition")) {
            return;
        }

        HttpServletRequest servletRequest = request.getServletRequest();
        String requestUri = RAW_URL_PATH_HELPER.getOriginatingRequestUri(servletRequest);

        int index = requestUri.lastIndexOf('/') + 1;
        String filename = requestUri.substring(index);
        String pathParams = "";

        index = filename.indexOf(';');
        if (index != -1) {
            pathParams = filename.substring(index);
            filename = filename.substring(0, index);
        }

        filename = DECODING_URL_PATH_HELPER.decodeRequestString(servletRequest, filename);
        String ext = StringUtils.getFilenameExtension(filename);

        pathParams = DECODING_URL_PATH_HELPER.decodeRequestString(servletRequest, pathParams);
        String extInPathParams = StringUtils.getFilenameExtension(pathParams);

        if (!safeExtension(servletRequest, ext) || !safeExtension(servletRequest, extInPathParams)) {
            headers.add("Content-Disposition", "inline;filename=f.txt");
        }
    }

    private boolean safeExtension(HttpServletRequest request, String extension) {
        if (!StringUtils.hasText(extension)) {
            return true;
        }
        extension = extension.toLowerCase(Locale.ENGLISH);
        if (this.safeExtensions.contains(extension)) {
            return true;
        }
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern != null && pattern.endsWith("." + extension)) {
            return true;
        }
        if (extension.equals("html")) {
            String name = HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;
            Set<MediaType> mediaTypes = (Set<MediaType>) request.getAttribute(name);
            if (!CollectionUtils.isEmpty(mediaTypes) && mediaTypes.contains(MediaType.TEXT_HTML)) {
                return true;
            }
        }
        return false;
    }


}
