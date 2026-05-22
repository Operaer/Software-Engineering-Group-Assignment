package com.bupt.ta.model;

import java.time.Instant;
import java.util.UUID;

public class AuditLogEntry {
    private String id;
    private Instant timestamp;
    private String operator;
    private String actionType;
    private String target;
    private String description;

    public AuditLogEntry() {
        // for deserialization
    }

    public AuditLogEntry(String operator, String actionType, String target, String description) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.operator = operator;
        this.actionType = actionType;
        this.target = target;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
