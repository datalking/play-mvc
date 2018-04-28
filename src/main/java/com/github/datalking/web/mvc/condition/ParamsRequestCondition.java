package com.github.datalking.web.mvc.condition;

import com.github.datalking.util.web.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author yaoo on 4/28/18
 */
public final class ParamsRequestCondition extends AbstractRequestCondition<ParamsRequestCondition> {

    private final Set<ParamExpression> expressions;

    public ParamsRequestCondition(String... params) {
        this(parseExpressions(params));
    }

    private ParamsRequestCondition(Collection<ParamExpression> conditions) {
        this.expressions = Collections.unmodifiableSet(new LinkedHashSet<ParamExpression>(conditions));
    }


    private static Collection<ParamExpression> parseExpressions(String... params) {
        Set<ParamExpression> expressions = new LinkedHashSet<ParamExpression>();
        if (params != null) {
            for (String param : params) {
                expressions.add(new ParamExpression(param));
            }
        }
        return expressions;
    }


    /**
     * Return the contained request parameter expressions.
     */
    public Set<NameValueExpression<String>> getExpressions() {
        return new LinkedHashSet<NameValueExpression<String>>(this.expressions);
    }

    @Override
    protected Collection<ParamExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    /**
     * Returns a new instance with the union of the param expressions
     * from "this" and the "other" instance.
     */
    public ParamsRequestCondition combine(ParamsRequestCondition other) {
        Set<ParamExpression> set = new LinkedHashSet<ParamExpression>(this.expressions);
        set.addAll(other.expressions);
        return new ParamsRequestCondition(set);
    }

    /**
     * Returns "this" instance if the request matches all param expressions;
     * or {@code null} otherwise.
     */
    public ParamsRequestCondition getMatchingCondition(HttpServletRequest request) {
        for (ParamExpression expression : expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }


    public int compareTo(ParamsRequestCondition other, HttpServletRequest request) {
        return (other.expressions.size() - this.expressions.size());
    }


    static class ParamExpression extends AbstractNameValueExpression<String> {

        ParamExpression(String expression) {
            super(expression);
        }

        @Override
        protected String parseValue(String valueExpression) {
            return valueExpression;
        }

        @Override
        protected boolean matchName(HttpServletRequest request) {
            return WebUtils.hasSubmitParameter(request, name);
        }

        @Override
        protected boolean matchValue(HttpServletRequest request) {
            return value.equals(request.getParameter(name));
        }
    }

}
