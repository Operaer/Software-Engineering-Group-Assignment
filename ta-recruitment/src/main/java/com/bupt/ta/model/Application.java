package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a TA application record in the recruitment system.
 *
 * <p>Applications track the TA email, the position applied for, the application
 * timestamp, current status, and the assigned workload hours when adjusted by an
 * administrator.</p>
 *
 * @author Operaer
 * @date 2026-05-17
 */
public class Application implements Serializable {
    private String id;
    private String taEmail;
    private String positionId;
    private String positionTitle;
    private Instant appliedAt;
    private String status; // Pending / Shortlisted / Accepted / Rejected
    private Integer assignedWorkloadHours;

    public Application() {
    }

    public Application(String id, String taEmail, String positionId, String positionTitle, Instant appliedAt, String status) {
        this.id = id;
        this.taEmail = taEmail;
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.appliedAt = appliedAt;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaEmail() {
        return taEmail;
    }

    public void setTaEmail(String taEmail) {
        this.taEmail = taEmail;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAssignedWorkloadHours() {
        return assignedWorkloadHours;
    }

    public void setAssignedWorkloadHours(Integer assignedWorkloadHours) {
        this.assignedWorkloadHours = assignedWorkloadHours;
    }

    public enum Status {
        Pending,
        Shortlisted,
        Accepted,
        Rejected,
        Expired
    }
}
