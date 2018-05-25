package com.github.datalking.context;

/**
 * @author yaoo on 4/25/18
 */
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {

    void initialize(C applicationContext);

}
