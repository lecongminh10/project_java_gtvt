package com.example.project.controller.admin;

import com.example.project.dto.TeacherDTO;
import com.example.project.entity.UserStatus;
import com.example.project.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

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

        model.addAttribute("pageTitle", "Teacher Management");
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
        model.addAttribute("pageTitle", "Teacher Details - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("statuses", UserStatus.values());
        return "admin/teacher/view";
    }

    /**
     * Show create form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Create New Teacher");
        model.addAttribute("teacher", new TeacherDTO());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("contractTypes", new String[] { "FULL_TIME", "PART_TIME", "FREELANCE" });
        return "admin/teacher/form";
    }

    /**
     * Create new teacher
     */
    @PostMapping
    public String createTeacher(@ModelAttribute TeacherDTO teacher,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if code already exists
            if (teacherService.teacherCodeExists(teacher.getCode())) {
                redirectAttributes.addFlashAttribute("error", "Teacher code already exists!");
                return "redirect:/admin/teachers/new";
            }

            // Set defaults
            if (teacher.getStatus() == null) {
                teacher.setStatus(UserStatus.ACTIVE);
            }

            TeacherDTO created = teacherService.create(teacher);
            redirectAttributes.addFlashAttribute("success", "Teacher created successfully!");
            return "redirect:/admin/teachers/" + created.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating teacher: " + e.getMessage());
            return "redirect:/admin/teachers/new";
        }
    }

    /**
     * Show edit form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getById(id);
        model.addAttribute("pageTitle", "Edit Teacher - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("contractTypes", new String[] { "FULL_TIME", "PART_TIME", "FREELANCE" });
        return "admin/teacher/form";
    }

    /**
     * Update teacher basic info
     */
    @PostMapping("/{id}/edit")
    public String updateTeacher(@PathVariable Long id,
            @ModelAttribute TeacherDTO teacher,
            RedirectAttributes redirectAttributes) {
        try {
            teacher.setId(id);
            TeacherDTO updated = teacherService.update(id, teacher);
            redirectAttributes.addFlashAttribute("success", "Teacher updated successfully!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating teacher: " + e.getMessage());
            return "redirect:/admin/teachers/" + id + "/edit";
        }
    }

    /**
     * Show salary management page
     */
    @GetMapping("/{id}/salary")
    public String showSalaryManagement(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getById(id);
        model.addAttribute("pageTitle", "Manage Salary - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("payPeriods", new String[] { "MONTHLY", "HOURLY", "CONTRACT" });
        model.addAttribute("currencies", new String[] { "VND", "USD", "EUR" });
        return "admin/teacher/salary";
    }

    /**
     * Update salary information
     */
    @PostMapping("/{id}/salary")
    public String updateSalary(@PathVariable Long id,
            @RequestParam BigDecimal salary,
            @RequestParam String currency,
            @RequestParam String payPeriod,
            RedirectAttributes redirectAttributes) {
        try {
            teacherService.updateSalary(id, salary, currency, payPeriod);
            redirectAttributes.addFlashAttribute("success", "Salary updated successfully!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating salary: " + e.getMessage());
            return "redirect:/admin/teachers/" + id + "/salary";
        }
    }

    /**
     * Show contract management page
     */
    @GetMapping("/{id}/contract")
    public String showContractManagement(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getById(id);
        model.addAttribute("pageTitle", "Manage Contract - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("contractTypes", new String[] { "FULL_TIME", "PART_TIME", "FREELANCE" });
        return "admin/teacher/contract";
    }

    /**
     * Update contract information
     */
    @PostMapping("/{id}/contract")
    public String updateContract(@PathVariable Long id,
            @RequestParam LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam String contractType,
            RedirectAttributes redirectAttributes) {
        try {
            teacherService.updateContract(id, startDate, endDate, contractType);
            redirectAttributes.addFlashAttribute("success", "Contract updated successfully!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating contract: " + e.getMessage());
            return "redirect:/admin/teachers/" + id + "/contract";
        }
    }

    /**
     * Show performance review page
     */
    @GetMapping("/{id}/performance-review")
    public String showPerformanceReview(@PathVariable Long id, Model model) {
        TeacherDTO teacher = teacherService.getById(id);
        model.addAttribute("pageTitle", "Performance Review - " + teacher.getName());
        model.addAttribute("teacher", teacher);
        return "admin/teacher/performance-review";
    }

    /**
     * Record performance review
     */
    @PostMapping("/{id}/performance-review")
    public String recordPerformanceReview(@PathVariable Long id,
            @RequestParam Double rating,
            @RequestParam String notes,
            RedirectAttributes redirectAttributes) {
        try {
            if (rating < 1.0 || rating > 5.0) {
                redirectAttributes.addFlashAttribute("error", "Rating must be between 1.0 and 5.0");
                return "redirect:/admin/teachers/" + id + "/performance-review";
            }

            teacherService.updatePerformanceReview(id, rating, notes);
            redirectAttributes.addFlashAttribute("success", "Performance review recorded successfully!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error recording review: " + e.getMessage());
            return "redirect:/admin/teachers/" + id + "/performance-review";
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
            teacher.setStatus(newStatus);
            teacherService.update(id, teacher);
            redirectAttributes.addFlashAttribute("success", "Teacher status updated successfully!");
            return "redirect:/admin/teachers/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
            return "redirect:/admin/teachers/" + id;
        }
    }

    /**
     * Delete teacher
     */
    @PostMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Teacher deleted successfully!");
            return "redirect:/admin/teachers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting teacher: " + e.getMessage());
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

        model.addAttribute("pageTitle", "Teachers with Expiring Contracts");
        model.addAttribute("teachers", teachers);
        model.addAttribute("expiryDate", futureDate);

        return "admin/teacher/expiring-contracts";
    }
}
