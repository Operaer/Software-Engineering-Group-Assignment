package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Position history entry model in the TA recruitment system.
 *
 * <p>Records historical information for each position change, including change time,
 * operator, action, change details, and a complete snapshot at the time of change,
 * enabling position change traceability.</p>
 *
 * @author Operaer
 * @since 2026-05-17
 */
public class JobHistoryEntry implements Serializable {
    private String jobId;
    private Instant changedAt;
    private String changedBy;
    private String action;
    private String details;
    private String snapshot;

    /** No-arg constructor */
    public JobHistoryEntry() {
    }

    /**
     * All-args constructor.
     *
     * @param jobId     Position ID
     * @param changedAt Change time
     * @param changedBy Operator
     * @param action    Action performed
     * @param details   Change details
     * @param snapshot  Position data snapshot at the time of change
     */
    public JobHistoryEntry(String jobId, Instant changedAt, String changedBy, String action, String details, String snapshot) {
        this.jobId = jobId;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.action = action;
        this.details = details;
        this.snapshot = snapshot;
    }

    /** Get position ID */
    public String getJobId() {
        return jobId;
    }

    /** Set position ID */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /** Get change time */
    public Instant getChangedAt() {
        return changedAt;
    }

    /** Set change time */
    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    /** Get operator */
    public String getChangedBy() {
        return changedBy;
    }

    /** Set operator */
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    /** Get action */
    public String getAction() {
        return action;
    }

    /** Set action */
    public void setAction(String action) {
        this.action = action;
    }

    /** Get change details */
    public String getDetails() {
        return details;
    }

    /** Set change details */
    public void setDetails(String details) {
        this.details = details;
    }

    /** Get position data snapshot */
    public String getSnapshot() {
        return snapshot;
    }

    /** Set position data snapshot */
    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }
}
