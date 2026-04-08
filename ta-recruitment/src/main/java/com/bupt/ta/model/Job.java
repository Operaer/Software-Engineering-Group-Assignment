package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class Job implements Serializable {
    private String id;
    private String title;
    private String moduleCode;
    private String workload;
    private String requirements;
    private LocalDate deadline;
    private String postedBy;
    private Instant postedAt;

    public Job() {
    }

    public Job(String id, String title, String moduleCode, String workload, String requirements, LocalDate deadline, String postedBy, Instant postedAt) {
        this.id = id;
        this.title = title;
        this.moduleCode = moduleCode;
        this.workload = workload;
        this.requirements = requirements;
        this.deadline = deadline;
        this.postedBy = postedBy;
        this.postedAt = postedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Instant postedAt) {
        this.postedAt = postedAt;
    }
}