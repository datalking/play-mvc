package com.github.datalking.web.mvc;

import com.github.datalking.annotation.ModelAttribute;
import com.github.datalking.common.BindingResult;
import com.github.datalking.common.Conventions;
import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.BeanUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.bind.WebDataBinder;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.mvc.method.HandlerMethod;
import com.github.datalking.web.servlet.InvocableHandlerMethod;
import com.github.datalking.web.support.ModelAndViewContainer;
import com.github.datalking.web.support.SessionAttributesHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 5/2/18
 */
public final class ModelFactory {

    private final List<InvocableHandlerMethod> attributeMethods;

    private final WebDataBinderFactory binderFactory;

    private final SessionAttributesHandler sessionAttributesHandler;

    public ModelFactory(List<InvocableHandlerMethod> attributeMethods,
                        WebDataBinderFactory binderFactory,
                        SessionAttributesHandler sessionAttributesHandler) {

        this.attributeMethods = (attributeMethods != null) ? attributeMethods : new ArrayList<>();
        this.binderFactory = binderFactory;
        this.sessionAttributesHandler = sessionAttributesHandler;
    }

    public void initModel(WebRequest request, ModelAndViewContainer mavContainer, HandlerMethod handlerMethod) throws Exception {

        Map<String, ?> attributesInSession = this.sessionAttributesHandler.retrieveAttributes(request);
        mavContainer.mergeAttributes(attributesInSession);

        invokeModelAttributeMethods(request, mavContainer);

        for (String name : findSessionAttributeArguments(handlerMethod)) {
            if (!mavContainer.containsAttribute(name)) {
                Object value = this.sessionAttributesHandler.retrieveAttribute(request, name);
                if (value == null) {
                    throw new Exception("Expected session attribute '" + name + "'");
                }
                mavContainer.addAttribute(name, value);
            }
        }
    }

    private void invokeModelAttributeMethods(WebRequest request, ModelAndViewContainer mavContainer)
            throws Exception {

        for (InvocableHandlerMethod attrMethod : this.attributeMethods) {
            String modelName = attrMethod.getMethodAnnotation(ModelAttribute.class).value();
            if (mavContainer.containsAttribute(modelName)) {
                continue;
            }

            Object returnValue = attrMethod.invokeForRequest(request, mavContainer);

            if (!attrMethod.isVoid()) {
                String returnValueName = getNameForReturnValue(returnValue, attrMethod.getReturnType());
                if (!mavContainer.containsAttribute(returnValueName)) {
                    mavContainer.addAttribute(returnValueName, returnValue);
                }
            }
        }
    }

    private List<String> findSessionAttributeArguments(HandlerMethod handlerMethod) {
        List<String> result = new ArrayList<>();
        for (MethodParameter param : handlerMethod.getMethodParameters()) {
            if (param.hasParameterAnnotation(ModelAttribute.class)) {
                String name = getNameForParameter(param);
                if (this.sessionAttributesHandler.isHandlerSessionAttribute(name, param.getParameterType())) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    public static String getNameForReturnValue(Object returnValue, MethodParameter returnType) {
        ModelAttribute annot = returnType.getMethodAnnotation(ModelAttribute.class);
        if (annot != null && StringUtils.hasText(annot.value())) {
            return annot.value();
        } else {
            Method method = returnType.getMethod();
            Class<?> resolvedType = GenericTypeResolver.resolveReturnType(method, returnType.getDeclaringClass());
            return Conventions.getVariableNameForReturnType(method, resolvedType, returnValue);
        }
    }

    public static String getNameForParameter(MethodParameter parameter) {
        ModelAttribute annot = parameter.getParameterAnnotation(ModelAttribute.class);
        String attrName = (annot != null) ? annot.value() : null;
        return StringUtils.hasText(attrName) ? attrName : Conventions.getVariableNameForParameter(parameter);
    }

    public void updateModel(WebRequest request, ModelAndViewContainer mavContainer) throws Exception {

        if (mavContainer.getSessionStatus().isComplete()) {
            this.sessionAttributesHandler.cleanupAttributes(request);
        } else {
            this.sessionAttributesHandler.storeAttributes(request, mavContainer.getModel());
        }

        if (!mavContainer.isRequestHandled()) {
            updateBindingResult(request, mavContainer.getModel());
        }
    }

    private void updateBindingResult(WebRequest request, ModelMap model) throws Exception {
        List<String> keyNames = new ArrayList<String>(model.keySet());
        for (String name : keyNames) {
            Object value = model.get(name);

            if (isBindingCandidate(name, value)) {
                String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + name;

                if (!model.containsAttribute(bindingResultKey)) {
                    WebDataBinder dataBinder = binderFactory.createBinder(request, value, name);
                    model.put(bindingResultKey, dataBinder.getBindingResult());
                }
            }
        }
    }

    private boolean isBindingCandidate(String attributeName, Object value) {
        if (attributeName.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
            return false;
        }

        Class<?> attrType = (value != null) ? value.getClass() : null;
        if (this.sessionAttributesHandler.isHandlerSessionAttribute(attributeName, attrType)) {
            return true;
        }

        return (value != null && !value.getClass().isArray() && !(value instanceof Collection) &&
                !(value instanceof Map) && !BeanUtils.isSimpleValueType(value.getClass()));
    }

}
