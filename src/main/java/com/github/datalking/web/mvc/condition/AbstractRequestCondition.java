package com.github.datalking.web.mvc.condition;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author yaoo on 4/28/18
 */
public abstract class AbstractRequestCondition<T extends AbstractRequestCondition<T>> implements RequestCondition<T> {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass().equals(obj.getClass())) {
            AbstractRequestCondition<?> other = (AbstractRequestCondition<?>) obj;
            return getContent().equals(other.getContent());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getContent().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (Iterator<?> iterator = getContent().iterator(); iterator.hasNext();) {
            Object expression = iterator.next();
            builder.append(expression.toString());
            if (iterator.hasNext()) {
                builder.append(getToStringInfix());
            }
        }
        builder.append("]");
        return builder.toString();
    }

    protected abstract Collection<?> getContent();

    protected abstract String getToStringInfix();

}
