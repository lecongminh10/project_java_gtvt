package com.example.project.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    //        @ManyToOne
    @JoinColumn(name = "subject_id", nullable = true)
    private Long subjectId;

    private Integer sessionsCount;

    private BigDecimal fee;

    private String description;

    private Integer durationWeeks;

    @Enumerated(EnumType.STRING)
    private ClassStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Course() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getSessionsCount() {
        return sessionsCount;
    }

    public void setSessionsCount(Integer sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(Integer durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
