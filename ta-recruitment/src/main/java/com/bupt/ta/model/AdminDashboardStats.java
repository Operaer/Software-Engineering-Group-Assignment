package com.bupt.ta.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * View model for the US03 Admin Global Recruitment Dashboard.
 *
 * <p>
 * This class stores all aggregated data required by the administrator dashboard,
 * including school-wide recruitment statistics, module-level applicant metrics,
 * TA workload distribution, and module workload distribution.
 * </p>
 *
 * <p>
 * The servlet layer calculates these values dynamically from the latest job and
 * application records, then passes this object to the JSP page for rendering.
 * </p>
 *
 * @author Wenqi Guan
 * @version 1.0
 * @since 2026-05-09
 */
public class AdminDashboardStats implements Serializable {
    private int totalPositions;
    private int openPositions;
    private int closedPositions;
    private int archivedPositions;
    private int totalApplications;
    private int acceptedApplications;
    private double completionRate;
    private List<ModuleMetric> moduleMetrics = new ArrayList<>();
    private List<TAWorkloadMetric> taWorkloadMetrics = new ArrayList<>();
    private List<ModuleWorkloadMetric> moduleWorkloadMetrics = new ArrayList<>();

    public int getTotalPositions() {
        return totalPositions;
    }

    public void setTotalPositions(int totalPositions) {
        this.totalPositions = totalPositions;
    }

    public int getOpenPositions() {
        return openPositions;
    }

    public void setOpenPositions(int openPositions) {
        this.openPositions = openPositions;
    }

    public int getClosedPositions() {
        return closedPositions;
    }

    public void setClosedPositions(int closedPositions) {
        this.closedPositions = closedPositions;
    }

    public int getArchivedPositions() {
        return archivedPositions;
    }

    public void setArchivedPositions(int archivedPositions) {
        this.archivedPositions = archivedPositions;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getAcceptedApplications() {
        return acceptedApplications;
    }

    public void setAcceptedApplications(int acceptedApplications) {
        this.acceptedApplications = acceptedApplications;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public List<ModuleMetric> getModuleMetrics() {
        return moduleMetrics;
    }

    public void setModuleMetrics(List<ModuleMetric> moduleMetrics) {
        this.moduleMetrics = moduleMetrics;
    }

    public List<TAWorkloadMetric> getTaWorkloadMetrics() {
        return taWorkloadMetrics;
    }

    public void setTaWorkloadMetrics(List<TAWorkloadMetric> taWorkloadMetrics) {
        this.taWorkloadMetrics = taWorkloadMetrics;
    }

    public List<ModuleWorkloadMetric> getModuleWorkloadMetrics() {
        return moduleWorkloadMetrics;
    }

    public void setModuleWorkloadMetrics(List<ModuleWorkloadMetric> moduleWorkloadMetrics) {
        this.moduleWorkloadMetrics = moduleWorkloadMetrics;
    }

    /**
     * Recruitment metrics aggregated for one module code.
     */
    public static class ModuleMetric implements Serializable {
        private String moduleCode;
        private int positionCount;
        private int applicantCount;
        private int acceptedCount;
        private double completionRate;

        public String getModuleCode() {
            return moduleCode;
        }

        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        public int getPositionCount() {
            return positionCount;
        }

        public void setPositionCount(int positionCount) {
            this.positionCount = positionCount;
        }

        public int getApplicantCount() {
            return applicantCount;
        }

        public void setApplicantCount(int applicantCount) {
            this.applicantCount = applicantCount;
        }

        public int getAcceptedCount() {
            return acceptedCount;
        }

        public void setAcceptedCount(int acceptedCount) {
            this.acceptedCount = acceptedCount;
        }

        public double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }
    }

    /**
     * Workload assigned to one TA based on accepted applications.
     */
    public static class TAWorkloadMetric implements Serializable {
        private String taEmail;
        private int acceptedPositions;
        private int totalWorkload;
        private String modules;

        public String getTaEmail() {
            return taEmail;
        }

        public void setTaEmail(String taEmail) {
            this.taEmail = taEmail;
        }

        public int getAcceptedPositions() {
            return acceptedPositions;
        }

        public void setAcceptedPositions(int acceptedPositions) {
            this.acceptedPositions = acceptedPositions;
        }

        public int getTotalWorkload() {
            return totalWorkload;
        }

        public void setTotalWorkload(int totalWorkload) {
            this.totalWorkload = totalWorkload;
        }

        public String getModules() {
            return modules;
        }

        public void setModules(String modules) {
            this.modules = modules;
        }
    }

    /**
     * Workload aggregated for one module based on hired TAs.
     */
    public static class ModuleWorkloadMetric implements Serializable {
        private String moduleCode;
        private int hiredTAs;
        private int acceptedPositions;
        private int totalWorkload;

        public String getModuleCode() {
            return moduleCode;
        }

        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        public int getHiredTAs() {
            return hiredTAs;
        }

        public void setHiredTAs(int hiredTAs) {
            this.hiredTAs = hiredTAs;
        }

        public int getAcceptedPositions() {
            return acceptedPositions;
        }

        public void setAcceptedPositions(int acceptedPositions) {
            this.acceptedPositions = acceptedPositions;
        }

        public int getTotalWorkload() {
            return totalWorkload;
        }

        public void setTotalWorkload(int totalWorkload) {
            this.totalWorkload = totalWorkload;
        }
    }
}
