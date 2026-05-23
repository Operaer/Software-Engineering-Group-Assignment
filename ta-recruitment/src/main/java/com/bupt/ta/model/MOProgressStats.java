package com.bupt.ta.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MO (Module Organiser) recruitment progress dashboard view model.
 *
 * <p>This class stores dynamically computed recruitment progress data for the
 * Module Organiser to review. These values are recalculated on each page load
 * based on the latest position and application storage records, ensuring that
 * applicant counts and status statistics stay consistent with application
 * status updates.</p>
 *
 * @author Wenqi Guan
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

    /** Get total number of positions */
    public int getTotalPositions() {
        return totalPositions;
    }

    /** Set total number of positions */
    public void setTotalPositions(int totalPositions) {
        this.totalPositions = totalPositions;
    }

    /** Get number of completed positions */
    public int getCompletedPositions() {
        return completedPositions;
    }

    /** Set number of completed positions */
    public void setCompletedPositions(int completedPositions) {
        this.completedPositions = completedPositions;
    }

    /** Get number of in-progress positions */
    public int getInProgressPositions() {
        return inProgressPositions;
    }

    /** Set number of in-progress positions */
    public void setInProgressPositions(int inProgressPositions) {
        this.inProgressPositions = inProgressPositions;
    }

    /** Get number of urgent positions */
    public int getUrgentPositions() {
        return urgentPositions;
    }

    /** Set number of urgent positions */
    public void setUrgentPositions(int urgentPositions) {
        this.urgentPositions = urgentPositions;
    }

    /** Get total number of applicants */
    public int getTotalApplicants() {
        return totalApplicants;
    }

    /** Set total number of applicants */
    public void setTotalApplicants(int totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    /** Get total number of shortlisted */
    public int getTotalShortlisted() {
        return totalShortlisted;
    }

    /** Set total number of shortlisted */
    public void setTotalShortlisted(int totalShortlisted) {
        this.totalShortlisted = totalShortlisted;
    }

    /** Get total number of accepted */
    public int getTotalAccepted() {
        return totalAccepted;
    }

    /** Set total number of accepted */
    public void setTotalAccepted(int totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    /** Get list of position progress entries */
    public List<PositionProgress> getPositions() {
        return positions;
    }

    /** Set list of position progress entries */
    public void setPositions(List<PositionProgress> positions) {
        this.positions = positions;
    }

    /**
     * Progress information for a single position, used for MO05 dashboard table and card display.
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

        /** Get position ID */
        public String getJobId() {
            return jobId;
        }

        /** Set position ID */
        public void setJobId(String jobId) {
            this.jobId = jobId;
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

        /** Get job status */
        public String getJobStatus() {
            return jobStatus;
        }

        /** Set job status */
        public void setJobStatus(String jobStatus) {
            this.jobStatus = jobStatus;
        }

        /** Get deadline */
        public LocalDate getDeadline() {
            return deadline;
        }

        /** Set deadline */
        public void setDeadline(LocalDate deadline) {
            this.deadline = deadline;
        }

        /** Get days remaining */
        public long getDaysRemaining() {
            return daysRemaining;
        }

        /** Set days remaining */
        public void setDaysRemaining(long daysRemaining) {
            this.daysRemaining = daysRemaining;
        }

        /** Get applicant count */
        public int getApplicantCount() {
            return applicantCount;
        }

        /** Set applicant count */
        public void setApplicantCount(int applicantCount) {
            this.applicantCount = applicantCount;
        }

        /** Get shortlisted count */
        public int getShortlistedCount() {
            return shortlistedCount;
        }

        /** Set shortlisted count */
        public void setShortlistedCount(int shortlistedCount) {
            this.shortlistedCount = shortlistedCount;
        }

        /** Get accepted count */
        public int getAcceptedCount() {
            return acceptedCount;
        }

        /** Set accepted count */
        public void setAcceptedCount(int acceptedCount) {
            this.acceptedCount = acceptedCount;
        }

        /** Get progress label text */
        public String getProgressLabel() {
            return progressLabel;
        }

        /** Set progress label text */
        public void setProgressLabel(String progressLabel) {
            this.progressLabel = progressLabel;
        }

        /** Get badge CSS class */
        public String getBadgeClass() {
            return badgeClass;
        }

        /** Set badge CSS class */
        public void setBadgeClass(String badgeClass) {
            this.badgeClass = badgeClass;
        }

        /** Get progress percentage */
        public int getProgressPercent() {
            return progressPercent;
        }

        /** Set progress percentage */
        public void setProgressPercent(int progressPercent) {
            this.progressPercent = progressPercent;
        }
    }
}
