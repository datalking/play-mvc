package com.github.datalking.web.http;


import com.github.datalking.common.MultiValueMap;
import com.github.datalking.util.ObjectUtils;

/**
 * HttpEntity + 状态码
 */
public class ResponseEntity<T> extends HttpEntity<T> {

    private final HttpStatus statusCode;

    public ResponseEntity(HttpStatus statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public ResponseEntity(T body, HttpStatus statusCode) {
        super(body);
        this.statusCode = statusCode;
    }

    public ResponseEntity(MultiValueMap<String, String> headers, HttpStatus statusCode) {
        super(headers);
        this.statusCode = statusCode;
    }

    public ResponseEntity(T body, MultiValueMap<String, String> headers, HttpStatus statusCode) {
        super(body, headers);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResponseEntity) || !super.equals(other)) {
            return false;
        }
        ResponseEntity<?> otherEntity = (ResponseEntity<?>) other;
        return ObjectUtils.nullSafeEquals(this.statusCode, otherEntity.statusCode);
    }

    @Override
    public int hashCode() {
        return (super.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.statusCode));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        builder.append(this.statusCode.toString());
        builder.append(' ');
        builder.append(this.statusCode.getReasonPhrase());
        builder.append(',');
        T body = getBody();
        HttpHeaders headers = getHeaders();
        if (body != null) {
            builder.append(body);
            if (headers != null) {
                builder.append(',');
            }
        }
        if (headers != null) {
            builder.append(headers);
        }
        builder.append('>');
        return builder.toString();
    }

}
