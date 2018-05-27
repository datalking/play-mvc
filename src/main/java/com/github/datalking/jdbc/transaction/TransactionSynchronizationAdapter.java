package com.github.datalking.jdbc.transaction;

import com.github.datalking.common.Ordered;

/**
 * @author yaoo on 5/27/18
 */
public abstract class TransactionSynchronizationAdapter implements TransactionSynchronization, Ordered {

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public void flush() {
    }

    public void beforeCommit(boolean readOnly) {
    }

    public void beforeCompletion() {
    }

    public void afterCommit() {
    }

    public void afterCompletion(int status) {
    }

}
