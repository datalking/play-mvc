package com.github.datalking.web.support;

import com.github.datalking.annotation.ModelAttribute;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.Errors;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.BeanUtils;
import com.github.datalking.web.bind.WebDataBinder;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.bind.WebRequestDataBinder;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.mvc.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author yaoo on 5/2/18
 */
public class ModelAttributeMethodProcessor
        implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean annotationNotRequired;

    public ModelAttributeMethodProcessor(boolean annotationNotRequired) {
        this.annotationNotRequired = annotationNotRequired;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(ModelAttribute.class)) {
            return true;
        } else if (this.annotationNotRequired) {
            return !BeanUtils.isSimpleProperty(parameter.getParameterType());
        } else {
            return false;
        }
    }

    public final Object resolveArgument(MethodParameter parameter,
                                        ModelAndViewContainer mavContainer,
                                        WebRequest webRequest,
                                        WebDataBinderFactory binderFactory) throws Exception {

        String name = ModelFactory.getNameForParameter(parameter);
        Object attribute = (mavContainer.containsAttribute(name) ? mavContainer.getModel().get(name) :
                createAttribute(name, parameter, binderFactory, webRequest));

        WebDataBinder binder = binderFactory.createBinder(webRequest, attribute, name);

        if (binder.getTarget() != null) {
            bindRequestParameters(binder, webRequest);
            validateIfApplicable(binder, parameter);
            if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                throw new Exception(binder.getBindingResult()+"");
            }
        }

        Map<String, Object> bindingResultModel = binder.getBindingResult().getModel();
        mavContainer.removeAttributes(bindingResultModel);
        mavContainer.addAllAttributes(bindingResultModel);

        return binder.getTarget();
    }

    protected Object createAttribute(String attributeName,
                                     MethodParameter methodParam,
                                     WebDataBinderFactory binderFactory,
                                     WebRequest request) throws Exception {

        return BeanUtils.instantiateClass(methodParam.getParameterType());
    }

    protected void bindRequestParameters(WebDataBinder binder, WebRequest request) {
        ((WebRequestDataBinder) binder).bind(request);
    }


    protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
        Annotation[] annotations = methodParam.getParameterAnnotations();
        for (Annotation ann : annotations) {
            if (ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object hints = AnnotationUtils.getValue(ann);
//                binder.validate(hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
                break;
            }
        }
    }

    protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter methodParam) {
        int i = methodParam.getParameterIndex();
        Class<?>[] paramTypes = methodParam.getMethod().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
        return !hasBindingResult;
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        if (returnType.getMethodAnnotation(ModelAttribute.class) != null) {
            return true;
        } else if (this.annotationNotRequired) {
            return !BeanUtils.isSimpleProperty(returnType.getParameterType());
        } else {
            return false;
        }
    }

    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest) throws Exception {

        if (returnValue != null) {
            String name = ModelFactory.getNameForReturnValue(returnValue, returnType);
            mavContainer.addAttribute(name, returnValue);
        }
    }

}
