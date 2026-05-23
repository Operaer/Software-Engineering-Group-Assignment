package com.bupt.ta.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit log entry model in the TA recruitment system.
 *
 * <p>Each audit log entry records an operation event, including the operator,
 * action type, target, and detailed description, enabling traceability audit
 * of system operations.</p>
 *
 * @author Operaer
 * @since 2026-05-17
 */
public class AuditLogEntry {
    private String id;
    private Instant timestamp;
    private String operator;
    private String actionType;
    private String target;
    private String description;

    /** No-arg constructor (for deserialization) */
    public AuditLogEntry() {
        // for deserialization
    }

    /**
     * Constructs an audit log entry from operation info, auto-generating the ID
     * and recording the current timestamp.
     *
     * @param operator    Operator
     * @param actionType  Action type
     * @param target      Operation target
     * @param description Operation description
     */
    public AuditLogEntry(String operator, String actionType, String target, String description) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.operator = operator;
        this.actionType = actionType;
        this.target = target;
        this.description = description;
    }

    /** Get log ID */
    public String getId() {
        return id;
    }

    /** Set log ID */
    public void setId(String id) {
        this.id = id;
    }

    /** Get operation timestamp */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** Set operation timestamp */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /** Get operator */
    public String getOperator() {
        return operator;
    }

    /** Set operator */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /** Get action type */
    public String getActionType() {
        return actionType;
    }

    /** Set action type */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /** Get operation target */
    public String getTarget() {
        return target;
    }

    /** Set operation target */
    public void setTarget(String target) {
        this.target = target;
    }

    /** Get operation description */
    public String getDescription() {
        return description;
    }

    /** Set operation description */
    public void setDescription(String description) {
        this.description = description;
    }
}
