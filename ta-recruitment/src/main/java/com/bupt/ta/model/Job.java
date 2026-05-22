package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents a TA position (job posting) in the recruitment system.
 *
 * <p>A job defines a TA position for a specific module, including the title,
 * workload description, requirements, application deadline, and current status.
 * Jobs are posted by administrators (MO users) and can be in Open, Closed, or
 * Archived states.</p>
 */
public class Job implements Serializable {
    /** Status constant indicating the position is open for applications. */
    public static final String STATUS_OPEN = "Open";
    /** Status constant indicating the position has been archived. */
    public static final String STATUS_ARCHIVED = "Archived";
    /** Status constant indicating the position is closed (no longer accepting applications). */
    public static final String STATUS_CLOSED = "Closed";

    private String id;
    private String title;
    private String moduleCode;
    private String workload;
    private String requirements;
    private LocalDate deadline;
    private String postedBy;
    private Instant postedAt;
    private Instant updatedAt;
    private String status = STATUS_OPEN;

    /**
     * Constructs an empty Job for deserialization.
     */
    public Job() {
    }

    /**
     * Constructs a Job with the given fields, defaulting status to {@link #STATUS_OPEN}.
     *
     * @param id           the unique job identifier
     * @param title        the job title
     * @param moduleCode   the module code this position belongs to
     * @param workload     the workload description
     * @param requirements the position requirements
     * @param deadline     the application deadline
     * @param postedBy     the user who posted the job
     * @param postedAt     the timestamp when the job was posted
     */
    public Job(String id, String title, String moduleCode, String workload, String requirements, LocalDate deadline, String postedBy, Instant postedAt) {
        this.id = id;
        this.title = title;
        this.moduleCode = moduleCode;
        this.workload = workload;
        this.requirements = requirements;
        this.deadline = deadline;
        this.postedBy = postedBy;
        this.postedAt = postedAt;
        this.updatedAt = postedAt;
        this.status = STATUS_OPEN;
    }

    // Getters and Setters

    /**
     * Returns the unique identifier of this job.
     *
     * @return the job id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this job.
     *
     * @param id the job id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the title of this job position.
     *
     * @return the job title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this job position.
     *
     * @param title the job title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the module code this position belongs to.
     *
     * @return the module code
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * Sets the module code this position belongs to.
     *
     * @param moduleCode the module code to set
     */
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    /**
     * Returns the workload description for this position.
     *
     * @return the workload description
     */
    public String getWorkload() {
        return workload;
    }

    /**
     * Sets the workload description for this position.
     *
     * @param workload the workload description to set
     */
    public void setWorkload(String workload) {
        this.workload = workload;
    }

    /**
     * Returns the requirements for this position.
     *
     * @return the requirements text
     */
    public String getRequirements() {
        return requirements;
    }

    /**
     * Sets the requirements for this position.
     *
     * @param requirements the requirements text to set
     */
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    /**
     * Returns the application deadline for this position.
     *
     * @return the deadline date
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * Sets the application deadline for this position.
     *
     * @param deadline the deadline date to set
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    /**
     * Returns the user who posted this job.
     *
     * @return the poster identifier
     */
    public String getPostedBy() {
        return postedBy;
    }

    /**
     * Sets the user who posted this job.
     *
     * @param postedBy the poster identifier to set
     */
    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    /**
     * Returns the timestamp when this job was posted.
     *
     * @return the posting timestamp
     */
    public Instant getPostedAt() {
        return postedAt;
    }

    /**
     * Sets the timestamp when this job was posted.
     *
     * @param postedAt the posting timestamp to set
     */
    public void setPostedAt(Instant postedAt) {
        this.postedAt = postedAt;
    }

    /**
     * Returns the timestamp when this job was last updated.
     *
     * @return the last update timestamp
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when this job was last updated.
     *
     * @param updatedAt the last update timestamp to set
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the current status of this job.
     *
     * @return the status string (Open, Closed, Archived)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of this job.
     *
     * @param status the status string to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
