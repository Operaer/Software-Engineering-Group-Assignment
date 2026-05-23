package com.bupt.ta.config;

/**
 * Application-wide configuration class.
 * Centralizes constants used throughout the system, including default password,
 * upload directory, data directory, and path definitions for various data files,
 * facilitating unified maintenance and modification.
 */
public class AppConfig {
    public static final String DEFAULT_PASSWORD = "default123";
    public static final String UPLOAD_DIR = "/WEB-INF/uploads";
    public static final String DATA_DIR = "/WEB-INF/data";
    public static final String USERS_FILE = DATA_DIR + "/users.json";
    public static final String APPLICATIONS_FILE = DATA_DIR + "/applications.json";
    public static final String PROFILES_FILE = DATA_DIR + "/ta_profiles.json";
    public static final String JOBS_FILE = DATA_DIR + "/jobs.txt";
    public static final String JOB_HISTORY_FILE = DATA_DIR + "/job_history.txt";
    public static final String AUDIT_LOG_FILE = DATA_DIR + "/audit_logs.json";
}
