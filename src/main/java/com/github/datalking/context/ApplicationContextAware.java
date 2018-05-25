package com.github.datalking.context;

import com.github.datalking.beans.factory.Aware;

/**
 * @author yaoo on 4/26/18
 */
public interface ApplicationContextAware extends Aware {

    void setApplicationContext(ApplicationContext applicationContext);

}
