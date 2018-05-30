package com.github.datalking.jdbc.transaction;

import com.github.datalking.common.Constants;

import java.io.Serializable;

/**
 * @author yaoo on 5/30/18
 */
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

    public static final String PREFIX_PROPAGATION = "PROPAGATION_";

    public static final String PREFIX_ISOLATION = "ISOLATION_";

    public static final String PREFIX_TIMEOUT = "timeout_";

    public static final String READ_ONLY_MARKER = "readOnly";

    static final Constants constants = new Constants(TransactionDefinition.class);

    private int propagationBehavior = PROPAGATION_REQUIRED;

    private int isolationLevel = ISOLATION_DEFAULT;

    private int timeout = TIMEOUT_DEFAULT;

    private boolean readOnly = false;

    private String name;


    /**
     * Create a new DefaultTransactionDefinition, with default settings.
     * Can be modified through bean property setters.
     */
    public DefaultTransactionDefinition() {
    }

    /**
     * Copy constructor. Definition can be modified through bean property setters.
     */
    public DefaultTransactionDefinition(TransactionDefinition other) {
        this.propagationBehavior = other.getPropagationBehavior();
        this.isolationLevel = other.getIsolationLevel();
        this.timeout = other.getTimeout();
        this.readOnly = other.isReadOnly();
        this.name = other.getName();
    }

    /**
     * Create a new DefaultTransactionDefinition with the the given
     * propagation behavior. Can be modified through bean property setters.
     *
     * @param propagationBehavior one of the propagation constants in the
     *                            TransactionDefinition interface
     */
    public DefaultTransactionDefinition(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }


    /**
     * Set the propagation behavior by the name of the corresponding constant in
     * TransactionDefinition, e.g. "PROPAGATION_REQUIRED".
     *
     * @param constantName name of the constant
     * @throws IllegalArgumentException if the supplied value is not resolvable
     *                                  to one of the {@code PROPAGATION_} constants or is {@code null}
     */
    public final void setPropagationBehaviorName(String constantName) throws IllegalArgumentException {
        if (constantName == null || !constantName.startsWith(PREFIX_PROPAGATION)) {
            throw new IllegalArgumentException("Only propagation constants allowed");
        }
        setPropagationBehavior(constants.asNumber(constantName).intValue());
    }

    /**
     * Set the propagation behavior. Must be one of the propagation constants
     * in the TransactionDefinition interface. Default is PROPAGATION_REQUIRED.
     *
     * @throws IllegalArgumentException if the supplied value is not
     *                                  one of the {@code PROPAGATION_} constants
     * @see #PROPAGATION_REQUIRED
     */
    public final void setPropagationBehavior(int propagationBehavior) {
        if (!constants.getValues(PREFIX_PROPAGATION).contains(propagationBehavior)) {
            throw new IllegalArgumentException("Only values of propagation constants allowed");
        }
        this.propagationBehavior = propagationBehavior;
    }

    public final int getPropagationBehavior() {
        return this.propagationBehavior;
    }

    /**
     * Set the isolation level by the name of the corresponding constant in
     * TransactionDefinition, e.g. "ISOLATION_DEFAULT".
     *
     * @param constantName name of the constant
     * @throws IllegalArgumentException if the supplied value is not resolvable
     *                                  to one of the {@code ISOLATION_} constants or is {@code null}
     * @see #setIsolationLevel
     * @see #ISOLATION_DEFAULT
     */
    public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
        if (constantName == null || !constantName.startsWith(PREFIX_ISOLATION)) {
            throw new IllegalArgumentException("Only isolation constants allowed");
        }
        setIsolationLevel(constants.asNumber(constantName).intValue());
    }

    /**
     * Set the isolation level. Must be one of the isolation constants
     * in the TransactionDefinition interface. Default is ISOLATION_DEFAULT.
     *
     * @throws IllegalArgumentException if the supplied value is not
     *                                  one of the {@code ISOLATION_} constants
     * @see #ISOLATION_DEFAULT
     */
    public final void setIsolationLevel(int isolationLevel) {
        if (!constants.getValues(PREFIX_ISOLATION).contains(isolationLevel)) {
            throw new IllegalArgumentException("Only values of isolation constants allowed");
        }
        this.isolationLevel = isolationLevel;
    }

    public final int getIsolationLevel() {
        return this.isolationLevel;
    }

    /**
     * Set the timeout to apply, as number of seconds.
     * Default is TIMEOUT_DEFAULT (-1).
     *
     * @see #TIMEOUT_DEFAULT
     */
    public final void setTimeout(int timeout) {
        if (timeout < TIMEOUT_DEFAULT) {
            throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
        }
        this.timeout = timeout;
    }

    public final int getTimeout() {
        return this.timeout;
    }

    /**
     * Set whether to optimize as read-only transaction.
     * Default is "false".
     */
    public final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public final boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Set the name of this transaction. Default is none.
     * <p>This will be used as transaction name to be shown in a
     * transaction monitor, if applicable (for example, WebLogic's).
     */
    public final void setName(String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }


    /**
     * This implementation compares the {@code toString()} results.
     *
     * @see #toString()
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof TransactionDefinition && toString().equals(other.toString()));
    }

    /**
     * This implementation returns {@code toString()}'s hash code.
     *
     * @see #toString()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Return an identifying description for this transaction definition.
     * <p>The format matches the one used by TransactionAttributeEditor,
     * to be able to feed {@code toString} results into bean properties of type TransactionAttribute.
     * <p>Has to be overridden in subclasses for correct {@code equals}
     * and {@code hashCode} behavior. Alternatively, {@link #equals}
     * and {@link #hashCode} can be overridden themselves.
     */
    @Override
    public String toString() {
        return getDefinitionDescription().toString();
    }

    /**
     * Return an identifying description for this transaction definition.
     * <p>Available to subclasses, for inclusion in their {@code toString()} result.
     */
    protected final StringBuilder getDefinitionDescription() {
        StringBuilder result = new StringBuilder();
        result.append(constants.toCode(this.propagationBehavior, PREFIX_PROPAGATION));
        result.append(',');
        result.append(constants.toCode(this.isolationLevel, PREFIX_ISOLATION));
        if (this.timeout != TIMEOUT_DEFAULT) {
            result.append(',');
            result.append(PREFIX_TIMEOUT).append(this.timeout);
        }
        if (this.readOnly) {
            result.append(',');
            result.append(READ_ONLY_MARKER);
        }
        return result;
    }

}
