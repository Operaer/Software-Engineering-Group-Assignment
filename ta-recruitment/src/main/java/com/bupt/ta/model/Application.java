package com.bupt.ta.model;

import java.io.Serializable;
import java.time.Instant;

public class Application implements Serializable {
    private String id;
    private String taEmail;
    private String positionId;
    private String positionTitle;
    private Instant appliedAt;
    private String status; // Pending / Shortlisted / Accepted / Rejected

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
}
