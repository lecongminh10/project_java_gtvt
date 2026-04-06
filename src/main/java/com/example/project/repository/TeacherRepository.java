package com.example.project.repository;

import com.example.project.entity.Teacher;
import com.example.project.entity.User;
import com.example.project.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByCode(String code);

    Optional<Teacher> findByUser(User user);

    List<Teacher> findByStatus(UserStatus status);

    List<Teacher> findByStatusOrderByNameAsc(UserStatus status);

    @Query("SELECT t FROM Teacher t WHERE t.status = 'ACTIVE'")
    List<Teacher> findActiveTeachers();

    @Query("SELECT t FROM Teacher t WHERE t.contractEndDate IS NOT NULL AND t.contractEndDate <= :expiryDate")
    List<Teacher> findExpiredContracts(@Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT t FROM Teacher t WHERE t.contractEndDate IS NOT NULL AND t.contractEndDate > CURRENT_DATE AND t.contractEndDate <= :futureDate")
    List<Teacher> findContractsExpiringBefore(@Param("futureDate") LocalDate futureDate);

    boolean existsByCode(String code);

    List<Teacher> findByEmail(String email);
}
