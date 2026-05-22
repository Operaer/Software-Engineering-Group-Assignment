package com.bupt.ta.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Teaching Assistant (TA) profile in the recruitment system.
 *
 * <p>Each TA profile stores personal information including email, name, student ID,
 * major field of study, phone number, GPA, a list of skills, and the filename of the
 * uploaded resume. Profiles are identified by the TA's email address.</p>
 */
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

    /**
     * Constructs an empty TAProfile for deserialization.
     */
    public TAProfile() {
    }

    /**
     * Constructs a TAProfile identified by the given email address.
     *
     * @param email the TA's email address
     */
    public TAProfile(String email) {
        this.email = email;
    }

    /**
     * Returns the TA's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the TA's email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the TA's full name.
     *
     * @return the full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the TA's full name.
     *
     * @param name the full name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the TA's student ID.
     *
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Sets the TA's student ID.
     *
     * @param studentId the student ID to set
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * Returns the TA's major field of study.
     *
     * @return the major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Sets the TA's major field of study.
     *
     * @param major the major to set
     */
    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * Returns the TA's phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the TA's phone number.
     *
     * @param phone the phone number to set
     */
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

    /**
     * Returns the list of skills reported by the TA.
     *
     * @return the list of skills
     */
    public List<String> getSkills() {
        return skills;
    }

    /**
     * Sets the list of skills reported by the TA.
     *
     * @param skills the list of skills to set
     */
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    /**
     * Returns the filename of the TA's uploaded resume.
     *
     * @return the resume filename, or {@code null} if no resume was uploaded
     */
    public String getResumeFileName() {
        return resumeFileName;
    }

    /**
     * Sets the filename of the TA's uploaded resume.
     *
     * @param resumeFileName the resume filename to set
     */
    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }
}
