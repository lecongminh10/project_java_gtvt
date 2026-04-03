package com.example.project.controller.admin;

import com.example.project.entity.ClassStatus;
import com.example.project.entity.Course;
import com.example.project.entity.Subject;
import com.example.project.repository.CourseRepository;
import com.example.project.repository.SubjectRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/courses")
public class CourseAdminController {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    public CourseAdminController(CourseRepository courseRepository, SubjectRepository subjectRepository) {
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    public String listCourses(Model model) {
        // Load latest courses for admin list view.
        List<Course> courses = courseRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "Quản lý khóa học");
        return "admin/course/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Prepare empty form state for create.
        model.addAttribute("course", new Course());
        model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
        model.addAttribute("statuses", ClassStatus.values());
        model.addAttribute("formAction", "/admin/courses");
        model.addAttribute("pageTitle", "Thêm khóa học");
        return "admin/course/form";
    }

    @PostMapping
    public String createCourse(
            Model model,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String sessionsCount,
            @RequestParam(required = false) String durationWeeks,
            @RequestParam(required = false) String fee,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) ClassStatus status
    ) {
        // Validate input and build entity; show form again if any errors.
        ValidationResult result = buildCourseFromForm(new Course(), code, name, subjectId, sessionsCount, durationWeeks, fee, description, status);
        if (result.hasErrors()) {
            populateFormModel(model, result.course, "/admin/courses", "Thêm khóa học", result.errors);
            return "admin/course/form";
        }
        // Set audit timestamps on create.
        result.course.setCreatedAt(LocalDateTime.now());
        result.course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(result.course);
        return "redirect:/admin/courses";
    }

    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        // Show read-only course detail.
        Course course = courseRepository.findById(id).orElseThrow();
        model.addAttribute("course", course);
        model.addAttribute("pageTitle", "Chi tiết khóa học");
        return "admin/course/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Load existing course for edit form.
        Course course = courseRepository.findById(id).orElseThrow();
        model.addAttribute("course", course);
        model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
        model.addAttribute("statuses", ClassStatus.values());
        model.addAttribute("formAction", "/admin/courses/" + id);
        model.addAttribute("pageTitle", "Cập nhật khóa học");
        return "admin/course/form";
    }

    @PostMapping("/{id}")
    public String updateCourse(
            @PathVariable Long id,
            Model model,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String sessionsCount,
            @RequestParam(required = false) String durationWeeks,
            @RequestParam(required = false) String fee,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) ClassStatus status
    ) {
        // Validate updates and reuse form data if validation fails.
        Course course = courseRepository.findById(id).orElseThrow();
        ValidationResult result = buildCourseFromForm(course, code, name, subjectId, sessionsCount, durationWeeks, fee, description, status);
        if (result.hasErrors()) {
            populateFormModel(model, result.course, "/admin/courses/" + id, "Cập nhật khóa học", result.errors);
            return "admin/course/form";
        }
        // Preserve createdAt if missing, always refresh updatedAt.
        if (result.course.getCreatedAt() == null) {
            result.course.setCreatedAt(LocalDateTime.now());
        }
        result.course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(result.course);
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id) {
        // Delete by id then return to list.
        courseRepository.deleteById(id);
        return "redirect:/admin/courses";
    }

    private ValidationResult buildCourseFromForm(
            Course course,
            String code,
            String name,
            Long subjectId,
            String sessionsCount,
            String durationWeeks,
            String fee,
            String description,
            ClassStatus status
    ) {
        // Normalize input and collect field-level validation errors.
        Map<String, String> errors = new HashMap<>();

        String normalizedCode = code != null ? code.trim() : "";
        String normalizedName = name != null ? name.trim() : "";

        if (normalizedCode.isEmpty()) {
            errors.put("code", "Mã khóa học bắt buộc.");
        }
        if (normalizedName.isEmpty()) {
            errors.put("name", "Tên khóa học bắt buộc.");
        }

        Subject subject = null;
        if (subjectId == null) {
            errors.put("subjectId", "Vui lòng chọn môn học.");
        } else {
            subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject == null) {
                errors.put("subjectId", "Môn học không tồn tại.");
            }
        }

        // Parse optional numeric fields with non-negative constraint.
        Integer parsedSessions = parseNonNegativeInteger(sessionsCount, "sessionsCount", errors);
        Integer parsedWeeks = parseNonNegativeInteger(durationWeeks, "durationWeeks", errors);
        BigDecimal parsedFee = parseNonNegativeBigDecimal(fee, "fee", errors);

        course.setCode(normalizedCode);
        course.setName(normalizedName);
        course.setSubject(subject);
        course.setSessionsCount(parsedSessions);
        course.setDurationWeeks(parsedWeeks);
        course.setFee(parsedFee);
        course.setDescription(description != null ? description.trim() : null);
        course.setStatus(status);

        return new ValidationResult(course, errors);
    }

    private Integer parseNonNegativeInteger(String value, String fieldKey, Map<String, String> errors) {
        // Accept blank as null, otherwise validate integer >= 0.
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                errors.put(fieldKey, "Giá trị phải lớn hơn hoặc bằng 0.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            errors.put(fieldKey, "Định dạng số không hợp lệ.");
            return null;
        }
    }

    private BigDecimal parseNonNegativeBigDecimal(String value, String fieldKey, Map<String, String> errors) {
        // Accept blank as null, otherwise validate decimal >= 0.
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            if (parsed.signum() < 0) {
                errors.put(fieldKey, "Giá trị phải lớn hơn hoặc bằng 0.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            errors.put(fieldKey, "Định dạng số không hợp lệ.");
            return null;
        }
    }

    private void populateFormModel(Model model, Course course, String formAction, String pageTitle, Map<String, String> errors) {
        // Rehydrate form with reference data and validation messages.
        model.addAttribute("course", course);
        model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
        model.addAttribute("statuses", ClassStatus.values());
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("errors", errors);
    }

    private static class ValidationResult {
        private final Course course;
        private final Map<String, String> errors;

        private ValidationResult(Course course, Map<String, String> errors) {
            this.course = course;
            this.errors = errors;
        }

        private boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }
}
