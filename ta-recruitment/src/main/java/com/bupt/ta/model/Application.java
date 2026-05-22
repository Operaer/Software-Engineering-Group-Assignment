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

    /**
     * Constructs an empty Application for deserialization.
     */
    public Application() {
    }

    /**
     * Constructs an Application with the given fields.
     *
     * @param id              the unique application identifier
     * @param taEmail         the email of the TA who applied
     * @param positionId      the identifier of the position applied to
     * @param positionTitle   the title of the position applied to
     * @param appliedAt       the timestamp when the application was submitted
     * @param status          the initial application status
     */
    public Application(String id, String taEmail, String positionId, String positionTitle, Instant appliedAt, String status) {
        this.id = id;
        this.taEmail = taEmail;
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.appliedAt = appliedAt;
        this.status = status;
    }

    /**
     * Returns the unique identifier of this application.
     *
     * @return the application id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this application.
     *
     * @param id the application id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the email of the TA who submitted this application.
     *
     * @return the TA email
     */
    public String getTaEmail() {
        return taEmail;
    }

    /**
     * Sets the email of the TA who submitted this application.
     *
     * @param taEmail the TA email to set
     */
    public void setTaEmail(String taEmail) {
        this.taEmail = taEmail;
    }

    /**
     * Returns the identifier of the position this application targets.
     *
     * @return the position id
     */
    public String getPositionId() {
        return positionId;
    }

    /**
     * Sets the identifier of the position this application targets.
     *
     * @param positionId the position id to set
     */
    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    /**
     * Returns the title of the position this application targets.
     *
     * @return the position title
     */
    public String getPositionTitle() {
        return positionTitle;
    }

    /**
     * Sets the title of the position this application targets.
     *
     * @param positionTitle the position title to set
     */
    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    /**
     * Returns the timestamp when this application was submitted.
     *
     * @return the application timestamp
     */
    public Instant getAppliedAt() {
        return appliedAt;
    }

    /**
     * Sets the timestamp when this application was submitted.
     *
     * @param appliedAt the application timestamp to set
     */
    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    /**
     * Returns the current status of this application.
     *
     * @return the status string (Pending, Shortlisted, Accepted, Rejected)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of this application.
     *
     * @param status the status string to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the workload hours assigned by an administrator.
     *
     * @return the assigned workload hours, or {@code null} if not yet assigned
     */
    public Integer getAssignedWorkloadHours() {
        return assignedWorkloadHours;
    }

    /**
     * Sets the workload hours assigned to this application.
     *
     * @param assignedWorkloadHours the workload hours to set
     */
    public void setAssignedWorkloadHours(Integer assignedWorkloadHours) {
        this.assignedWorkloadHours = assignedWorkloadHours;
    }

    /**
     * Represents the possible statuses of a TA application.
     */
    public enum Status {
        Pending,
        Shortlisted,
        Accepted,
        Rejected,
        Expired
    }
}
