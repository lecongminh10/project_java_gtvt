package com.example.project.init;

import com.example.project.entity.User;
import com.example.project.entity.UserRole;
import com.example.project.entity.UserStatus;
import com.example.project.entity.Teacher;
import com.example.project.repository.UserRepository;
import com.example.project.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializationRunner implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeTestData();
    }

    private void initializeTestData() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isPresent()) {
            System.out.println("✓ Test data already exists. Skipping initialization.");
            return;
        }

        System.out.println("Initializing test data...");

        // Create admin user
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("admin123"); // Will be encoded by @PrePersist
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@elc.local");
        adminUser.setPhone("0123456789");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setEmployeeCode("EMP001");
        userRepository.save(adminUser);

        // Create teacher user 1
        User teacher1 = new User();
        teacher1.setUsername("teacher1");
        teacher1.setPassword("teacher123");
        teacher1.setFullName("John Doe");
        teacher1.setEmail("john@elc.local");
        teacher1.setPhone("0987654321");
        teacher1.setRole(UserRole.TEACHER);
        teacher1.setStatus(UserStatus.ACTIVE);
        teacher1.setEmployeeCode("EMP002");
        teacher1 = userRepository.save(teacher1);

        // Create teacher user 2
        User teacher2 = new User();
        teacher2.setUsername("teacher2");
        teacher2.setPassword("teacher123");
        teacher2.setFullName("Jane Smith");
        teacher2.setEmail("jane@elc.local");
        teacher2.setPhone("0976543210");
        teacher2.setRole(UserRole.TEACHER);
        teacher2.setStatus(UserStatus.ACTIVE);
        teacher2.setEmployeeCode("EMP003");
        teacher2 = userRepository.save(teacher2);

        // Create teacher 1 record
        Teacher teacherRecord1 = new Teacher();
        teacherRecord1.setCode("TCH001");
        teacherRecord1.setName("John Doe");
        teacherRecord1.setDob(LocalDate.of(1990, 5, 15));
        teacherRecord1.setEmail("john@elc.local");
        teacherRecord1.setPhone("0987654321");
        teacherRecord1.setStatus(UserStatus.ACTIVE);
        teacherRecord1.setUser(teacher1);
        teacherRecord1.setSalary(BigDecimal.valueOf(15000000.00));
        teacherRecord1.setSalaryCurrency("VND");
        teacherRecord1.setSalaryPayPeriod("MONTHLY");
        teacherRecord1.setContractStartDate(LocalDate.of(2024, 1, 1));
        teacherRecord1.setContractEndDate(LocalDate.of(2025, 12, 31));
        teacherRecord1.setContractType("FULL_TIME");
        teacherRecord1.setPerformanceRating(4.5);
        teacherRecord1.setLastPerformanceReviewDate(LocalDateTime.now());
        teacherRecord1.setLastReviewerNotes("Excellent teacher, very engaged with students");
        teacherRepository.save(teacherRecord1);

        // Create teacher 2 record
        Teacher teacherRecord2 = new Teacher();
        teacherRecord2.setCode("TCH002");
        teacherRecord2.setName("Jane Smith");
        teacherRecord2.setDob(LocalDate.of(1992, 8, 20));
        teacherRecord2.setEmail("jane@elc.local");
        teacherRecord2.setPhone("0976543210");
        teacherRecord2.setStatus(UserStatus.ACTIVE);
        teacherRecord2.setUser(teacher2);
        teacherRecord2.setSalary(BigDecimal.valueOf(12000000.00));
        teacherRecord2.setSalaryCurrency("VND");
        teacherRecord2.setSalaryPayPeriod("MONTHLY");
        teacherRecord2.setContractStartDate(LocalDate.of(2024, 6, 1));
        teacherRecord2.setContractEndDate(LocalDate.of(2026, 5, 31));
        teacherRecord2.setContractType("FULL_TIME");
        teacherRecord2.setPerformanceRating(4.0);
        teacherRecord2.setLastPerformanceReviewDate(LocalDateTime.now());
        teacherRecord2.setLastReviewerNotes("Good teaching skills, needs improvement in classroom management");
        teacherRepository.save(teacherRecord2);

        System.out.println("✓ Test data initialized successfully!");
        System.out.println("  - Admin user: admin / admin123");
        System.out.println("  - Teacher 1: teacher1 / teacher123");
        System.out.println("  - Teacher 2: teacher2 / teacher123");
    }
}
