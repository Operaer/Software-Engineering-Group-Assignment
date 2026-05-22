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
 * @author Operaer
 * @date 2026-05-17
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

        /**
         * Returns the module code for this metric.
         *
         * @return the module code
         */
        public String getModuleCode() {
            return moduleCode;
        }

        /**
         * Sets the module code for this metric.
         *
         * @param moduleCode the module code to set
         */
        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        /**
         * Returns the number of positions created for this module.
         *
         * @return the position count
         */
        public int getPositionCount() {
            return positionCount;
        }

        /**
         * Sets the number of positions created for this module.
         *
         * @param positionCount the position count to set
         */
        public void setPositionCount(int positionCount) {
            this.positionCount = positionCount;
        }

        /**
         * Returns the number of applicants for this module.
         *
         * @return the applicant count
         */
        public int getApplicantCount() {
            return applicantCount;
        }

        /**
         * Sets the number of applicants for this module.
         *
         * @param applicantCount the applicant count to set
         */
        public void setApplicantCount(int applicantCount) {
            this.applicantCount = applicantCount;
        }

        /**
         * Returns the number of accepted applicants for this module.
         *
         * @return the accepted count
         */
        public int getAcceptedCount() {
            return acceptedCount;
        }

        /**
         * Sets the number of accepted applicants for this module.
         *
         * @param acceptedCount the accepted count to set
         */
        public void setAcceptedCount(int acceptedCount) {
            this.acceptedCount = acceptedCount;
        }

        /**
         * Returns the completion rate (accepted / total positions) for this module.
         *
         * @return the completion rate
         */
        public double getCompletionRate() {
            return completionRate;
        }

        /**
         * Sets the completion rate for this module.
         *
         * @param completionRate the completion rate to set
         */
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
        private String taName;

        /**
         * Returns the TA's email address.
         *
         * @return the TA email
         */
        public String getTaEmail() {
            return taEmail;
        }

        /**
         * Sets the TA's email address.
         *
         * @param taEmail the TA email to set
         */
        public void setTaEmail(String taEmail) {
            this.taEmail = taEmail;
        }

        /**
         * Returns the number of accepted positions assigned to this TA.
         *
         * @return the accepted position count
         */
        public int getAcceptedPositions() {
            return acceptedPositions;
        }

        /**
         * Sets the number of accepted positions assigned to this TA.
         *
         * @param acceptedPositions the accepted position count to set
         */
        public void setAcceptedPositions(int acceptedPositions) {
            this.acceptedPositions = acceptedPositions;
        }

        /**
         * Returns the total workload hours assigned to this TA.
         *
         * @return the total workload hours
         */
        public int getTotalWorkload() {
            return totalWorkload;
        }

        /**
         * Sets the total workload hours assigned to this TA.
         *
         * @param totalWorkload the total workload hours to set
         */
        public void setTotalWorkload(int totalWorkload) {
            this.totalWorkload = totalWorkload;
        }

        /**
         * Returns the module names assigned to this TA.
         *
         * @return the module names as a string
         */
        public String getModules() {
            return modules;
        }

        /**
         * Returns the TA's display name.
         *
         * @return the TA name
         */
        public String getTaName() {
            return taName;
        }

        /**
         * Sets the TA's display name.
         *
         * @param taName the TA name to set
         */
        public void setTaName(String taName) {
            this.taName = taName;
        }

        /**
         * Sets the module names assigned to this TA.
         *
         * @param modules the module names to set
         */
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

        /**
         * Returns the module code for this workload metric.
         *
         * @return the module code
         */
        public String getModuleCode() {
            return moduleCode;
        }

        /**
         * Sets the module code for this workload metric.
         *
         * @param moduleCode the module code to set
         */
        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        /**
         * Returns the number of TAs hired for this module.
         *
         * @return the hired TA count
         */
        public int getHiredTAs() {
            return hiredTAs;
        }

        /**
         * Sets the number of TAs hired for this module.
         *
         * @param hiredTAs the hired TA count to set
         */
        public void setHiredTAs(int hiredTAs) {
            this.hiredTAs = hiredTAs;
        }

        /**
         * Returns the number of accepted positions for this module.
         *
         * @return the accepted position count
         */
        public int getAcceptedPositions() {
            return acceptedPositions;
        }

        /**
         * Sets the number of accepted positions for this module.
         *
         * @param acceptedPositions the accepted position count to set
         */
        public void setAcceptedPositions(int acceptedPositions) {
            this.acceptedPositions = acceptedPositions;
        }

        /**
         * Returns the total workload hours for this module.
         *
         * @return the total workload hours
         */
        public int getTotalWorkload() {
            return totalWorkload;
        }

        /**
         * Sets the total workload hours for this module.
         *
         * @param totalWorkload the total workload hours to set
         */
        public void setTotalWorkload(int totalWorkload) {
            this.totalWorkload = totalWorkload;
        }
    }
}
