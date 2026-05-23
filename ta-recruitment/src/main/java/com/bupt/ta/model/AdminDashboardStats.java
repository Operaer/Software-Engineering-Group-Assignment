package com.bupt.ta.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin global recruitment dashboard view model.
 *
 * <p>This class stores all aggregated data required by the admin dashboard, including
 * university-wide recruitment statistics, module-level applicant metrics, TA workload
 * distribution, and module workload distribution.</p>
 *
 * <p>The servlet layer dynamically computes these values based on the latest position
 * and application records, then passes this object to the JSP page for rendering.</p>
 *
 * @author Wenqi Guan
 * @author Operaer
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

    /** Get total number of positions */
    public int getTotalPositions() {
        return totalPositions;
    }

    /** Set total number of positions */
    public void setTotalPositions(int totalPositions) {
        this.totalPositions = totalPositions;
    }

    /** Get number of open positions */
    public int getOpenPositions() {
        return openPositions;
    }

    /** Set number of open positions */
    public void setOpenPositions(int openPositions) {
        this.openPositions = openPositions;
    }

    /** Get number of closed positions */
    public int getClosedPositions() {
        return closedPositions;
    }

    /** Set number of closed positions */
    public void setClosedPositions(int closedPositions) {
        this.closedPositions = closedPositions;
    }

    /** Get number of archived positions */
    public int getArchivedPositions() {
        return archivedPositions;
    }

    /** Set number of archived positions */
    public void setArchivedPositions(int archivedPositions) {
        this.archivedPositions = archivedPositions;
    }

    /** Get total number of applications */
    public int getTotalApplications() {
        return totalApplications;
    }

    /** Set total number of applications */
    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    /** Get number of accepted applications */
    public int getAcceptedApplications() {
        return acceptedApplications;
    }

    /** Set number of accepted applications */
    public void setAcceptedApplications(int acceptedApplications) {
        this.acceptedApplications = acceptedApplications;
    }

    /** Get completion rate */
    public double getCompletionRate() {
        return completionRate;
    }

    /** Set completion rate */
    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    /** Get list of module metrics */
    public List<ModuleMetric> getModuleMetrics() {
        return moduleMetrics;
    }

    /** Set list of module metrics */
    public void setModuleMetrics(List<ModuleMetric> moduleMetrics) {
        this.moduleMetrics = moduleMetrics;
    }

    /** Get list of TA workload metrics */
    public List<TAWorkloadMetric> getTaWorkloadMetrics() {
        return taWorkloadMetrics;
    }

    /** Set list of TA workload metrics */
    public void setTaWorkloadMetrics(List<TAWorkloadMetric> taWorkloadMetrics) {
        this.taWorkloadMetrics = taWorkloadMetrics;
    }

    /** Get list of module workload metrics */
    public List<ModuleWorkloadMetric> getModuleWorkloadMetrics() {
        return moduleWorkloadMetrics;
    }

    /** Set list of module workload metrics */
    public void setModuleWorkloadMetrics(List<ModuleWorkloadMetric> moduleWorkloadMetrics) {
        this.moduleWorkloadMetrics = moduleWorkloadMetrics;
    }

    /**
     * Recruitment metrics aggregated by module code.
     */
    public static class ModuleMetric implements Serializable {
        private String moduleCode;
        private int positionCount;
        private int applicantCount;
        private int acceptedCount;
        private double completionRate;

        /** Get module code */
        public String getModuleCode() {
            return moduleCode;
        }

        /** Set module code */
        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        /** Get position count */
        public int getPositionCount() {
            return positionCount;
        }

        /** Set position count */
        public void setPositionCount(int positionCount) {
            this.positionCount = positionCount;
        }

        /** Get applicant count */
        public int getApplicantCount() {
            return applicantCount;
        }

        /** Set applicant count */
        public void setApplicantCount(int applicantCount) {
            this.applicantCount = applicantCount;
        }

        /** Get accepted count */
        public int getAcceptedCount() {
            return acceptedCount;
        }

        /** Set accepted count */
        public void setAcceptedCount(int acceptedCount) {
            this.acceptedCount = acceptedCount;
        }

        /** Get completion rate */
        public double getCompletionRate() {
            return completionRate;
        }

        /** Set completion rate */
        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }
    }

    /**
     * Workload assigned to a TA based on accepted applications.
     */
    public static class TAWorkloadMetric implements Serializable {
        private String taEmail;
        private int acceptedPositions;
        private int totalWorkload;
        private String modules;
        private String taName;

        /** Get TA email */
        public String getTaEmail() {
            return taEmail;
        }

        /** Set TA email */
        public void setTaEmail(String taEmail) {
            this.taEmail = taEmail;
        }

        /** Get number of accepted positions */
        public int getAcceptedPositions() {
            return acceptedPositions;
        }

        /** Set number of accepted positions */
        public void setAcceptedPositions(int acceptedPositions) {
            this.acceptedPositions = acceptedPositions;
        }

        /** Get total workload */
        public int getTotalWorkload() {
            return totalWorkload;
        }

        /** Set total workload */
        public void setTotalWorkload(int totalWorkload) {
            this.totalWorkload = totalWorkload;
        }

        /** Get assigned modules (comma-separated) */
        public String getModules() {
            return modules;
        }

        /** Set assigned modules */
        public void setModules(String modules) {
            this.modules = modules;
        }

        /** Get TA name */
        public String getTaName() {
            return taName;
        }

        /** Set TA name */
        public void setTaName(String taName) {
            this.taName = taName;
        }
    }

    /**
     * Workload aggregated by module based on hired TAs.
     */
    public static class ModuleWorkloadMetric implements Serializable {
        private String moduleCode;
        private int hiredTAs;
        private int acceptedPositions;
        private int totalWorkload;

        /** Get module code */
        public String getModuleCode() {
            return moduleCode;
        }

        /** Set module code */
        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        /** Get number of hired TAs */
        public int getHiredTAs() {
            return hiredTAs;
        }

        /** Set number of hired TAs */
        public void setHiredTAs(int hiredTAs) {
            this.hiredTAs = hiredTAs;
        }

        /** Get number of accepted positions */
        public int getAcceptedPositions() {
            return acceptedPositions;
        }

        /** Set number of accepted positions */
        public void setAcceptedPositions(int acceptedPositions) {
            this.acceptedPositions = acceptedPositions;
        }

        /** Get total workload */
        public int getTotalWorkload() {
            return totalWorkload;
        }

        /** Set total workload */
        public void setTotalWorkload(int totalWorkload) {
            this.totalWorkload = totalWorkload;
        }
    }
}
