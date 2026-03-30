package com.example.project.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClassStudentId implements Serializable {

    private Long classId;
    private Long studentId;

    public ClassStudentId() {
    }

    public ClassStudentId(Long classId, Long studentId) {
        this.classId = classId;
        this.studentId = studentId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ClassStudentId))
            return false;
        ClassStudentId that = (ClassStudentId) o;
        return Objects.equals(getClassId(), that.getClassId()) && Objects.equals(getStudentId(), that.getStudentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassId(), getStudentId());
    }
}
