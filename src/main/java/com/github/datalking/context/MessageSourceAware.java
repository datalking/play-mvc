package com.github.datalking.context;

import com.github.datalking.beans.factory.Aware;

/**
 * @author yaoo on 5/3/18
 */
public interface MessageSourceAware extends Aware {

    void setMessageSource(MessageSource messageSource);

}
