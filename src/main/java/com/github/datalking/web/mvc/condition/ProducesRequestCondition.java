
package com.github.datalking.web.mvc.condition;

import com.github.datalking.web.http.MediaType;
import com.github.datalking.web.http.accept.ContentNegotiationManager;
import com.github.datalking.web.servlet.ServletWebRequest;
import com.github.datalking.web.mvc.condition.HeadersRequestCondition.HeaderExpression;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class ProducesRequestCondition extends AbstractRequestCondition<ProducesRequestCondition> {

    private final List<ProduceMediaTypeExpression> MEDIA_TYPE_ALL_LIST =
            Collections.singletonList(new ProduceMediaTypeExpression("*/*"));

    private final List<ProduceMediaTypeExpression> expressions;

    private final ContentNegotiationManager contentNegotiationManager;


    public ProducesRequestCondition(String... produces) {
        this(produces, null);
    }

    public ProducesRequestCondition(String[] produces, String[] headers) {
        this(produces, headers, null);
    }


    public ProducesRequestCondition(String[] produces, String[] headers, ContentNegotiationManager manager) {
        this.expressions = new ArrayList<>(parseExpressions(produces, headers));
        Collections.sort(this.expressions);
        this.contentNegotiationManager = (manager != null ? manager : new ContentNegotiationManager());
    }

    private ProducesRequestCondition(Collection<ProduceMediaTypeExpression> expressions, ContentNegotiationManager manager) {
        this.expressions = new ArrayList<>(expressions);
        Collections.sort(this.expressions);
        this.contentNegotiationManager = (manager != null ? manager : new ContentNegotiationManager());
    }


    private Set<ProduceMediaTypeExpression> parseExpressions(String[] produces, String[] headers) {
        Set<ProduceMediaTypeExpression> result = new LinkedHashSet<>();
        if (headers != null) {
            for (String header : headers) {
                HeadersRequestCondition.HeaderExpression expr = new HeaderExpression(header);
                if ("Accept".equalsIgnoreCase(expr.name)) {
                    for (MediaType mediaType : MediaType.parseMediaTypes(expr.value)) {
                        result.add(new ProduceMediaTypeExpression(mediaType, expr.isNegated));
                    }
                }
            }
        }
        if (produces != null) {
            for (String produce : produces) {
                result.add(new ProduceMediaTypeExpression(produce));
            }
        }
        return result;
    }


    public Set<MediaTypeExpression> getExpressions() {
        return new LinkedHashSet<>(this.expressions);
    }


    public Set<MediaType> getProducibleMediaTypes() {
        Set<MediaType> result = new LinkedHashSet<>();
        for (ProduceMediaTypeExpression expression : this.expressions) {
            if (!expression.isNegated()) {
                result.add(expression.getMediaType());
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return this.expressions.isEmpty();
    }

    @Override
    protected List<ProduceMediaTypeExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    public ProducesRequestCondition combine(ProducesRequestCondition other) {
        return (!other.expressions.isEmpty() ? other : this);
    }

    public ProducesRequestCondition getMatchingCondition(HttpServletRequest request) {
        if (isEmpty()) {
            return this;
        }
        Set<ProduceMediaTypeExpression> result = new LinkedHashSet<ProduceMediaTypeExpression>(expressions);
        for (Iterator<ProduceMediaTypeExpression> iterator = result.iterator(); iterator.hasNext(); ) {
            ProduceMediaTypeExpression expression = iterator.next();
            if (!expression.match(request)) {
                iterator.remove();
            }
        }
        return (result.isEmpty()) ? null : new ProducesRequestCondition(result, this.contentNegotiationManager);
    }

    public int compareTo(ProducesRequestCondition other, HttpServletRequest request) {
        try {
            List<MediaType> acceptedMediaTypes = getAcceptedMediaTypes(request);
            for (MediaType acceptedMediaType : acceptedMediaTypes) {
                int thisIndex = this.indexOfEqualMediaType(acceptedMediaType);
                int otherIndex = other.indexOfEqualMediaType(acceptedMediaType);
                int result = compareMatchingMediaTypes(this, thisIndex, other, otherIndex);
                if (result != 0) {
                    return result;
                }
                thisIndex = this.indexOfIncludedMediaType(acceptedMediaType);
                otherIndex = other.indexOfIncludedMediaType(acceptedMediaType);
                result = compareMatchingMediaTypes(this, thisIndex, other, otherIndex);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        } catch (Exception ex) {
            // should never happen
//			throw new IllegalStateException("Cannot compare without having any requested media types", ex);
            ex.printStackTrace();
        }
        return 0;
    }

    private List<MediaType> getAcceptedMediaTypes(HttpServletRequest request) {
        List<MediaType> mediaTypes = this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
        return mediaTypes.isEmpty() ? Collections.singletonList(MediaType.ALL) : mediaTypes;
    }

    private int indexOfEqualMediaType(MediaType mediaType) {
        for (int i = 0; i < getExpressionsToCompare().size(); i++) {
            MediaType currentMediaType = getExpressionsToCompare().get(i).getMediaType();
            if (mediaType.getType().equalsIgnoreCase(currentMediaType.getType()) &&
                    mediaType.getSubtype().equalsIgnoreCase(currentMediaType.getSubtype())) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfIncludedMediaType(MediaType mediaType) {
        for (int i = 0; i < getExpressionsToCompare().size(); i++) {
            if (mediaType.includes(getExpressionsToCompare().get(i).getMediaType())) {
                return i;
            }
        }
        return -1;
    }

    private int compareMatchingMediaTypes(ProducesRequestCondition condition1, int index1,
                                          ProducesRequestCondition condition2, int index2) {

        int result = 0;
        if (index1 != index2) {
            result = index2 - index1;
        } else if (index1 != -1) {
            ProduceMediaTypeExpression expr1 = condition1.getExpressionsToCompare().get(index1);
            ProduceMediaTypeExpression expr2 = condition2.getExpressionsToCompare().get(index2);
            result = expr1.compareTo(expr2);
            result = (result != 0) ? result : expr1.getMediaType().compareTo(expr2.getMediaType());
        }
        return result;
    }


    private List<ProduceMediaTypeExpression> getExpressionsToCompare() {
        return (this.expressions.isEmpty() ? MEDIA_TYPE_ALL_LIST : this.expressions);
    }

    class ProduceMediaTypeExpression extends AbstractMediaTypeExpression {

        ProduceMediaTypeExpression(MediaType mediaType, boolean negated) {
            super(mediaType, negated);
        }

        ProduceMediaTypeExpression(String expression) {
            super(expression);
        }

        @Override
        protected boolean matchMediaType(HttpServletRequest request) {
            List<MediaType> acceptedMediaTypes = getAcceptedMediaTypes(request);
            for (MediaType acceptedMediaType : acceptedMediaTypes) {
                if (getMediaType().isCompatibleWith(acceptedMediaType)) {
                    return true;
                }
            }
            return false;
        }
    }

}
