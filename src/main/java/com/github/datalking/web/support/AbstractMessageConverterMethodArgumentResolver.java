package com.github.datalking.web.support;

import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.ServletServerHttpRequest;
import com.github.datalking.web.http.converter.GenericHttpMessageConverter;
import com.github.datalking.web.http.converter.HttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yaoo on 4/29/18
 */
public abstract class AbstractMessageConverterMethodArgumentResolver implements HandlerMethodArgumentResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final List<HttpMessageConverter<?>> messageConverters;

    protected final List<MediaType> allSupportedMediaTypes;

    public AbstractMessageConverterMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters) {
        Assert.notEmpty(messageConverters.toArray(), "'messageConverters' must not be empty");
        this.messageConverters = messageConverters;
        this.allSupportedMediaTypes = getAllSupportedMediaTypes(messageConverters);
    }

    private static List<MediaType> getAllSupportedMediaTypes(List<HttpMessageConverter<?>> messageConverters) {
        Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<>();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
        }
        List<MediaType> result = new ArrayList<>(allSupportedMediaTypes);
        MediaType.sortBySpecificity(result);
        return Collections.unmodifiableList(result);
    }

    protected <T> Object readWithMessageConverters(WebRequest webRequest,
                                                   MethodParameter methodParam,
                                                   Type paramType) throws IOException {

        HttpInputMessage inputMessage = createInputMessage(webRequest);
        return readWithMessageConverters(inputMessage, methodParam, paramType);
    }

    protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage,
                                                   MethodParameter methodParam,
                                                   Type targetType) throws IOException {

        MediaType contentType = null;
        try {
            contentType = inputMessage.getHeaders().getContentType();
        }catch(Exception e){
            e.printStackTrace();
        }
//        catch (InvalidMediaTypeException ex) {
//            throw new HttpMediaTypeNotSupportedException(ex.getMessage());
//        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        Class<?> contextClass = methodParam.getDeclaringClass();
        Map<TypeVariable, Type> map = GenericTypeResolver.getTypeVariableMap(contextClass);
        Class<T> targetClass = (Class<T>) GenericTypeResolver.resolveType(targetType, map);

        for (HttpMessageConverter<?> converter : this.messageConverters) {

            if (converter instanceof GenericHttpMessageConverter) {
                GenericHttpMessageConverter genericConverter = (GenericHttpMessageConverter) converter;
                if (genericConverter.canRead(targetType, contextClass, contentType)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reading [" + targetType + "] as \"" +
                                contentType + "\" using [" + converter + "]");
                    }
                    return genericConverter.read(targetType, contextClass, inputMessage);
                }
            }

            if (targetClass != null) {
                if (converter.canRead(targetClass, contentType)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reading [" + targetClass.getName() + "] as \"" +
                                contentType + "\" using [" + converter + "]");
                    }
                    return ((HttpMessageConverter<T>) converter).read(targetClass, inputMessage);
                }
            }
        }

//        throw new HttpMediaTypeNotSupportedException(contentType, this.allSupportedMediaTypes);
        try {
            throw new Exception(contentType.toString()+this.allSupportedMediaTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

        protected ServletServerHttpRequest createInputMessage(WebRequest webRequest) {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        return new ServletServerHttpRequest(servletRequest);
    }

}
