package com.github.datalking.web.mvc.condition;

import com.github.datalking.web.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


abstract class AbstractMediaTypeExpression implements Comparable<AbstractMediaTypeExpression>, MediaTypeExpression {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final MediaType mediaType;

    private final boolean isNegated;


    AbstractMediaTypeExpression(String expression) {
        if (expression.startsWith("!")) {
            this.isNegated = true;
            expression = expression.substring(1);
        } else {
            this.isNegated = false;
        }
        this.mediaType = MediaType.parseMediaType(expression);
    }

    AbstractMediaTypeExpression(MediaType mediaType, boolean negated) {
        this.mediaType = mediaType;
        this.isNegated = negated;
    }

    public MediaType getMediaType() {
        return this.mediaType;
    }

    public boolean isNegated() {
        return this.isNegated;
    }


    public final boolean match(HttpServletRequest request) {
        try {
            boolean match = matchMediaType(request);
            return (!this.isNegated ? match : !match);
        } catch (Exception ex) {
        }
        return false;
    }

    protected abstract boolean matchMediaType(HttpServletRequest request);


    public int compareTo(AbstractMediaTypeExpression other) {
        return MediaType.SPECIFICITY_COMPARATOR.compare(this.getMediaType(), other.getMediaType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass().equals(obj.getClass())) {
            AbstractMediaTypeExpression other = (AbstractMediaTypeExpression) obj;
            return (this.mediaType.equals(other.mediaType) && this.isNegated == other.isNegated);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.mediaType.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.isNegated) {
            builder.append('!');
        }
        builder.append(this.mediaType.toString());
        return builder.toString();
    }

}
