package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class StudentModel {

    private String schoolName;
    private String department;
    private String studentId;
    private String studentIdThumb;
    private String approval;

    public StudentModel() {
    }

    public StudentModel(String schoolName, String department, String studentId, String studentIdThumb, String approval) {
        this.schoolName = schoolName;
        this.department = department;
        this.studentId = studentId;
        this.studentIdThumb = studentIdThumb;
        this.approval = approval;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentIdThumb() {
        return studentIdThumb;
    }

    public void setStudentIdThumb(String studentIdThumb) {
        this.studentIdThumb = studentIdThumb;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }
}
