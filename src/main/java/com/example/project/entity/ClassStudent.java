package com.example.project.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "class_students")
public class ClassStudent {

    @EmbeddedId
    private ClassStudentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("classId")
    @JoinColumn(name = "class_id")
    private TrainingClass trainingClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDate joinedDate;
    private LocalDate leaveDate;

    @Column(nullable = false)
    private String roleInClass;

    public ClassStudent() {
    }

    public ClassStudentId getId() {
        return id;
    }

    public void setId(ClassStudentId id) {
        this.id = id;
    }

    public TrainingClass getTrainingClass() {
        return trainingClass;
    }

    public void setTrainingClass(TrainingClass trainingClass) {
        this.trainingClass = trainingClass;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getRoleInClass() {
        return roleInClass;
    }

    public void setRoleInClass(String roleInClass) {
        this.roleInClass = roleInClass;
    }
}
