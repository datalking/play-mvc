package com.github.datalking.aop.aspectj;

import com.github.datalking.util.Assert;

import java.io.Serializable;

/**
 * @author yaoo on 4/19/18
 */
public class LazySingletonAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory, Serializable {

    private final MetadataAwareAspectInstanceFactory maaif;

    private volatile Object materialized;

    public LazySingletonAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory maaif) {
        Assert.notNull(maaif, "AspectInstanceFactory must not be null");
        this.maaif = maaif;
    }


    @Override
    public Object getAspectInstance() {
        if (this.materialized == null) {
            //Object mutex = this.maaif.getAspectCreationMutex();
                this.materialized = this.maaif.getAspectInstance();

        }
        return this.materialized;
    }

    public boolean isMaterialized() {
        return (this.materialized != null);
    }

//    @Override
//    public ClassLoader getAspectClassLoader() {
//        return this.maaif.getAspectClassLoader();
//    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return this.maaif.getAspectMetadata();
    }

    @Override
    public int getOrder() {
        return this.maaif.getOrder();
    }


    @Override
    public String toString() {
        return "LazySingletonAspectInstanceFactoryDecorator: decorating " + this.maaif;
    }



}
