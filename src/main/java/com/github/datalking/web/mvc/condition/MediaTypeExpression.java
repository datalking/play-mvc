package com.github.datalking.web.mvc.condition;

import com.github.datalking.web.http.MediaType;

/**
 * @author yaoo on 4/29/18
 */
public interface MediaTypeExpression {

    MediaType getMediaType();

    boolean isNegated();

}
