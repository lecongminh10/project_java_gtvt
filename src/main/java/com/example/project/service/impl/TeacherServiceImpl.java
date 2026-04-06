package com.example.project.service.impl;

import com.example.project.dto.TeacherDTO;
import com.example.project.dto.UserDTO;
import com.example.project.entity.Teacher;
import com.example.project.entity.User;
import com.example.project.entity.UserStatus;
import com.example.project.repository.TeacherRepository;
import com.example.project.service.TeacherService;
import com.example.project.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherServiceImpl extends AbstractBaseService<Teacher, TeacherDTO, Long> implements TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserService userService;

    @Override
    protected JpaRepository<Teacher, Long> getRepository() {
        return teacherRepository;
    }

    @Override
    protected TeacherDTO toDTO(Teacher entity) {
        if (entity == null) {
            return null;
        }
        TeacherDTO dto = new TeacherDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDob(entity.getDob());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Salary
        dto.setSalary(entity.getSalary());
        dto.setSalaryCurrency(entity.getSalaryCurrency());
        dto.setSalaryPayPeriod(entity.getSalaryPayPeriod());

        // Contract
        dto.setContractStartDate(entity.getContractStartDate());
        dto.setContractEndDate(entity.getContractEndDate());
        dto.setContractType(entity.getContractType());

        // Performance
        dto.setPerformanceRating(entity.getPerformanceRating());
        dto.setLastPerformanceReviewDate(entity.getLastPerformanceReviewDate());
        dto.setLastReviewerNotes(entity.getLastReviewerNotes());

        // User info
        if (entity.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(entity.getUser().getId());
            userDTO.setUsername(entity.getUser().getUsername());
            userDTO.setFullName(entity.getUser().getFullName());
            dto.setUserInfo(userDTO);
        }

        return dto;
    }

    @Override
    protected Teacher toEntity(TeacherDTO dto) {
        if (dto == null) {
            return null;
        }
        Teacher entity = new Teacher();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDob(dto.getDob());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setStatus(dto.getStatus());

        // Salary
        entity.setSalary(dto.getSalary());
        entity.setSalaryCurrency(dto.getSalaryCurrency());
        entity.setSalaryPayPeriod(dto.getSalaryPayPeriod());

        // Contract
        entity.setContractStartDate(dto.getContractStartDate());
        entity.setContractEndDate(dto.getContractEndDate());
        entity.setContractType(dto.getContractType());

        // Performance
        entity.setPerformanceRating(dto.getPerformanceRating());
        entity.setLastPerformanceReviewDate(dto.getLastPerformanceReviewDate());
        entity.setLastReviewerNotes(dto.getLastReviewerNotes());

        return entity;
    }

    @Override
    protected Teacher mergeEntity(Teacher existingEntity, TeacherDTO dto) {
        if (dto.getCode() != null) {
            existingEntity.setCode(dto.getCode());
        }
        if (dto.getName() != null) {
            existingEntity.setName(dto.getName());
        }
        if (dto.getDob() != null) {
            existingEntity.setDob(dto.getDob());
        }
        if (dto.getEmail() != null) {
            existingEntity.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            existingEntity.setPhone(dto.getPhone());
        }
        if (dto.getStatus() != null) {
            existingEntity.setStatus(dto.getStatus());
        }
        if (dto.getSalary() != null) {
            existingEntity.setSalary(dto.getSalary());
        }
        if (dto.getSalaryCurrency() != null) {
            existingEntity.setSalaryCurrency(dto.getSalaryCurrency());
        }
        if (dto.getSalaryPayPeriod() != null) {
            existingEntity.setSalaryPayPeriod(dto.getSalaryPayPeriod());
        }
        if (dto.getContractStartDate() != null) {
            existingEntity.setContractStartDate(dto.getContractStartDate());
        }
        if (dto.getContractEndDate() != null) {
            existingEntity.setContractEndDate(dto.getContractEndDate());
        }
        if (dto.getContractType() != null) {
            existingEntity.setContractType(dto.getContractType());
        }
        if (dto.getPerformanceRating() != null) {
            existingEntity.setPerformanceRating(dto.getPerformanceRating());
        }
        if (dto.getLastReviewerNotes() != null) {
            existingEntity.setLastReviewerNotes(dto.getLastReviewerNotes());
        }
        return existingEntity;
    }

    @Override
    public List<TeacherDTO> getTeachersByStatus(UserStatus status) {
        return teacherRepository.findByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDTO> getActiveTeachers() {
        return teacherRepository.findActiveTeachers()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeacherDTO updateSalary(Long teacherId, BigDecimal newSalary, String currency, String payPeriod) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        teacher.setSalary(newSalary);
        teacher.setSalaryCurrency(currency);
        teacher.setSalaryPayPeriod(payPeriod);
        Teacher saved = teacherRepository.save(teacher);
        return toDTO(saved);
    }

    @Override
    public TeacherDTO updatePerformanceReview(Long teacherId, Double rating, String notes) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        teacher.setPerformanceRating(rating);
        teacher.setLastReviewerNotes(notes);
        teacher.setLastPerformanceReviewDate(LocalDateTime.now());
        Teacher saved = teacherRepository.save(teacher);
        return toDTO(saved);
    }

    @Override
    public TeacherDTO updateContract(Long teacherId, LocalDate startDate, LocalDate endDate, String contractType) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        teacher.setContractStartDate(startDate);
        teacher.setContractEndDate(endDate);
        teacher.setContractType(contractType);
        Teacher saved = teacherRepository.save(teacher);
        return toDTO(saved);
    }

    @Override
    public boolean isContractExpiringWithin30Days(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));

        if (teacher.getContractEndDate() == null) {
            return false; // No end date, contract is ongoing
        }

        LocalDate futureDate = LocalDate.now().plusDays(30);
        return teacher.getContractEndDate().isBefore(futureDate) &&
                teacher.getContractEndDate().isAfter(LocalDate.now());
    }

    @Override
    public List<TeacherDTO> getTeachersWithExpiringContracts(LocalDate futureDate) {
        return teacherRepository.findContractsExpiringBefore(futureDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean teacherCodeExists(String code) {
        return teacherRepository.existsByCode(code);
    }

    @Override
    public TeacherDTO getTeacherByCode(String code) {
        return teacherRepository.findByCode(code)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with code: " + code));
    }
}
