package com.bupt.ta.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * View model for the MO05 Recruitment Progress Dashboard.
 *
 * <p>This class stores the dynamically calculated recruitment progress data
 * shown to Module Organisers. Each page load recalculates these values from
 * the latest job and application storage records, so applicant counts and
 * status totals stay consistent with application status updates.</p>
 *
 * @author Wenqi Guan
 * @version 1.0
 * @since 2026-05-21
 */
public class MOProgressStats implements Serializable {
    private int totalPositions;
    private int completedPositions;
    private int inProgressPositions;
    private int urgentPositions;
    private int totalApplicants;
    private int totalShortlisted;
    private int totalAccepted;
    private List<PositionProgress> positions = new ArrayList<>();

    public int getTotalPositions() {
        return totalPositions;
    }

    public void setTotalPositions(int totalPositions) {
        this.totalPositions = totalPositions;
    }

    public int getCompletedPositions() {
        return completedPositions;
    }

    public void setCompletedPositions(int completedPositions) {
        this.completedPositions = completedPositions;
    }

    public int getInProgressPositions() {
        return inProgressPositions;
    }

    public void setInProgressPositions(int inProgressPositions) {
        this.inProgressPositions = inProgressPositions;
    }

    public int getUrgentPositions() {
        return urgentPositions;
    }

    public void setUrgentPositions(int urgentPositions) {
        this.urgentPositions = urgentPositions;
    }

    public int getTotalApplicants() {
        return totalApplicants;
    }

    public void setTotalApplicants(int totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    public int getTotalShortlisted() {
        return totalShortlisted;
    }

    public void setTotalShortlisted(int totalShortlisted) {
        this.totalShortlisted = totalShortlisted;
    }

    public int getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(int totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public List<PositionProgress> getPositions() {
        return positions;
    }

    public void setPositions(List<PositionProgress> positions) {
        this.positions = positions;
    }

    /**
     * Per-position progress row used by the MO05 dashboard table and cards.
     */
    public static class PositionProgress implements Serializable {
        private String jobId;
        private String title;
        private String moduleCode;
        private String jobStatus;
        private LocalDate deadline;
        private long daysRemaining;
        private int applicantCount;
        private int shortlistedCount;
        private int acceptedCount;
        private String progressLabel;
        private String badgeClass;
        private int progressPercent;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
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

        public String getJobStatus() {
            return jobStatus;
        }

        public void setJobStatus(String jobStatus) {
            this.jobStatus = jobStatus;
        }

        public LocalDate getDeadline() {
            return deadline;
        }

        public void setDeadline(LocalDate deadline) {
            this.deadline = deadline;
        }

        public long getDaysRemaining() {
            return daysRemaining;
        }

        public void setDaysRemaining(long daysRemaining) {
            this.daysRemaining = daysRemaining;
        }

        public int getApplicantCount() {
            return applicantCount;
        }

        public void setApplicantCount(int applicantCount) {
            this.applicantCount = applicantCount;
        }

        public int getShortlistedCount() {
            return shortlistedCount;
        }

        public void setShortlistedCount(int shortlistedCount) {
            this.shortlistedCount = shortlistedCount;
        }

        public int getAcceptedCount() {
            return acceptedCount;
        }

        public void setAcceptedCount(int acceptedCount) {
            this.acceptedCount = acceptedCount;
        }

        public String getProgressLabel() {
            return progressLabel;
        }

        public void setProgressLabel(String progressLabel) {
            this.progressLabel = progressLabel;
        }

        public String getBadgeClass() {
            return badgeClass;
        }

        public void setBadgeClass(String badgeClass) {
            this.badgeClass = badgeClass;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public void setProgressPercent(int progressPercent) {
            this.progressPercent = progressPercent;
        }
    }
}
