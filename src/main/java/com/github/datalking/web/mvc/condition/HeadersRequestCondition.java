package com.github.datalking.web.mvc.condition;


import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * copied from spring
 */
public class HeadersRequestCondition extends AbstractRequestCondition<HeadersRequestCondition> {

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

    public HeadersRequestCondition combine(HeadersRequestCondition other) {
        Set<HeaderExpression> set = new LinkedHashSet<>(this.expressions);
        set.addAll(other.expressions);
        return new HeadersRequestCondition(set);
    }

    public HeadersRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (HeaderExpression expression : expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }

    public int compareTo(HeadersRequestCondition other, HttpServletRequest request) {
        return other.expressions.size() - this.expressions.size();
    }

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
