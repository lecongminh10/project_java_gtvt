package com.example.project.controller.admin;

import com.example.project.dto.TeacherDTO;
import com.example.project.entity.UserStatus;
import com.example.project.service.TeacherService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    /**
     * List all teachers with filtering
     */
    @GetMapping
    public String listTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "false") boolean expiringContracts,
            Model model) {

        model.addAttribute("pageTitle", "Quản lý giáo viên");
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("expiringContracts", expiringContracts);

        List<TeacherDTO> allTeachers;

        // Get teachers based on filters
        if (expiringContracts) {
            LocalDate futureDate = LocalDate.now().plusDays(30);
            allTeachers = teacherService.getTeachersWithExpiringContracts(futureDate);
        } else if (status != null) {
            allTeachers = teacherService.getTeachersByStatus(status);
        } else {
            allTeachers = teacherService.findAll();
        }

        // Apply search filter
        if (!search.isEmpty()) {
            allTeachers = allTeachers.stream()
                    .filter(t -> t.getCode().toLowerCase().contains(search.toLowerCase()) ||
                            t.getName().toLowerCase().contains(search.toLowerCase()) ||
                            t.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .toList();
        }

        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, allTeachers.size());
        List<TeacherDTO> pageContent = allTeachers.subList(start, end);

        model.addAttribute("teachers", pageContent);
        model.addAttribute("totalElements", allTeachers.size());
        model.addAttribute("totalPages", (allTeachers.size() + size - 1) / size);
        model.addAttribute("statuses", UserStatus.values());

        return "admin/teacher/list";
    }

    /**
     * View teacher details
     */
    @GetMapping("/{id}")
    public String viewTeacher(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getById(id);
        model.addAttribute("pageTitle", "Chi tiết giáo viên - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("statuses", UserStatus.values());
        return "admin/teacher/view";
    }

    /**
     * Show create form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Thêm giáo viên");
        model.addAttribute("teacher", new TeacherDTO());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("contractTypes", new String[] { "FULL_TIME", "PART_TIME", "FREELANCE" });
        model.addAttribute("actionUrl", "/admin/teachers");
        return "admin/teacher/form";
    }

    /**
     * Create new teacher
     */
    @PostMapping
    public String createTeacher(@ModelAttribute TeacherDTO teacher,
            @RequestParam(required = false) Boolean contractNoEndDate,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if code already exists
            if (teacherService.teacherCodeExists(teacher.getCode())) {
                redirectAttributes.addFlashAttribute("error", "Mã giáo viên đã tồn tại!");
                return "redirect:/admin/teachers/new";
            }

            if (teacher.getEmail() == null || teacher.getEmail().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập email để tạo tài khoản giáo viên.");
                return "redirect:/admin/teachers/new";
            }

            // Set defaults
            if (teacher.getStatus() == null) {
                teacher.setStatus(UserStatus.ACTIVE);
            }

            if (Boolean.TRUE.equals(contractNoEndDate)) {
                teacher.setContractEndDate(null);
            }

            if (teacher.getContractStartDate() != null && teacher.getContractEndDate() != null
                    && teacher.getContractEndDate().isBefore(teacher.getContractStartDate())) {
                redirectAttributes.addFlashAttribute("error", "Ngày kết thúc không được bé hơn ngày bắt đầu.");
                return "redirect:/admin/teachers/new";
            }

            TeacherDTO created = teacherService.create(teacher);
            redirectAttributes.addFlashAttribute("success", "Đã tạo giáo viên và tài khoản đăng nhập.");
            return "redirect:/admin/teachers/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi tạo giáo viên: " + e.getMessage());
            return "redirect:/admin/teachers/new";
        }
    }

    /**
     * Show edit form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getById(id);
        model.addAttribute("pageTitle", "Chỉnh sửa giáo viên - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("contractTypes", new String[] { "FULL_TIME", "PART_TIME", "FREELANCE" });
        model.addAttribute("actionUrl", "/admin/teachers/" + id + "/edit");
        return "admin/teacher/form";
    }

    /**
     * Update teacher basic info
     */
    @PostMapping("/{id}/edit")
    public String updateTeacher(@PathVariable Long id,
            @ModelAttribute TeacherDTO teacher,
            @RequestParam(required = false) Boolean contractNoEndDate,
            RedirectAttributes redirectAttributes) {
        try {
            teacher.setId(id);
            if (Boolean.TRUE.equals(contractNoEndDate)) {
                teacher.setContractEndDate(null);
            }
            if (teacher.getContractStartDate() != null && teacher.getContractEndDate() != null
                    && teacher.getContractEndDate().isBefore(teacher.getContractStartDate())) {
                redirectAttributes.addFlashAttribute("error", "Ngày kết thúc không được bé hơn ngày bắt đầu.");
                return "redirect:/admin/teachers/" + id + "/edit";
            }
            TeacherDTO updated = teacherService.update(id, teacher);
            redirectAttributes.addFlashAttribute("success", "Cập nhật giáo viên thành công!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật giáo viên: " + e.getMessage());
            return "redirect:/admin/teachers/" + id + "/edit";
        }
    }

    /**
     * Toggle teacher status
     */
    @PostMapping("/{id}/status")
    public String toggleStatus(@PathVariable Long id,
            @RequestParam UserStatus newStatus,
            RedirectAttributes redirectAttributes) {
        try {
            TeacherDTO teacher = teacherService.getById(id);
            if (newStatus == UserStatus.ACTIVE && teacher.getUserInfo() != null) {
                userService.activateUser(teacher.getUserInfo().getId());
            }
            teacher.setStatus(newStatus);
            teacherService.update(id, teacher);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái giáo viên thành công!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật trạng thái: " + e.getMessage());
            return "redirect:/admin/teachers/" + id;
        }
    }

    /**
     * Delete teacher
     */
    @PostMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            TeacherDTO teacher = teacherService.getById(id);
            teacher.setStatus(UserStatus.INACTIVE);
            teacherService.update(id, teacher);
            if (teacher.getUserInfo() != null) {
                userService.deactivateUser(teacher.getUserInfo().getId());
            }
            redirectAttributes.addFlashAttribute("toastSuccess", "Đã ngừng hoạt động giáo viên.");
            return "redirect:/admin/teachers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastError", "Lỗi ngừng hoạt động giáo viên: " + e.getMessage());
            return "redirect:/admin/teachers/" + id;
        }
    }

    /**
     * List teachers with expiring contracts
     */
    @GetMapping("/expiring-contracts")
    public String listExpiringContracts(Model model) {
        LocalDate futureDate = LocalDate.now().plusDays(30);
        List<TeacherDTO> teachers = teacherService.getTeachersWithExpiringContracts(futureDate);

        model.addAttribute("pageTitle", "Hợp đồng sắp hết hạn");
        model.addAttribute("teachers", teachers);
        model.addAttribute("expiryDate", futureDate);

        return "admin/teacher/expiring-contracts";
    }
}
