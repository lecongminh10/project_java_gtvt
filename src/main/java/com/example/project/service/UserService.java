package com.example.project.service;

import com.example.project.dto.UserDTO;
import com.example.project.entity.UserRole;
import com.example.project.entity.UserStatus;
import java.util.List;

public interface UserService extends BaseService<UserDTO, Long> {

    /**
     * Find users by role
     */
    List<UserDTO> getUsersByRole(UserRole role);

    /**
     * Find users by status
     */
    List<UserDTO> getUsersByStatus(UserStatus status);

    /**
     * Find users by role and status
     */
    List<UserDTO> getUsersByRoleAndStatus(UserRole role, UserStatus status);

    /**
     * Find active admin users
     */
    List<UserDTO> getActiveAdmins();

    /**
     * Update user role
     */
    UserDTO updateUserRole(Long userId, UserRole newRole);

    /**
     * Toggle user status (active/inactive)
     */
    UserDTO deactivateUser(Long userId);

    UserDTO activateUser(Long userId);

    /**
     * Reset user password with a new password
     */
    void resetUserPassword(Long userId, String newPassword);

    /**
     * Check if username exists
     */
    boolean usernameExists(String username);

    /**
     * Find user by username
     */
    UserDTO getUserByUsername(String username);
}
