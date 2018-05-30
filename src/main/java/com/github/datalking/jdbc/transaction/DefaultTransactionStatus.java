package com.github.datalking.jdbc.transaction;

import com.github.datalking.jdbc.support.SmartTransactionObject;
import com.github.datalking.jdbc.transaction.exception.CannotCreateTransactionException;

/**
 * @author yaoo on 5/30/18
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus {

    private final Object transaction;

    private final boolean newTransaction;

    private final boolean newSynchronization;

    private final boolean readOnly;

    private final boolean debug;

    private final Object suspendedResources;


    /**
     * Create a new DefaultTransactionStatus instance.
     *
     * @param transaction        underlying transaction object that can hold state for the internal transaction implementation
     * @param newTransaction     if the transaction is new, else participating in an existing transaction
     * @param newSynchronization if a new transaction synchronization has been opened for the given transaction
     * @param readOnly           whether the transaction is read-only
     * @param debug              should debug logging be enabled for the handling of this transaction?
     *                           Caching it in here can prevent repeated calls to ask the logging system whether
     *                           debug logging should be enabled.
     * @param suspendedResources a holder for resources that have been suspended for this transaction, if any
     */
    public DefaultTransactionStatus(Object transaction,
                                    boolean newTransaction,
                                    boolean newSynchronization,
                                    boolean readOnly,
                                    boolean debug,
                                    Object suspendedResources) {

        this.transaction = transaction;
        this.newTransaction = newTransaction;
        this.newSynchronization = newSynchronization;
        this.readOnly = readOnly;
        this.debug = debug;
        this.suspendedResources = suspendedResources;
    }

    public Object getTransaction() {
        return this.transaction;
    }

    public boolean hasTransaction() {
        return (this.transaction != null);
    }

    public boolean isNewTransaction() {
        return (hasTransaction() && this.newTransaction);
    }

    /**
     * Return if a new transaction synchronization has been opened
     * for this transaction.
     */
    public boolean isNewSynchronization() {
        return this.newSynchronization;
    }

    /**
     * Return if this transaction is defined as read-only transaction.
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Return whether the progress of this transaction is debugged. This is used
     * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
     * calls to logger.isDebug(). Not really intended for client code.
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * Return the holder for resources that have been suspended for this transaction,
     * if any.
     */
    public Object getSuspendedResources() {
        return this.suspendedResources;
    }


    //---------------------------------------------------------------------
    // Enable functionality through underlying transaction object
    //---------------------------------------------------------------------

    /**
     * Determine the rollback-only flag via checking both the transaction object,
     * provided that the latter implements the SmartTransactionObject interface.
     * <p>Will return "true" if the transaction itself has been marked rollback-only
     * by the transaction coordinator, for example in case of a timeout.
     */
    @Override
    public boolean isGlobalRollbackOnly() {

        return ((this.transaction instanceof SmartTransactionObject) &&
                ((SmartTransactionObject) this.transaction).isRollbackOnly());

    }

    /**
     * Delegate the flushing to the transaction object,
     * provided that the latter implements the  SmartTransactionObject interface.
     */
    @Override
    public void flush() {

        if (this.transaction instanceof SmartTransactionObject) {
            ((SmartTransactionObject) this.transaction).flush();
        }

    }

    @Override
    protected SavepointManager getSavepointManager() {
        if (!isTransactionSavepointManager()) {
            throw new CannotCreateTransactionException("Transaction object [" + getTransaction() + "] does not support savepoints");
        }
        return (SavepointManager) getTransaction();
    }

    public boolean isTransactionSavepointManager() {
        return (getTransaction() instanceof SavepointManager);
    }

}
