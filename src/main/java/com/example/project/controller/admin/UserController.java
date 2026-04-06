package com.example.project.controller.admin;

import com.example.project.dto.UserDTO;
import com.example.project.entity.UserRole;
import com.example.project.entity.UserStatus;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * List all users with pagination and filtering
     */
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            Model model) {

        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);

        // Get all users and filter in memory (for simplicity; consider adding search
        // repository methods)
        List<UserDTO> allUsers = userService.findAll();

        // Apply filters
        if (role != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getRole() == role)
                    .toList();
        }
        if (status != null) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getStatus() == status)
                    .toList();
        }
        if (!search.isEmpty()) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                            u.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                            u.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }

        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, allUsers.size());
        List<UserDTO> pageContent = allUsers.subList(start, end);

        model.addAttribute("users", pageContent);
        model.addAttribute("totalElements", allUsers.size());
        model.addAttribute("totalPages", (allUsers.size() + size - 1) / size);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());

        return "admin/user/list";
    }

    /**
     * View user details
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        UserDTO user = userService.getById(id);
        model.addAttribute("pageTitle", "User Details - " + user.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/user/view";
    }

    /**
     * Show create user form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Create New User");
        model.addAttribute("user", new UserDTO());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/user/form";
    }

    /**
     * Create new user
     */
    @PostMapping
    public String createUser(@ModelAttribute UserDTO user,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if username already exists
            if (userService.usernameExists(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Username already exists!");
                return "redirect:/admin/users/new";
            }

            // Set default values
            if (user.getStatus() == null) {
                user.setStatus(UserStatus.ACTIVE);
            }
            if (user.getRole() == null) {
                user.setRole(UserRole.TEACHER);
            }

            UserDTO created = userService.create(user);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
            return "redirect:/admin/users/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
            return "redirect:/admin/users/new";
        }
    }

    /**
     * Show edit user form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserDTO user = userService.getById(id);
        model.addAttribute("pageTitle", "Edit User - " + user.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/user/form";
    }

    /**
     * Update user information
     */
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
            @ModelAttribute UserDTO user,
            RedirectAttributes redirectAttributes) {
        try {
            user.setId(id);
            UserDTO updated = userService.update(id, user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    /**
     * Update user role
     */
    @PostMapping("/{id}/role")
    public String updateRole(@PathVariable Long id,
            @RequestParam UserRole newRole,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, newRole);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully!");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating role: " + e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }

    /**
     * Toggle user status
     */
    @PostMapping("/{id}/status")
    public String toggleStatus(@PathVariable Long id,
            @RequestParam UserStatus newStatus,
            RedirectAttributes redirectAttributes) {
        try {
            if (newStatus == UserStatus.ACTIVE) {
                userService.activateUser(id);
            } else {
                userService.deactivateUser(id);
            }
            redirectAttributes.addFlashAttribute("success", "User status updated successfully!");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }

    /**
     * Show password reset form
     */
    @GetMapping("/{id}/reset-password")
    public String showResetPasswordForm(@PathVariable Long id, Model model) {
        UserDTO user = userService.getById(id);
        model.addAttribute("pageTitle", "Reset Password - " + user.getUsername());
        model.addAttribute("user", user);
        return "admin/user/reset-password";
    }

    /**
     * Reset user password
     */
    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
                return "redirect:/admin/users/" + id + "/reset-password";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters!");
                return "redirect:/admin/users/" + id + "/reset-password";
            }

            userService.resetUserPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password reset successfully!");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error resetting password: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/reset-password";
        }
    }

    /**
     * Delete user
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }
}
