package com.bupt.ta.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TAProfile implements Serializable {
    private String email;
    private String name;
    private String studentId;
    private String major;
    private String phone;
    /** GPA reported by the TA for applicant display and MO-side sorting. */
    private Double gpa;
    private List<String> skills = new ArrayList<>();
    private String resumeFileName;

    public TAProfile() {
    }

    public TAProfile(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the GPA reported by the TA.
     *
     * @return GPA value, or {@code null} when the TA has not provided one
     */
    public Double getGpa() {
        return gpa;
    }

    /**
     * Stores the GPA reported by the TA.
     *
     * @param gpa GPA value to save
     */
    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }
}
