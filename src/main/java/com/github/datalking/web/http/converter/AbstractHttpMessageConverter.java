package com.github.datalking.web.http.converter;

import com.github.datalking.util.Assert;
import com.github.datalking.web.http.HttpHeaders;
import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.HttpOutputMessage;
import com.github.datalking.web.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author yaoo on 4/29/18
 */
public abstract class AbstractHttpMessageConverter<T> implements HttpMessageConverter<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private List<MediaType> supportedMediaTypes = Collections.emptyList();

    protected AbstractHttpMessageConverter() {
    }

    protected AbstractHttpMessageConverter(MediaType supportedMediaType) {
        setSupportedMediaTypes(Collections.singletonList(supportedMediaType));
    }

    protected AbstractHttpMessageConverter(MediaType... supportedMediaTypes) {
        setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
    }

    public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
        Assert.notEmpty(supportedMediaTypes.toArray(), "'supportedMediaTypes' must not be empty");
        this.supportedMediaTypes = new ArrayList<>(supportedMediaTypes);
    }

    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(this.supportedMediaTypes);
    }

    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return supports(clazz) && canRead(mediaType);
    }

    protected boolean canRead(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.includes(mediaType)) {
                return true;
            }
        }
        return false;
    }

    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return supports(clazz) && canWrite(mediaType);
    }

    protected boolean canWrite(MediaType mediaType) {
        if (mediaType == null || MediaType.ALL.equals(mediaType)) {
            return true;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

    public final T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException {
        return readInternal(clazz, inputMessage);
    }

    //    public final void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    public final void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException {

        HttpHeaders headers = outputMessage.getHeaders();
        if (headers.getContentType() == null) {
            MediaType contentTypeToUse = contentType;
            if (contentType == null || contentType.isWildcardType() || contentType.isWildcardSubtype()) {
                contentTypeToUse = getDefaultContentType(t);
            }
            if (contentTypeToUse != null) {
                headers.setContentType(contentTypeToUse);
            }
        }
        if (headers.getContentLength() == -1) {
            Long contentLength = getContentLength(t, headers.getContentType());
            if (contentLength != null) {
                headers.setContentLength(contentLength);
            }
        }
        writeInternal(t, outputMessage);
        outputMessage.getBody().flush();
    }

    protected MediaType getDefaultContentType(T t) throws IOException {
        List<MediaType> mediaTypes = getSupportedMediaTypes();
        return (!mediaTypes.isEmpty() ? mediaTypes.get(0) : null);
    }

    protected Long getContentLength(T t, MediaType contentType) throws IOException {
        return null;
    }

    protected abstract boolean supports(Class<?> clazz);

    //    protected abstract T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException;
    protected abstract T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException;

    //    protected abstract void writeInternal(T t, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException;
    protected abstract void writeInternal(T t, HttpOutputMessage outputMessage) throws IOException;

}
