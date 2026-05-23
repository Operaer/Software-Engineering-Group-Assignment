package com.bupt.ta.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TA personal profile model in the TA recruitment system.
 *
 * <p>Stores the TA's basic personal information, including name, student ID, major,
 * phone, GPA, skill list, and resume filename. This information is displayed to
 * the Module Organiser during the application process and is used for TA screening
 * and sorting.</p>
 *
 * @author Operaer
 * @since 2026-05-17
 */
public class TAProfile implements Serializable {
    private String email;
    private String name;
    private String studentId;
    private String major;
    private String phone;
    /** GPA (self-reported by TA, used for list display and sorting on the MO side) */
    private Double gpa;
    private List<String> skills = new ArrayList<>();
    private String resumeFileName;

    /** No-arg constructor */
    public TAProfile() {
    }

    /**
     * Constructs a TA profile by email.
     *
     * @param email TA email
     */
    public TAProfile(String email) {
        this.email = email;
    }

    /** Get email */
    public String getEmail() {
        return email;
    }

    /** Set email */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Get name */
    public String getName() {
        return name;
    }

    /** Set name */
    public void setName(String name) {
        this.name = name;
    }

    /** Get student ID */
    public String getStudentId() {
        return studentId;
    }

    /** Set student ID */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /** Get major */
    public String getMajor() {
        return major;
    }

    /** Set major */
    public void setMajor(String major) {
        this.major = major;
    }

    /** Get phone number */
    public String getPhone() {
        return phone;
    }

    /** Set phone number */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Get GPA */
    public Double getGpa() {
        return gpa;
    }

    /** Set GPA */
    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    /** Get skill list */
    public List<String> getSkills() {
        return skills;
    }

    /** Set skill list */
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    /** Get resume filename */
    public String getResumeFileName() {
        return resumeFileName;
    }

    /** Set resume filename */
    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }
}
