package com.example.project.service.impl;

import com.example.project.dto.UserDTO;
import com.example.project.entity.Teacher;
import com.example.project.entity.User;
import com.example.project.entity.UserRole;
import com.example.project.entity.UserStatus;
import com.example.project.repository.TeacherRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl extends AbstractBaseService<User, UserDTO, Long> implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO create(UserDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu người dùng");
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập là bắt buộc");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        if (dto.getStatus() == null) {
            dto.setStatus(UserStatus.ACTIVE);
        }
        if (dto.getRole() == null) {
            dto.setRole(UserRole.TEACHER);
        }

        if (dto.getRole() == UserRole.TEACHER) {
            String employeeCode = dto.getEmployeeCode();
            if (employeeCode == null || employeeCode.isBlank()) {
                throw new IllegalArgumentException("Mã nhân viên là bắt buộc cho giáo viên");
            }
            if (teacherRepository.existsByCode(employeeCode.trim())) {
                throw new IllegalArgumentException("Mã nhân viên đã tồn tại: " + employeeCode);
            }
        }

        User entity = toEntity(dto);
        User savedUser = userRepository.save(entity);

        if (savedUser.getRole() == UserRole.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setCode(savedUser.getEmployeeCode());
            teacher.setName(savedUser.getFullName() != null ? savedUser.getFullName() : savedUser.getUsername());
            teacher.setEmail(savedUser.getEmail());
            teacher.setPhone(savedUser.getPhone());
            teacher.setStatus(savedUser.getStatus());
            teacher.setUser(savedUser);
            teacherRepository.save(teacher);
        }

        return toDTO(savedUser);
    }

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setRole(entity.getRole());
        dto.setStatus(entity.getStatus());
        dto.setEmployeeCode(entity.getEmployeeCode());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    protected User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        User entity = new User();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(dto.getPassword());
        }
        entity.setFullName(dto.getFullName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setRole(dto.getRole());
        entity.setStatus(dto.getStatus());
        entity.setEmployeeCode(dto.getEmployeeCode());
        return entity;
    }

    /**
     * Merge changes from DTO to existing entity.
     * Preserves created date and password-related fields.
     */
    @Override
    protected User mergeEntity(User existingEntity, UserDTO dto) {
        if (dto.getUsername() != null) {
            existingEntity.setUsername(dto.getUsername());
        }
        if (dto.getFullName() != null) {
            existingEntity.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            existingEntity.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            existingEntity.setPhone(dto.getPhone());
        }
        if (dto.getRole() != null) {
            existingEntity.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            existingEntity.setStatus(dto.getStatus());
        }
        if (dto.getEmployeeCode() != null) {
            existingEntity.setEmployeeCode(dto.getEmployeeCode());
        }
        // createdAt and password are NOT updated during merge
        return existingEntity;
    }

    @Override
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRoleAndStatus(UserRole role, UserStatus status) {
        return userRepository.findByRoleAndStatus(role, status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getActiveAdmins() {
        return userRepository.findActiveAdmins()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    @Override
    public UserDTO deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setStatus(UserStatus.INACTIVE);
        User saved = userRepository.save(user);
        teacherRepository.findByUser(saved).ifPresent(teacher -> {
            teacher.setStatus(UserStatus.INACTIVE);
            teacherRepository.save(teacher);
        });
        return toDTO(saved);
    }

    @Override
    public UserDTO activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        teacherRepository.findByUser(saved).ifPresent(teacher -> {
            teacher.setStatus(UserStatus.ACTIVE);
            teacherRepository.save(teacher);
        });
        return toDTO(saved);
    }

    @Override
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        // Set password and mark it as changed so @PreUpdate will encode it
        user.setPassword(newPassword);
        user.setPasswordChanged(true);
        userRepository.save(user);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }
}
