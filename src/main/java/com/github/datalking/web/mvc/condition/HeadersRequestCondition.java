package com.github.datalking.web.mvc.condition;


import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * copied from spring
 */
public final class HeadersRequestCondition extends AbstractRequestCondition<HeadersRequestCondition> {

    private final Set<HeaderExpression> expressions;


    public HeadersRequestCondition(String... headers) {
        this(parseExpressions(headers));
    }

    private HeadersRequestCondition(Collection<HeaderExpression> conditions) {
        this.expressions = Collections.unmodifiableSet(new LinkedHashSet<>(conditions));
    }


    private static Collection<HeaderExpression> parseExpressions(String... headers) {
        Set<HeaderExpression> expressions = new LinkedHashSet<>();
        if (headers != null) {
            for (String header : headers) {
                HeaderExpression expr = new HeaderExpression(header);
                if ("Accept".equalsIgnoreCase(expr.name) || "Content-Type".equalsIgnoreCase(expr.name)) {
                    continue;
                }
                expressions.add(expr);
            }
        }
        return expressions;
    }

    /**
     * Return the contained request header expressions.
     */
    public Set<NameValueExpression<String>> getExpressions() {
        return new LinkedHashSet<>(this.expressions);
    }

    @Override
    protected Collection<HeaderExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    /**
     * Returns a new instance with the union of the header expressions
     * from "this" and the "other" instance.
     */
    public HeadersRequestCondition combine(HeadersRequestCondition other) {
        Set<HeaderExpression> set = new LinkedHashSet<HeaderExpression>(this.expressions);
        set.addAll(other.expressions);
        return new HeadersRequestCondition(set);
    }

    /**
     * Returns "this" instance if the request matches all expressions;
     * or {@code null} otherwise.
     */
    public HeadersRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (HeaderExpression expression : expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }

    /**
     * Returns:
     * <ul>
     * <li>0 if the two conditions have the same number of header expressions
     * <li>Less than 0 if "this" instance has more header expressions
     * <li>Greater than 0 if the "other" instance has more header expressions
     * </ul>
     * <p>It is assumed that both instances have been obtained via
     * {@link #getMatchingCondition(HttpServletRequest)} and each instance
     * contains the matching header expression only or is otherwise empty.
     */
    public int compareTo(HeadersRequestCondition other, HttpServletRequest request) {
        return other.expressions.size() - this.expressions.size();
    }


    /**
     * Parses and matches a single header expression to a request.
     */
    static class HeaderExpression extends AbstractNameValueExpression<String> {

        public HeaderExpression(String expression) {
            super(expression);
        }

        @Override
        protected String parseValue(String valueExpression) {
            return valueExpression;
        }

        @Override
        protected boolean matchName(HttpServletRequest request) {
            return request.getHeader(name) != null;
        }

        @Override
        protected boolean matchValue(HttpServletRequest request) {
            return value.equals(request.getHeader(name));
        }

        @Override
        public int hashCode() {
            int result = name.toLowerCase().hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (isNegated ? 1 : 0);
            return result;
        }
    }

}
