package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

public class JobHistoryEntry implements Serializable {
    private String jobId;
    private Instant changedAt;
    private String changedBy;
    private String action;
    private String details;
    private String snapshot;

    public JobHistoryEntry() {
    }

    public JobHistoryEntry(String jobId, Instant changedAt, String changedBy, String action, String details, String snapshot) {
        this.jobId = jobId;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.action = action;
        this.details = details;
        this.snapshot = snapshot;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }
}
