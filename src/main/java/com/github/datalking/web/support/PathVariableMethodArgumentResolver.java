package com.github.datalking.web.support;

import com.github.datalking.annotation.ValueConstants;
import com.github.datalking.annotation.web.PathVariable;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.http.RequestAttributes;
import com.github.datalking.web.mvc.View;
import com.github.datalking.web.servlet.HandlerMapping;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析方法中@PathVariable的参数
 *
 * @author yaoo on 4/29/18
 */
public class PathVariableMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    public PathVariableMethodArgumentResolver() {
        super(null);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查方法参数是否有@PathVariable注解
        if (!parameter.hasParameterAnnotation(PathVariable.class)) {
            return false;
        }

        // 检查@PathVariable配置的值是否为Map类型
        if (Map.class.isAssignableFrom(parameter.getParameterType())) {
            String paramName = parameter.getParameterAnnotation(PathVariable.class).value();
            return StringUtils.hasText(paramName);
        }

        return true;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        PathVariable annotation = parameter.getParameterAnnotation(PathVariable.class);
        return new PathVariableNamedValueInfo(annotation);
    }

    // 从request中获取name属性
    @Override
    protected Object resolveName(String name, MethodParameter parameter, WebRequest request) throws Exception {
        Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST);

        return (uriTemplateVars != null) ? uriTemplateVars.get(name) : null;
    }

    @Override
    protected void handleMissingValue(String name, MethodParameter param) throws ServletException {
        String paramType = param.getParameterType().getName();
        try {
            throw new Exception("Missing URI template variable '" + name + "' for method param type [" + paramType + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleResolvedValue(Object arg, String name,
                                       MethodParameter parameter,
                                       ModelAndViewContainer mavContainer,
                                       WebRequest request) {

        String key = View.PATH_VARIABLES;
        int scope = RequestAttributes.SCOPE_REQUEST;
        Map<String, Object> pathVars = (Map<String, Object>) request.getAttribute(key, scope);

        if (pathVars == null) {
            pathVars = new HashMap<>();
            request.setAttribute(key, pathVars, scope);
        }

        // 把参数的key-value放进请求域
        pathVars.put(name, arg);
    }

    private static class PathVariableNamedValueInfo extends NamedValueInfo {

        private PathVariableNamedValueInfo(PathVariable annotation) {
            super(annotation.value(), true, ValueConstants.DEFAULT_NONE);
        }
    }

}
