package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * TA application record model in the TA recruitment system.
 *
 * <p>The application record contains the applicant's TA email, the applied position
 * information, application timestamp, current status, and the assigned workload hours
 * adjusted by the admin.</p>
 *
 * @author Operaer
 * @since 2026-05-17
 */
public class Application implements Serializable {
    private String id;
    private String taEmail;
    private String positionId;
    private String positionTitle;
    private Instant appliedAt;
    private String status; // Pending / Shortlisted / Accepted / Rejected
    private Integer assignedWorkloadHours;

    /** No-arg constructor */
    public Application() {
    }

    /**
     * All-args constructor.
     *
     * @param id         Application ID
     * @param taEmail    Applicant TA email
     * @param positionId Position ID
     * @param positionTitle Position title
     * @param appliedAt  Application time
     * @param status     Application status
     */
    public Application(String id, String taEmail, String positionId, String positionTitle, Instant appliedAt, String status) {
        this.id = id;
        this.taEmail = taEmail;
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.appliedAt = appliedAt;
        this.status = status;
    }

    /** Get application ID */
    public String getId() {
        return id;
    }

    /** Set application ID */
    public void setId(String id) {
        this.id = id;
    }

    /** Get applicant TA email */
    public String getTaEmail() {
        return taEmail;
    }

    /** Set applicant TA email */
    public void setTaEmail(String taEmail) {
        this.taEmail = taEmail;
    }

    /** Get position ID */
    public String getPositionId() {
        return positionId;
    }

    /** Set position ID */
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    /** Get position title */
    public String getPositionTitle() {
        return positionTitle;
    }

    /** Set position title */
    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    /** Get application time */
    public Instant getAppliedAt() {
        return appliedAt;
    }

    /** Set application time */
    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    /** Get application status */
    public String getStatus() {
        return status;
    }

    /** Set application status */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Get assigned workload hours */
    public Integer getAssignedWorkloadHours() {
        return assignedWorkloadHours;
    }

    /** Set assigned workload hours */
    public void setAssignedWorkloadHours(Integer assignedWorkloadHours) {
        this.assignedWorkloadHours = assignedWorkloadHours;
    }

    /**
     * Application status enumeration.
     */
    public enum Status {
        Pending,
        Shortlisted,
        Accepted,
        Rejected,
        Expired
    }
}
