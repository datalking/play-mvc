package com.github.datalking.web.mvc.method;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.common.StringValueResolver;
import com.github.datalking.context.EmbeddedValueResolverAware;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.mvc.condition.ConsumesRequestCondition;
import com.github.datalking.web.mvc.condition.HeadersRequestCondition;
import com.github.datalking.web.mvc.condition.ParamsRequestCondition;
import com.github.datalking.web.mvc.condition.PatternsRequestCondition;
import com.github.datalking.web.mvc.condition.ProducesRequestCondition;
import com.github.datalking.web.mvc.condition.RequestCondition;
import com.github.datalking.web.mvc.condition.RequestMethodsRequestCondition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认执行url与bean匹配的类
 *
 * @author yaoo on 4/28/18
 */
public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
        implements EmbeddedValueResolverAware {

    private boolean useSuffixPatternMatch = true;

    private boolean useRegisteredSuffixPatternMatch = false;

    private boolean useTrailingSlashMatch = true;

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private final List<String> fileExtensions = new ArrayList<>();

    private StringValueResolver embeddedValueResolver;

    public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch) {
        this.useSuffixPatternMatch = useSuffixPatternMatch;
    }

    public void setUseRegisteredSuffixPatternMatch(boolean useRegisteredSuffixPatternMatch) {
        this.useRegisteredSuffixPatternMatch = useRegisteredSuffixPatternMatch;
        this.useSuffixPatternMatch = (useRegisteredSuffixPatternMatch || this.useSuffixPatternMatch);
    }

    public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
        this.useTrailingSlashMatch = useTrailingSlashMatch;
    }

    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        Assert.notNull(contentNegotiationManager, "ContentNegotiationManager must not be null");
        this.contentNegotiationManager = contentNegotiationManager;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    /**
     * 这里扫描bean中requestMapping配置的url与方法
     */
    @Override
    public void afterPropertiesSet() {
        if (this.useRegisteredSuffixPatternMatch) {
            this.fileExtensions.addAll(this.contentNegotiationManager.getAllFileExtensions());
        }
        super.afterPropertiesSet();
    }


    public boolean useSuffixPatternMatch() {
        return this.useSuffixPatternMatch;
    }

    public boolean useRegisteredSuffixPatternMatch() {
        return this.useRegisteredSuffixPatternMatch;
    }

    public boolean useTrailingSlashMatch() {
        return this.useTrailingSlashMatch;
    }

    public ContentNegotiationManager getContentNegotiationManager() {
        return this.contentNegotiationManager;
    }

    public List<String> getFileExtensions() {
        return this.fileExtensions;
    }

    /**
     * 判断bean是否是handlerMapping的bean
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
        return ((AnnotationUtils.findAnnotation(beanType, Controller.class) != null) ||
                (AnnotationUtils.findAnnotation(beanType, RequestMapping.class) != null));
    }

    /**
     * 获取method上的@RequestMapping内容
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {

        RequestMappingInfo info = null;
        RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);

        if (methodAnnotation != null) {

            RequestCondition<?> methodCondition = getCustomMethodCondition(method);

            info = createRequestMappingInfo(methodAnnotation, methodCondition);

            RequestMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);

            if (typeAnnotation != null) {

                RequestCondition<?> typeCondition = getCustomTypeCondition(handlerType);

                info = createRequestMappingInfo(typeAnnotation, typeCondition).combine(info);
            }
        }
        return info;
    }

    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return null;
    }

    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }


    protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation, RequestCondition<?> customCondition) {

        String[] patterns = resolveEmbeddedValuesInPatterns(annotation.value());

        return new RequestMappingInfo(
                new PatternsRequestCondition(patterns, getUrlPathHelper(), getPathMatcher(), this.useSuffixPatternMatch, this.useTrailingSlashMatch, this.fileExtensions),
                new RequestMethodsRequestCondition(annotation.method()),
                new ParamsRequestCondition(annotation.params()),
                new HeadersRequestCondition(annotation.headers()),
                new ConsumesRequestCondition(annotation.consumes(), annotation.headers()),
                new ProducesRequestCondition(annotation.produces(), annotation.headers(), getContentNegotiationManager()), customCondition);
    }


    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.embeddedValueResolver == null) {
            return patterns;
        } else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }


}
