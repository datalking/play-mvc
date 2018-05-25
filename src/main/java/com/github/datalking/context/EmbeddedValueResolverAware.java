package com.github.datalking.context;

import com.github.datalking.beans.factory.Aware;
import com.github.datalking.common.StringValueResolver;

/**
 * @author yaoo on 4/29/18
 */
public interface EmbeddedValueResolverAware extends Aware {

    void setEmbeddedValueResolver(StringValueResolver resolver);

}
