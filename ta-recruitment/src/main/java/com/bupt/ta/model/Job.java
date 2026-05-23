package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * TA position (job) model in the TA recruitment system.
 *
 * <p>The position contains basic information such as title, module code, workload,
 * requirements, deadline, as well as tracking information for the publisher and
 * publish time. The status field indicates the current state of the position
 * (Open / Closed / Archived).</p>
 *
 * @author Operaer
 * @since 2026-05-17
 */
public class Job implements Serializable {
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_ARCHIVED = "Archived";
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

    /** No-arg constructor */
    public Job() {
    }

    /**
     * All-args constructor. Status defaults to Open on creation, updatedAt equals postedAt.
     *
     * @param id          Position ID
     * @param title       Position title
     * @param moduleCode  Module code
     * @param workload    Workload description
     * @param requirements Position requirements
     * @param deadline    Application deadline
     * @param postedBy    Publisher
     * @param postedAt    Publish time
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

    /** Get position ID */
    public String getId() {
        return id;
    }

    /** Set position ID */
    public void setId(String id) {
        this.id = id;
    }

    /** Get position title */
    public String getTitle() {
        return title;
    }

    /** Set position title */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Get module code */
    public String getModuleCode() {
        return moduleCode;
    }

    /** Set module code */
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    /** Get workload description */
    public String getWorkload() {
        return workload;
    }

    /** Set workload description */
    public void setWorkload(String workload) {
        this.workload = workload;
    }

    /** Get position requirements */
    public String getRequirements() {
        return requirements;
    }

    /** Set position requirements */
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    /** Get application deadline */
    public LocalDate getDeadline() {
        return deadline;
    }

    /** Set application deadline */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    /** Get publisher */
    public String getPostedBy() {
        return postedBy;
    }

    /** Set publisher */
    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    /** Get publish time */
    public Instant getPostedAt() {
        return postedAt;
    }

    /** Set publish time */
    public void setPostedAt(Instant postedAt) {
        this.postedAt = postedAt;
    }

    /** Get last update time */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Set last update time */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Get position status (Open / Closed / Archived) */
    public String getStatus() {
        return status;
    }

    /** Set position status */
    public void setStatus(String status) {
        this.status = status;
    }
}
