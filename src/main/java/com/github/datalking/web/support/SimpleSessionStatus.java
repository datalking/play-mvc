package com.github.datalking.web.support;

/**
 * @author yaoo on 4/29/18
 */
public class SimpleSessionStatus implements SessionStatus {

    private boolean complete = false;

    public void setComplete() {
        this.complete = true;
    }

    public boolean isComplete() {
        return this.complete;
    }

}
