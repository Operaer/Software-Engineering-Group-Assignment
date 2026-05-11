package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a recorded administrative operation log entry.
 * It captures when the operation occurred, who performed it, and
 * what was affected.
 */
public class OperationLogEntry implements Serializable {
    private Instant timestamp;
    private String operator;
    private String actionType;
    private String target;
    private String details;

    public OperationLogEntry() {
    }

    public OperationLogEntry(Instant timestamp, String operator, String actionType, String target, String details) {
        this.timestamp = timestamp;
        this.operator = operator;
        this.actionType = actionType;
        this.target = target;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
