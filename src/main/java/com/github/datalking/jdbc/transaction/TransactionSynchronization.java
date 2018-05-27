package com.github.datalking.jdbc.transaction;

/**
 * @author yaoo on 5/27/18
 */
public interface TransactionSynchronization {

    /// 事务完成状态
    int STATUS_COMMITTED = 0;
    int STATUS_ROLLED_BACK = 1;
    int STATUS_UNKNOWN = 2;

    void suspend();

    void resume();

    void flush();

    void beforeCommit(boolean readOnly);

    void beforeCompletion();

    void afterCommit();

    void afterCompletion(int status);

}
