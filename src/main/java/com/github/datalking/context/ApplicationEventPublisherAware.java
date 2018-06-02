package com.github.datalking.context;

import com.github.datalking.beans.factory.Aware;

/**
 * @author yaoo on 6/2/18
 */
public interface ApplicationEventPublisherAware extends Aware {

    void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher);

}
