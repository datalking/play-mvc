package com.github.datalking.web.support;

import com.github.datalking.common.Ordered;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.BindException;
import java.util.List;

/**
 * @author yaoo on 5/4/18
 */
public class DefaultHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    protected static final Logger pageNotFoundLogger = LoggerFactory.getLogger(PAGE_NOT_FOUND_LOG_CATEGORY);

    public DefaultHandlerExceptionResolver() {
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Object handler,
                                              Exception ex) {

        try {
//            if (ex instanceof NoSuchRequestHandlingMethodException) {
//                return handleNoSuchRequestHandlingMethod((NoSuchRequestHandlingMethodException) ex, request, response,
//                        handler);
//            } else if (ex instanceof HttpRequestMethodNotSupportedException) {
//                return handleHttpRequestMethodNotSupported((HttpRequestMethodNotSupportedException) ex, request,
//                        response, handler);
//            } else if (ex instanceof HttpMediaTypeNotSupportedException) {
//                return handleHttpMediaTypeNotSupported((HttpMediaTypeNotSupportedException) ex, request, response,
//                        handler);
//            } else if (ex instanceof HttpMediaTypeNotAcceptableException) {
//                return handleHttpMediaTypeNotAcceptable((HttpMediaTypeNotAcceptableException) ex, request, response,
//                        handler);
//            } else if (ex instanceof MissingServletRequestParameterException) {
//                return handleMissingServletRequestParameter((MissingServletRequestParameterException) ex, request,
//                        response, handler);
//            } else if (ex instanceof ServletRequestBindingException) {
//                return handleServletRequestBindingException((ServletRequestBindingException) ex, request, response,
//                        handler);
//            } else if (ex instanceof ConversionNotSupportedException) {
//                return handleConversionNotSupported((ConversionNotSupportedException) ex, request, response, handler);
//            } else if (ex instanceof TypeMismatchException) {
//                return handleTypeMismatch((TypeMismatchException) ex, request, response, handler);
//            } else if (ex instanceof HttpMessageNotReadableException) {
//                return handleHttpMessageNotReadable((HttpMessageNotReadableException) ex, request, response, handler);
//            } else if (ex instanceof HttpMessageNotWritableException) {
//                return handleHttpMessageNotWritable((HttpMessageNotWritableException) ex, request, response, handler);
//            } else if (ex instanceof MethodArgumentNotValidException) {
//                return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex, request, response, handler);
//            } else if (ex instanceof MissingServletRequestPartException) {
//                return handleMissingServletRequestPartException((MissingServletRequestPartException) ex, request, response, handler);
//            } else
            if (ex instanceof BindException) {
                return handleBindException((BindException) ex, request, response, handler);
            }
        } catch (Exception handlerException) {
            logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
        }
        return null;
    }

//    protected ModelAndView handleNoSuchRequestHandlingMethod(NoSuchRequestHandlingMethodException ex,
//                                                             HttpServletRequest request,
//                                                             HttpServletResponse response,
//                                                             Object handler) throws IOException {
//
//        pageNotFoundLogger.warn(ex.getMessage());
//        response.sendError(HttpServletResponse.SC_NOT_FOUND);
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
//                                                               HttpServletRequest request,
//                                                               HttpServletResponse response,
//                                                               Object handler) throws IOException {
//
//        pageNotFoundLogger.warn(ex.getMessage());
//        String[] supportedMethods = ex.getSupportedMethods();
//        if (supportedMethods != null) {
//            response.setHeader("Allow", StringUtils.arrayToDelimitedString(supportedMethods, ", "));
//        }
//        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
//                                                           HttpServletRequest request,
//                                                           HttpServletResponse response,
//                                                           Object handler) throws IOException {
//
//        response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
//        List<MediaType> mediaTypes = ex.getSupportedMediaTypes();
//        if (!CollectionUtils.isEmpty(mediaTypes)) {
//            response.setHeader("Accept", MediaType.toString(mediaTypes));
//        }
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
//                                                            HttpServletRequest request,
//                                                            HttpServletResponse response,
//                                                            Object handler) throws IOException {
//
//        response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
//                                                                HttpServletRequest request,
//                                                                HttpServletResponse response,
//                                                                Object handler) throws IOException {
//
//        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleServletRequestBindingException(ServletRequestBindingException ex,
//                                                                HttpServletRequest request,
//                                                                HttpServletResponse response,
//                                                                Object handler) throws IOException {
//
//        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleConversionNotSupported(ConversionNotSupportedException ex,
//                                                        HttpServletRequest request,
//                                                        HttpServletResponse response,
//                                                        Object handler) throws IOException {
//
//        sendServerError(ex, request, response);
//        return new ModelAndView();
//    }

    protected void sendServerError(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setAttribute("javax.servlet.error.exception", ex);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

//    protected ModelAndView handleTypeMismatch(TypeMismatchException ex,
//                                              HttpServletRequest request,
//                                              HttpServletResponse response,
//                                              Object handler) throws IOException {
//
//        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
//                                                        HttpServletRequest request,
//                                                        HttpServletResponse response,
//                                                        Object handler) throws IOException {
//
//        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//        return new ModelAndView();
//    }
//
//    protected ModelAndView handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
//                                                        HttpServletRequest request,
//                                                        HttpServletResponse response,
//                                                        Object handler) throws IOException {
//
//        sendServerError(ex, request, response);
//        return new ModelAndView();
//    }


//    protected ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
//                                                                 HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
//        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//        return new ModelAndView();
//    }

//    protected ModelAndView handleMissingServletRequestPartException(MissingServletRequestPartException ex,
//                                                                    HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
//        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
//        return new ModelAndView();
//    }

    protected ModelAndView handleBindException(BindException ex,
                                               HttpServletRequest request,
                                               HttpServletResponse response,
                                               Object handler) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return new ModelAndView();
    }

}
