package com.example.project.dto;

import com.example.project.entity.UserStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TeacherDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private LocalDate dob;
    private String email;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Salary
    private BigDecimal salary;
    private String salaryCurrency;
    private String salaryPayPeriod;

    // Contract
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String contractType;

    // Performance
    private Double performanceRating;
    private LocalDateTime lastPerformanceReviewDate;
    private String lastReviewerNotes;

    // Relationships
    private UserDTO userInfo;

    // Constructors
    public TeacherDTO() {
    }

    public TeacherDTO(Long id, String code, String name, String email, UserStatus status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.email = email;
        this.status = status;
    }

    // Getters and Setters
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

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public String getSalaryPayPeriod() {
        return salaryPayPeriod;
    }

    public void setSalaryPayPeriod(String salaryPayPeriod) {
        this.salaryPayPeriod = salaryPayPeriod;
    }

    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public Double getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(Double performanceRating) {
        this.performanceRating = performanceRating;
    }

    public LocalDateTime getLastPerformanceReviewDate() {
        return lastPerformanceReviewDate;
    }

    public void setLastPerformanceReviewDate(LocalDateTime lastPerformanceReviewDate) {
        this.lastPerformanceReviewDate = lastPerformanceReviewDate;
    }

    public String getLastReviewerNotes() {
        return lastReviewerNotes;
    }

    public void setLastReviewerNotes(String lastReviewerNotes) {
        this.lastReviewerNotes = lastReviewerNotes;
    }

    public UserDTO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserDTO userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String toString() {
        return "TeacherDTO{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", salary=" + salary +
                ", contractType='" + contractType + '\'' +
                ", performanceRating=" + performanceRating +
                '}';
    }
}
