package com.github.datalking.context;

import com.github.datalking.beans.factory.Aware;
import com.github.datalking.io.ResourceLoader;

/**
 * @author yaoo on 5/28/18
 */
public interface ResourceLoaderAware extends Aware {

    void setResourceLoader(ResourceLoader resourceLoader);

}
