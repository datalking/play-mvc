package com.github.datalking.context;

import com.github.datalking.beans.factory.Aware;
import com.github.datalking.common.env.Environment;

/**
 * @author yaoo on 5/29/18
 */
public interface EnvironmentAware extends Aware {

    void setEnvironment(Environment environment);

}
