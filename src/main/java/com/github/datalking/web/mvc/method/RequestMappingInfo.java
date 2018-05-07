package com.github.datalking.web.mvc.method;

import com.github.datalking.web.mvc.condition.ConsumesRequestCondition;
import com.github.datalking.web.mvc.condition.HeadersRequestCondition;
import com.github.datalking.web.mvc.condition.ParamsRequestCondition;
import com.github.datalking.web.mvc.condition.PatternsRequestCondition;
import com.github.datalking.web.mvc.condition.ProducesRequestCondition;
import com.github.datalking.web.mvc.condition.RequestCondition;
import com.github.datalking.web.mvc.condition.RequestConditionHolder;
import com.github.datalking.web.mvc.condition.RequestMethodsRequestCondition;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yaoo on 4/28/18
 */
public class RequestMappingInfo implements RequestCondition<RequestMappingInfo> {

    private final PatternsRequestCondition patternsCondition;

    private final RequestMethodsRequestCondition methodsCondition;

    private final ParamsRequestCondition paramsCondition;

    private final HeadersRequestCondition headersCondition;

    private final ConsumesRequestCondition consumesCondition;

    private final ProducesRequestCondition producesCondition;

    private final RequestConditionHolder customConditionHolder;

    public RequestMappingInfo(PatternsRequestCondition patterns,
                              RequestMethodsRequestCondition methods,
                              ParamsRequestCondition params,
                              HeadersRequestCondition headers,
                              ConsumesRequestCondition consumes,
                              ProducesRequestCondition produces,
                              RequestCondition<?> custom) {

        this.patternsCondition = (patterns != null ? patterns : new PatternsRequestCondition());
        this.methodsCondition = (methods != null ? methods : new RequestMethodsRequestCondition());
        this.paramsCondition = (params != null ? params : new ParamsRequestCondition());
        this.headersCondition = (headers != null ? headers : new HeadersRequestCondition());
        this.consumesCondition = (consumes != null ? consumes : new ConsumesRequestCondition());
        this.producesCondition = (produces != null ? produces : new ProducesRequestCondition());
        this.customConditionHolder = new RequestConditionHolder(custom);
    }

    public RequestMappingInfo(RequestMappingInfo info, RequestCondition<?> customRequestCondition) {
        this(info.patternsCondition,
                info.methodsCondition,
                info.paramsCondition,
                info.headersCondition,
                info.consumesCondition,
                info.producesCondition,
                customRequestCondition);
    }

    public PatternsRequestCondition getPatternsCondition() {
        return this.patternsCondition;
    }

    public RequestMethodsRequestCondition getMethodsCondition() {
        return this.methodsCondition;
    }

    public ParamsRequestCondition getParamsCondition() {
        return this.paramsCondition;
    }

    public HeadersRequestCondition getHeadersCondition() {
        return this.headersCondition;
    }

    public ConsumesRequestCondition getConsumesCondition() {
        return this.consumesCondition;
    }

    public ProducesRequestCondition getProducesCondition() {
        return this.producesCondition;
    }

    public RequestCondition<?> getCustomCondition() {
        return this.customConditionHolder.getCondition();
    }

    public RequestMappingInfo combine(RequestMappingInfo other) {
        PatternsRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
        RequestMethodsRequestCondition methods = this.methodsCondition.combine(other.methodsCondition);
        ParamsRequestCondition params = this.paramsCondition.combine(other.paramsCondition);
        HeadersRequestCondition headers = this.headersCondition.combine(other.headersCondition);
        ConsumesRequestCondition consumes = this.consumesCondition.combine(other.consumesCondition);
        ProducesRequestCondition produces = this.producesCondition.combine(other.producesCondition);
        RequestConditionHolder custom = this.customConditionHolder.combine(other.customConditionHolder);

        return new RequestMappingInfo(patterns, methods, params, headers, consumes, produces, custom.getCondition());
    }

    public RequestMappingInfo getMatchingCondition(HttpServletRequest request) {
        RequestMethodsRequestCondition methods = this.methodsCondition.getMatchingCondition(request);
        ParamsRequestCondition params = this.paramsCondition.getMatchingCondition(request);
        HeadersRequestCondition headers = this.headersCondition.getMatchingCondition(request);
        ConsumesRequestCondition consumes = this.consumesCondition.getMatchingCondition(request);
        ProducesRequestCondition produces = this.producesCondition.getMatchingCondition(request);

        if (methods == null || params == null || headers == null || consumes == null || produces == null) {
            return null;
        }

        PatternsRequestCondition patterns = this.patternsCondition.getMatchingCondition(request);
        if (patterns == null) {
            return null;
        }

        RequestConditionHolder custom = this.customConditionHolder.getMatchingCondition(request);
        if (custom == null) {
            return null;
        }

        return new RequestMappingInfo(patterns, methods, params, headers, consumes, produces, custom.getCondition());
    }

    public int compareTo(RequestMappingInfo other, HttpServletRequest request) {
        int result = this.patternsCondition.compareTo(other.getPatternsCondition(), request);
        if (result != 0) {
            return result;
        }
        result = this.paramsCondition.compareTo(other.getParamsCondition(), request);
        if (result != 0) {
            return result;
        }
        result = this.headersCondition.compareTo(other.getHeadersCondition(), request);
        if (result != 0) {
            return result;
        }
        result = this.consumesCondition.compareTo(other.getConsumesCondition(), request);
        if (result != 0) {
            return result;
        }
        result = this.producesCondition.compareTo(other.getProducesCondition(), request);
        if (result != 0) {
            return result;
        }
        result = this.methodsCondition.compareTo(other.getMethodsCondition(), request);
        if (result != 0) {
            return result;
        }
        result = this.customConditionHolder.compareTo(other.customConditionHolder, request);
        if (result != 0) {
            return result;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof RequestMappingInfo) {
            RequestMappingInfo other = (RequestMappingInfo) obj;
            return (this.patternsCondition.equals(other.patternsCondition) &&
                    this.methodsCondition.equals(other.methodsCondition) &&
                    this.paramsCondition.equals(other.paramsCondition) &&
                    this.headersCondition.equals(other.headersCondition) &&
                    this.consumesCondition.equals(other.consumesCondition) &&
                    this.producesCondition.equals(other.producesCondition) &&
                    this.customConditionHolder.equals(other.customConditionHolder));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.patternsCondition.hashCode() * 31 +  // primary differentiation
                this.methodsCondition.hashCode() + this.paramsCondition.hashCode() +
                this.headersCondition.hashCode() + this.consumesCondition.hashCode() +
                this.producesCondition.hashCode() + this.customConditionHolder.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append(this.patternsCondition);
        builder.append(",methods=").append(this.methodsCondition);
        builder.append(",params=").append(this.paramsCondition);
        builder.append(",headers=").append(this.headersCondition);
        builder.append(",consumes=").append(this.consumesCondition);
        builder.append(",produces=").append(this.producesCondition);
        builder.append(",custom=").append(this.customConditionHolder);
        builder.append('}');
        return builder.toString();
    }

}
