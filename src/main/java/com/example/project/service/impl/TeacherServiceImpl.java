package com.example.project.service.impl;

import com.example.project.dto.TeacherDTO;
import com.example.project.dto.UserDTO;
import com.example.project.entity.Teacher;
import com.example.project.entity.User;
import com.example.project.entity.UserRole;
import com.example.project.entity.UserStatus;
import com.example.project.repository.TeacherRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.TeacherService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherServiceImpl extends AbstractBaseService<Teacher, TeacherDTO, Long> implements TeacherService {

    private static final String DEFAULT_TEACHER_PASSWORD = "12345678";

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@gtvt.edu.vn}")
    private String mailFrom;

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
    public TeacherDTO create(TeacherDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu giáo viên");
        }

        String username = dto.getCode();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Mã giáo viên là bắt buộc");
        }
        String email = dto.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email giáo viên là bắt buộc để tạo tài khoản");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tài khoản đã tồn tại: " + username);
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại: " + email);
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setFullName(dto.getName());
        user.setEmail(email.trim());
        user.setPhone(dto.getPhone());
        user.setRole(UserRole.TEACHER);
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : UserStatus.ACTIVE);
        user.setEmployeeCode(dto.getCode());
        user.setPassword(DEFAULT_TEACHER_PASSWORD);
        User savedUser = userRepository.save(user);

        Teacher teacher = toEntity(dto);
        teacher.setUser(savedUser);
        Teacher savedTeacher = teacherRepository.save(teacher);

        sendTeacherAccountEmail(savedUser.getEmail(), savedUser.getUsername(), DEFAULT_TEACHER_PASSWORD);

        return toDTO(savedTeacher);
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
    public boolean isContractExpiringWithin30Days(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giáo viên với ID: " + teacherId));

        if (teacher.getContractEndDate() == null) {
            return false; // Không có ngày kết thúc, hợp đồng còn hiệu lực
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
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giáo viên với mã: " + code));
    }

    private void sendTeacherAccountEmail(String email, String username, String password) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(email);
            helper.setFrom(mailFrom);
            helper.setSubject("Thông báo tạo tài khoản giáo viên");
            helper.setText(buildTeacherAccountEmailHtml(username, password), true);
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Không gửi được email tạo tài khoản", ex);
        }
    }

    private String buildTeacherAccountEmailHtml(String username, String password) {
        return String.format("""
                <div style=\"font-family: 'Inter', Arial, sans-serif; background:#f8fafc; padding:32px;\">
                    <div style=\"max-width:560px; margin:0 auto; background:#ffffff; border-radius:16px; padding:28px; box-shadow:0 10px 30px rgba(15,23,42,0.08);\">
                        <div style=\"font-size:18px; font-weight:700; color:#0f172a; margin-bottom:8px;\">Tài khoản giáo viên đã được tạo</div>
                        <div style=\"font-size:14px; color:#475569; margin-bottom:16px;\">Thông tin đăng nhập của bạn:</div>
                        <div style=\"font-size:14px; color:#0f172a; margin-bottom:8px;\"><strong>Tài khoản:</strong> %s</div>
                        <div style=\"font-size:14px; color:#0f172a; margin-bottom:20px;\"><strong>Mật khẩu:</strong> %s</div>
                        <div style=\"font-size:12px; color:#94a3b8;\">Vui lòng đăng nhập và đổi mật khẩu ngay sau khi đăng nhập.</div>
                    </div>
                </div>
                """, username, password);
    }
}
