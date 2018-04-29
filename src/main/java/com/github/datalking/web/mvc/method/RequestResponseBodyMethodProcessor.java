package com.github.datalking.web.mvc.method;

import com.github.datalking.annotation.web.RequestBody;
import com.github.datalking.annotation.web.ResponseBody;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.http.converter.HttpMessageConverter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author yaoo on 4/26/18
 */
public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor {

    public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> messageConverters,
                                              ContentNegotiationManager contentNegotiationManager) {

        super(messageConverters, contentNegotiationManager);
    }


    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        return (returnType.getMethodAnnotation(ResponseBody.class) != null);
    }


    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Object arg = readWithMessageConverters(webRequest, parameter, parameter.getGenericParameterType());
        String name = Conventions.getVariableNameForParameter(parameter);
        WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
        if (arg != null) {
            validate(binder, parameter);
        }
        mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
        return arg;
    }


    private void validate(WebDataBinder binder, MethodParameter methodParam) {
        Annotation[] annotations = methodParam.getParameterAnnotations();
        for (Annotation ann : annotations) {
            if (ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = AnnotationUtils.getValue(ann);
                binder.validate(hints instanceof Object[] ? (Object[]) hints : new Object[]{hints});
                BindingResult bindingResult = binder.getBindingResult();
                if (bindingResult.hasErrors() && isBindExceptionRequired(binder, methodParam)) {
                    throw new MethodArgumentNotValidException(methodParam, bindingResult);
                }
                break;
            }
        }
    }


    private boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter methodParam) {
        int i = methodParam.getParameterIndex();
        Class<?>[] paramTypes = methodParam.getMethod().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
        return !hasBindingResult;
    }

    @Override
    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest,
                                                   MethodParameter methodParam,
                                                   Type paramType) throws IOException {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpInputMessage inputMessage = new ServletServerHttpRequest(servletRequest);

        RequestBody ann = methodParam.getParameterAnnotation(RequestBody.class);
        if (!ann.required()) {
            InputStream inputStream = inputMessage.getBody();
            if (inputStream == null) {
                return null;
            } else if (inputStream.markSupported()) {
                inputStream.mark(1);
                if (inputStream.read() == -1) {
                    return null;
                }
                inputStream.reset();
            } else {
                final PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
                int b = pushbackInputStream.read();
                if (b == -1) {
                    return null;
                } else {
                    pushbackInputStream.unread(b);
                }
                inputMessage = new ServletServerHttpRequest(servletRequest) {
                    @Override
                    public InputStream getBody() {
                        // Form POST should not get here
                        return pushbackInputStream;
                    }
                };
            }
        }

        return super.readWithMessageConverters(inputMessage, methodParam, paramType);
    }

    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException {

        mavContainer.setRequestHandled(true);
        if (returnValue != null) {
            writeWithMessageConverters(returnValue, returnType, webRequest);
        }
    }


}
