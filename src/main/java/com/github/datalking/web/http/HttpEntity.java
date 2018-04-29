package com.github.datalking.web.http;


import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.ObjectUtils;

/**
 */
public class HttpEntity<T> {

    public static final HttpEntity EMPTY = new HttpEntity();

    private final HttpHeaders headers;

    private final T body;

    /**
     * Create a new, empty {@code HttpEntity}.
     */
    protected HttpEntity() {
        this(null, null);
    }

    /**
     * Create a new {@code HttpEntity} with the given body and no headers.
     *
     * @param body the entity body
     */
    public HttpEntity(T body) {
        this(body, null);
    }

    /**
     * Create a new {@code HttpEntity} with the given headers and no body.
     *
     * @param headers the entity headers
     */
    public HttpEntity(MultiValueMap<String, String> headers) {
        this(null, headers);
    }

    /**
     * Create a new {@code HttpEntity} with the given body and headers.
     *
     * @param body    the entity body
     * @param headers the entity headers
     */
    public HttpEntity(T body, MultiValueMap<String, String> headers) {
        this.body = body;
        HttpHeaders tempHeaders = new HttpHeaders();
        if (headers != null) {
            tempHeaders.putAll(headers);
        }
        this.headers = HttpHeaders.readOnlyHttpHeaders(tempHeaders);
    }


    /**
     * Returns the headers of this entity.
     */
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    /**
     * Returns the body of this entity.
     */
    public T getBody() {
        return this.body;
    }

    /**
     * Indicates whether this entity has a body.
     */
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
