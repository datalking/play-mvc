package com.github.datalking.web.http.converter;

import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.HttpOutputMessage;
import com.github.datalking.web.http.MediaType;

import java.io.IOException;
import java.util.List;

/**
 * @author yaoo on 4/26/18
 */
public interface HttpMessageConverter<T> {

    boolean canRead(Class<?> clazz, MediaType mediaType);

    boolean canWrite(Class<?> clazz, MediaType mediaType);

    T read(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException;

    void write(T t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException;

    List<MediaType> getSupportedMediaTypes();

}
