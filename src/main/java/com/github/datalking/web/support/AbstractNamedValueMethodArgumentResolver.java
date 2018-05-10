package com.github.datalking.web.support;

import com.github.datalking.annotation.ValueConstants;
import com.github.datalking.beans.factory.config.BeanExpressionContext;
import com.github.datalking.beans.factory.config.BeanExpressionResolver;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.Assert;
import com.github.datalking.web.bind.WebDataBinder;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.RequestScope;
import com.github.datalking.web.context.request.WebRequest;

import javax.servlet.ServletException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 4/29/18
 */
public abstract class AbstractNamedValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final ConfigurableBeanFactory configurableBeanFactory;

    private final BeanExpressionContext expressionContext;

    private Map<MethodParameter, NamedValueInfo> namedValueInfoCache = new ConcurrentHashMap<>(256);

    public AbstractNamedValueMethodArgumentResolver(ConfigurableBeanFactory beanFactory) {
        this.configurableBeanFactory = beanFactory;
        this.expressionContext = (beanFactory != null) ? new BeanExpressionContext(beanFactory, new RequestScope()) : null;
    }

    @Override
    public final Object resolveArgument(MethodParameter parameter,
                                        ModelAndViewContainer mavContainer,
                                        WebRequest webRequest,
                                        WebDataBinderFactory binderFactory) throws Exception {
        // 获取参数类型
        Class<?> paramType = parameter.getParameterType();
        // 获取@PathVariable中配置的参数名
        NamedValueInfo namedValueInfo = getNamedValueInfo(parameter);
        // 从request中获取name属性
        Object arg = resolveName(namedValueInfo.name, parameter, webRequest);

        if (arg == null) {
            if (namedValueInfo.defaultValue != null) {

                // 使用默认值
                arg = resolveDefaultValue(namedValueInfo.defaultValue);
            } else if (namedValueInfo.required) {

                // 处理缺失值
                handleMissingValue(namedValueInfo.name, parameter);
            }

            // 处理空值
            arg = handleNullValue(namedValueInfo.name, arg, paramType);

        } else if ("".equals(arg) && (namedValueInfo.defaultValue != null)) {
            arg = resolveDefaultValue(namedValueInfo.defaultValue);
        }

        if (binderFactory != null) {
            // 创建WebDataBinder对象作为类型转换工厂
            WebDataBinder binder = binderFactory.createBinder(webRequest, null, namedValueInfo.name);

            // 执行转换
            arg = binder.convertIfNecessary(arg, paramType, parameter);

        }

        handleResolvedValue(arg, namedValueInfo.name, parameter, mavContainer, webRequest);

        return arg;
    }

    private NamedValueInfo getNamedValueInfo(MethodParameter parameter) {
        NamedValueInfo namedValueInfo = this.namedValueInfoCache.get(parameter);
        if (namedValueInfo == null) {
            namedValueInfo = createNamedValueInfo(parameter);
            namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo);
            this.namedValueInfoCache.put(parameter, namedValueInfo);
        }
        return namedValueInfo;
    }

    protected abstract NamedValueInfo createNamedValueInfo(MethodParameter parameter);

    private NamedValueInfo updateNamedValueInfo(MethodParameter parameter, NamedValueInfo info) {
        String name = info.name;
        if (info.name.length() == 0) {
            name = parameter.getParameterName();
            Assert.notNull(name, "Name for argument type [" + parameter.getParameterType().getName()
                    + "] not available, and parameter name information not found in class file either.");
        }
        String defaultValue = (ValueConstants.DEFAULT_NONE.equals(info.defaultValue) ? null : info.defaultValue);
        return new NamedValueInfo(name, info.required, defaultValue);
    }


    protected abstract Object resolveName(String name, MethodParameter parameter, WebRequest request) throws Exception;

    private Object resolveDefaultValue(String defaultValue) {
        if (this.configurableBeanFactory == null) {
            return defaultValue;
        }
        String placeholdersResolved = this.configurableBeanFactory.resolveEmbeddedValue(defaultValue);
        BeanExpressionResolver exprResolver = this.configurableBeanFactory.getBeanExpressionResolver();
        if (exprResolver == null) {
            return defaultValue;
        }
        return exprResolver.evaluate(placeholdersResolved, this.expressionContext);
    }

    protected abstract void handleMissingValue(String name, MethodParameter parameter) throws ServletException;

    private Object handleNullValue(String name, Object value, Class<?> paramType) {
        if (value == null) {
            if (Boolean.TYPE.equals(paramType)) {
                return Boolean.FALSE;
            } else if (paramType.isPrimitive()) {
                throw new IllegalStateException("Optional " + paramType + " parameter '" + name +
                        "' is present but cannot be translated into a null value due to being declared as a " +
                        "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
            }
        }
        return value;
    }

    protected void handleResolvedValue(Object arg,
                                       String name,
                                       MethodParameter parameter,
                                       ModelAndViewContainer mavContainer,
                                       WebRequest webRequest) {
    }


    protected static class NamedValueInfo {

        private final String name;

        private final boolean required;

        private final String defaultValue;

        protected NamedValueInfo(String name, boolean required, String defaultValue) {
            this.name = name;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }


}
