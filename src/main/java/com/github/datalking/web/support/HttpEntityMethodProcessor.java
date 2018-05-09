package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.Assert;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.HttpEntity;
import com.github.datalking.web.http.HttpHeaders;
import com.github.datalking.web.http.HttpInputMessage;
import com.github.datalking.web.http.ResponseEntity;
import com.github.datalking.web.http.ServletServerHttpRequest;
import com.github.datalking.web.http.ServletServerHttpResponse;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author yaoo on 5/2/18
 */
public class HttpEntityMethodProcessor extends AbstractMessageConverterMethodProcessor {

    public HttpEntityMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    public HttpEntityMethodProcessor(List<HttpMessageConverter<?>> messageConverters,
                                     ContentNegotiationManager contentNegotiationManager) {

        super(messageConverters, contentNegotiationManager);
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return HttpEntity.class.equals(parameter.getParameterType());
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        return HttpEntity.class.isAssignableFrom(returnType.getParameterType());
    }

    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws IOException {

        HttpInputMessage inputMessage = createInputMessage(webRequest);
        Type paramType = getHttpEntityType(parameter);

        Object body = readWithMessageConverters(webRequest, parameter, paramType);
        return new HttpEntity<>(body, inputMessage.getHeaders());
    }

    private Type getHttpEntityType(MethodParameter parameter) {
        Assert.isAssignable(HttpEntity.class, parameter.getParameterType());
        Type parameterType = parameter.getGenericParameterType();
        if (parameterType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) parameterType;
            if (type.getActualTypeArguments().length == 1) {
                return type.getActualTypeArguments()[0];
            }
        }
        throw new IllegalArgumentException("HttpEntity parameter '" + parameter.getParameterName() +
                "' in method " + parameter.getMethod() + " is not parameterized or has more than one parameter");
    }

    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest) throws Exception {

        mavContainer.setRequestHandled(true);
        if (returnValue == null) {
            return;
        }

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        Assert.isInstanceOf(HttpEntity.class, returnValue);
        HttpEntity<?> responseEntity = (HttpEntity<?>) returnValue;
        if (responseEntity instanceof ResponseEntity) {
            outputMessage.setStatusCode(((ResponseEntity<?>) responseEntity).getStatusCode());
        }

        HttpHeaders entityHeaders = responseEntity.getHeaders();
        if (!entityHeaders.isEmpty()) {
            outputMessage.getHeaders().putAll(entityHeaders);
        }

        Object body = responseEntity.getBody();
        if (body != null) {
            writeWithMessageConverters(body, returnType, inputMessage, outputMessage);
        } else {
            // Flush headers to the HttpServletResponse
            outputMessage.getBody();
        }
    }

}
