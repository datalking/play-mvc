package com.github.datalking.web.http;


import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.ObjectUtils;

/**
 * 代表http request或response，由header和body2部分构成
 */
public class HttpEntity<T> {

    public static final HttpEntity EMPTY = new HttpEntity();

    private final HttpHeaders headers;

    private final T body;

    protected HttpEntity() {
        this(null, null);
    }

    public HttpEntity(T body) {
        this(body, null);
    }

    public HttpEntity(MultiValueMap<String, String> headers) {
        this(null, headers);
    }

    public HttpEntity(T body, MultiValueMap<String, String> headers) {
        this.body = body;
        HttpHeaders tempHeaders = new HttpHeaders();
        if (headers != null) {
            tempHeaders.putAll(headers);
        }
        this.headers = HttpHeaders.readOnlyHttpHeaders(tempHeaders);
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public T getBody() {
        return this.body;
    }

    public boolean hasBody() {
        return (this.body != null);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HttpEntity)) {
            return false;
        }
        HttpEntity<?> otherEntity = (HttpEntity<?>) other;
        return (ObjectUtils.nullSafeEquals(this.headers, otherEntity.headers) &&
                ObjectUtils.nullSafeEquals(this.body, otherEntity.body));
    }

    @Override
    public int hashCode() {
        return (ObjectUtils.nullSafeHashCode(this.headers) * 29 + ObjectUtils.nullSafeHashCode(this.body));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        if (this.body != null) {
            builder.append(this.body);
            if (this.headers != null) {
                builder.append(',');
            }
        }
        if (this.headers != null) {
            builder.append(this.headers);
        }
        builder.append('>');
        return builder.toString();
    }

}
