package com.github.datalking.beans;

/**
 * @author yaoo on 5/27/18
 */
public interface Mergeable {

    boolean isMergeEnabled();

    Object merge(Object parent);

}
