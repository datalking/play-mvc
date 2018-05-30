package com.github.datalking.jdbc.transaction;

import com.github.datalking.jdbc.transaction.exception.CannotCreateTransactionException;
import com.github.datalking.jdbc.transaction.exception.TransactionException;

/**
 * @author yaoo on 5/30/18
 */
public abstract class AbstractTransactionStatus implements TransactionStatus {

    private boolean rollbackOnly = false;

    private boolean completed = false;

    private Object savepoint;


    //---------------------------------------------------------------------
    // Handling of current transaction state
    //---------------------------------------------------------------------

    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    /**
     * Determine the rollback-only flag via checking both the local rollback-only flag
     * of this TransactionStatus and the global rollback-only flag of the underlying
     * transaction, if any.
     */
    public boolean isRollbackOnly() {
        return (isLocalRollbackOnly() || isGlobalRollbackOnly());
    }

    /**
     * Determine the rollback-only flag via checking this TransactionStatus.
     * <p>Will only return "true" if the application called {@code setRollbackOnly}
     * on this TransactionStatus object.
     */
    public boolean isLocalRollbackOnly() {
        return this.rollbackOnly;
    }

    /**
     * Template method for determining the global rollback-only flag of the
     * underlying transaction, if any.
     * <p>This implementation always returns {@code false}.
     */
    public boolean isGlobalRollbackOnly() {
        return false;
    }

    /**
     * This implementations is empty, considering flush as a no-op.
     */
    public void flush() {
    }

    /**
     * Mark this transaction as completed, that is, committed or rolled back.
     */
    public void setCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return this.completed;
    }


    //---------------------------------------------------------------------
    // Handling of current savepoint state
    //---------------------------------------------------------------------

    /**
     * Set a savepoint for this transaction. Useful for PROPAGATION_NESTED.
     */
    protected void setSavepoint(Object savepoint) {
        this.savepoint = savepoint;
    }

    /**
     * Get the savepoint for this transaction, if any.
     */
    protected Object getSavepoint() {
        return this.savepoint;
    }

    public boolean hasSavepoint() {
        return (this.savepoint != null);
    }

    /**
     * Create a savepoint and hold it for the transaction.
     */
    public void createAndHoldSavepoint() throws TransactionException {
        setSavepoint(getSavepointManager().createSavepoint());
    }

    /**
     * Roll back to the savepoint that is held for the transaction.
     */
    public void rollbackToHeldSavepoint() throws TransactionException {
        if (!hasSavepoint()) {
            throw new TransactionException("No savepoint associated with current transaction");
        }
        getSavepointManager().rollbackToSavepoint(getSavepoint());
        setSavepoint(null);
    }

    /**
     * Release the savepoint that is held for the transaction.
     */
    public void releaseHeldSavepoint() throws TransactionException {
        if (!hasSavepoint()) {
            throw new TransactionException("No savepoint associated with current transaction");
        }
        getSavepointManager().releaseSavepoint(getSavepoint());
        setSavepoint(null);
    }


    //---------------------------------------------------------------------
    // Implementation of SavepointManager
    //---------------------------------------------------------------------

    /**
     * This implementation delegates to a SavepointManager for the
     * underlying transaction, if possible.
     */
    public Object createSavepoint() throws TransactionException {
        return getSavepointManager().createSavepoint();
    }

    /**
     * This implementation delegates to a SavepointManager for the
     * underlying transaction, if possible.
     */
    public void rollbackToSavepoint(Object savepoint) throws TransactionException {
        getSavepointManager().rollbackToSavepoint(savepoint);
    }

    /**
     * This implementation delegates to a SavepointManager for the
     * underlying transaction, if possible.
     */
    public void releaseSavepoint(Object savepoint) throws TransactionException {
        getSavepointManager().releaseSavepoint(savepoint);
    }

    /**
     * Return a SavepointManager for the underlying transaction, if possible.
     * <p>Default implementation always throws a NestedTransactionNotSupportedException.
     */
    protected SavepointManager getSavepointManager() {
//        throw new NestedTransactionNotSupportedException("This transaction does not support savepoints");
        throw new CannotCreateTransactionException("This transaction does not support savepoints");
    }

}
