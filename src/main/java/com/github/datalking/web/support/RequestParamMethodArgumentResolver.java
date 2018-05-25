package com.github.datalking.web.support;

import com.github.datalking.annotation.ValueConstants;
import com.github.datalking.annotation.web.RequestParam;
import com.github.datalking.annotation.web.RequestPart;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.BeanUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.context.request.WebRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 如果controller的方法没有任何参数的注解，但有参数对象而且参数对象的类型是简单类型，
 * 则会默认调用RequestParamMethodArgumentResolver类进行解析
 * 从httpServletRequest中获取parameter，然后调用参数对象的set方法进行注入
 *
 * @author yaoo on 4/29/18
 */
public class RequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    private final boolean useDefaultResolution;

    /**
     * 构造函数
     *
     * @param beanFactory          用于解析 ${...} 占位符
     * @param useDefaultResolution 方法参数类型默认为简单类型 isSimpleProperty
     */
    public RequestParamMethodArgumentResolver(ConfigurableBeanFactory beanFactory, boolean useDefaultResolution) {
        super(beanFactory);
        this.useDefaultResolution = useDefaultResolution;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();

        // 检查方法参数是否有@RequestParam注解
        if (parameter.hasParameterAnnotation(RequestParam.class)) {
            if (Map.class.isAssignableFrom(paramType)) {
                String paramName = parameter.getParameterAnnotation(RequestParam.class).value();
                return StringUtils.hasText(paramName);
            } else {
                return true;
            }
        } else {
            if (parameter.hasParameterAnnotation(RequestPart.class)) {
                return false;
            }
//            else if (MultipartFile.class.equals(paramType) || "javax.servlet.http.Part".equals(paramType.getName())) {
//                return true;
//            }
            else if (this.useDefaultResolution) {
                return BeanUtils.isSimpleProperty(paramType);
            } else {
                return false;
            }
        }
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        return (ann != null ? new RequestParamNamedValueInfo(ann) : new RequestParamNamedValueInfo());
    }

    /**
     * 从webRequest中解析参数
     */
    @Override
    protected Object resolveName(String name, MethodParameter parameter, WebRequest webRequest) throws Exception {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
//        MultipartHttpServletRequest multipartRequest = WebUtils.getNativeRequest(servletRequest, MultipartHttpServletRequest.class);
        Object arg;

//        if (MultipartFile.class.equals(parameter.getParameterType())) {
//            assertIsMultipartRequest(servletRequest);
//            Assert.notNull(multipartRequest, "Expected MultipartHttpServletRequest: is a MultipartResolver configured?");
//            arg = multipartRequest.getFile(name);
//        } else if (isMultipartFileCollection(parameter)) {
//            assertIsMultipartRequest(servletRequest);
//            Assert.notNull(multipartRequest, "Expected MultipartHttpServletRequest: is a MultipartResolver configured?");
//            arg = multipartRequest.getFiles(name);
//        } else if ("javax.servlet.http.Part".equals(parameter.getParameterType().getName())) {
//            assertIsMultipartRequest(servletRequest);
//            arg = servletRequest.getPart(name);
//        } else {
        arg = null;
//            if (multipartRequest != null) {
//                List<MultipartFile> files = multipartRequest.getFiles(name);
//                if (!files.isEmpty()) {
//                    arg = (files.size() == 1 ? files.get(0) : files);
//                }
//            }
        if (arg == null) {
            String[] paramValues = webRequest.getParameterValues(name);
            if (paramValues != null) {
                arg = (paramValues.length == 1 ? paramValues[0] : paramValues);
            }
        }
//        }

        return arg;
    }

//    private void assertIsMultipartRequest(HttpServletRequest request) {
//        String contentType = request.getContentType();
//        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
//            throw new MultipartException("The current request is not a multipart request");
//        }
//    }

//    private boolean isMultipartFileCollection(MethodParameter parameter) {
//        Class<?> paramType = parameter.getParameterType();
//        if (Collection.class.equals(paramType) || List.class.isAssignableFrom(paramType)) {
//            Class<?> valueType = GenericCollectionTypeResolver.getCollectionParameterType(parameter);
//            if (MultipartFile.class.equals(valueType)) {
//                return true;
//            }
//        }
//        return false;
//    }


    @Override
    protected void handleMissingValue(String paramName, MethodParameter parameter) throws ServletException {
//        throw new MissingServletRequestParameterException(paramName, parameter.getParameterType().getSimpleName());
        try {
            throw new Exception(paramName + parameter.getParameterType().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class RequestParamNamedValueInfo extends NamedValueInfo {

        public RequestParamNamedValueInfo() {
            super("", false, ValueConstants.DEFAULT_NONE);
        }

        public RequestParamNamedValueInfo(RequestParam annotation) {
            super(annotation.value(), annotation.required(), annotation.defaultValue());
        }
    }

}
