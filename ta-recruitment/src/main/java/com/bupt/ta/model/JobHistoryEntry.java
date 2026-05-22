package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a single historical change made to a job posting.
 *
 * <p>Each entry records what action was performed on a job (e.g. created, updated,
 * closed), who performed it, when it occurred, and a snapshot of the job state
 * at that point in time.</p>
 */
public class JobHistoryEntry implements Serializable {
    private String jobId;
    private Instant changedAt;
    private String changedBy;
    private String action;
    private String details;
    private String snapshot;

    /**
     * Constructs an empty JobHistoryEntry for deserialization.
     */
    public JobHistoryEntry() {
    }

    /**
     * Constructs a JobHistoryEntry with the given fields.
     *
     * @param jobId     the identifier of the job that was changed
     * @param changedAt the timestamp when the change occurred
     * @param changedBy the user who made the change
     * @param action    the type of action performed
     * @param details   additional details about the change
     * @param snapshot  a snapshot of the job state after the change
     */
    public JobHistoryEntry(String jobId, Instant changedAt, String changedBy, String action, String details, String snapshot) {
        this.jobId = jobId;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.action = action;
        this.details = details;
        this.snapshot = snapshot;
    }

    /**
     * Returns the identifier of the job that was changed.
     *
     * @return the job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets the identifier of the job that was changed.
     *
     * @param jobId the job id to set
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Returns the timestamp when the change occurred.
     *
     * @return the change timestamp
     */
    public Instant getChangedAt() {
        return changedAt;
    }

    /**
     * Sets the timestamp when the change occurred.
     *
     * @param changedAt the change timestamp to set
     */
    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    /**
     * Returns the user who made the change.
     *
     * @return the user identifier
     */
    public String getChangedBy() {
        return changedBy;
    }

    /**
     * Sets the user who made the change.
     *
     * @param changedBy the user identifier to set
     */
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    /**
     * Returns the type of action performed (e.g. CREATED, UPDATED, CLOSED).
     *
     * @return the action type
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the type of action performed.
     *
     * @param action the action type to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns additional details about the change.
     *
     * @return the change details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets additional details about the change.
     *
     * @param details the change details to set
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Returns a snapshot of the job state after the change.
     *
     * @return the job snapshot
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets a snapshot of the job state after the change.
     *
     * @param snapshot the job snapshot to set
     */
    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }
}
