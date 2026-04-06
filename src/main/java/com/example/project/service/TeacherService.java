package com.example.project.service;

import com.example.project.dto.TeacherDTO;
import com.example.project.entity.UserStatus;
import java.time.LocalDate;
import java.util.List;

public interface TeacherService extends BaseService<TeacherDTO, Long> {

    /**
     * Find teachers by status
     */
    List<TeacherDTO> getTeachersByStatus(UserStatus status);

    /**
     * Get all active teachers
     */
    List<TeacherDTO> getActiveTeachers();

    /**
     * Update teacher salary information
     */
    TeacherDTO updateSalary(Long teacherId, java.math.BigDecimal newSalary, String currency, String payPeriod);

    /**
     * Record a performance review
     */
    TeacherDTO updatePerformanceReview(Long teacherId, Double rating, String notes);

    /**
     * Update contract information
     */
    TeacherDTO updateContract(Long teacherId, LocalDate startDate, LocalDate endDate, String contractType);

    /**
     * Check if teacher's contract is expiring soon (within 30 days)
     */
    boolean isContractExpiringWithin30Days(Long teacherId);

    /**
     * Get all teachers with contracts expiring within specified days
     */
    List<TeacherDTO> getTeachersWithExpiringContracts(LocalDate futureDate);

    /**
     * Check if teacher code already exists
     */
    boolean teacherCodeExists(String code);

    /**
     * Find teacher by code
     */
    TeacherDTO getTeacherByCode(String code);
}
