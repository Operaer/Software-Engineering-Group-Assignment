package com.bupt.ta.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single audit log entry recording an administrative action.
 *
 * <p>Each entry captures who performed the action, what type of action was taken,
 * which target resource was affected, and a human-readable description of the
 * operation. Entries are automatically assigned a unique identifier and timestamp
 * upon creation.</p>
 */
public class AuditLogEntry {
    private String id;
    private Instant timestamp;
    private String operator;
    private String actionType;
    private String target;
    private String description;

    /**
     * Constructs an empty AuditLogEntry for deserialization.
     */
    public AuditLogEntry() {
        // for deserialization
    }

    /**
     * Constructs an AuditLogEntry with the given details, auto-generating
     * a unique identifier and recording the current timestamp.
     *
     * @param operator    the user who performed the action
     * @param actionType  the type of action performed
     * @param target      the target resource affected by the action
     * @param description a human-readable description of the action
     */
    public AuditLogEntry(String operator, String actionType, String target, String description) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.operator = operator;
        this.actionType = actionType;
        this.target = target;
        this.description = description;
    }

    /**
     * Returns the unique identifier of this audit log entry.
     *
     * @return the entry id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this audit log entry.
     *
     * @param id the entry id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the timestamp when this entry was created.
     *
     * @return the creation timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp for this entry.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the user who performed the action.
     *
     * @return the operator name
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the user who performed the action.
     *
     * @param operator the operator name to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Returns the type of action performed (e.g. CREATE, UPDATE, DELETE).
     *
     * @return the action type
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Sets the type of action performed.
     *
     * @param actionType the action type to set
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * Returns the target resource affected by the action.
     *
     * @return the target identifier
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target resource affected by the action.
     *
     * @param target the target identifier to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Returns a human-readable description of the action.
     *
     * @return the description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a human-readable description of the action.
     *
     * @param description the description text to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
