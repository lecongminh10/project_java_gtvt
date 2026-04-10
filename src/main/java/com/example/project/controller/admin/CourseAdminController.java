package com.example.project.controller.admin;

import com.example.project.dto.CourseDTO;
import com.example.project.entity.ClassStatus;
import com.example.project.entity.Course;
import com.example.project.repository.SubjectRepository;
import com.example.project.service.CourseService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/courses")
public class CourseAdminController {

    private final CourseService courseService;
    private final SubjectRepository subjectRepository;

    public CourseAdminController(CourseService courseService, SubjectRepository subjectRepository) {
        this.courseService = courseService;
        this.subjectRepository = subjectRepository;
    }


    @GetMapping
    public String listCourses(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) ClassStatus status,
            Model model
    ) {
        List<CourseDTO> courses = ((courseName != null && !courseName.isEmpty()) || status != null)
                ? courseService.searchCourses(courseName, status)
                : courseService.findAll();
        ;
        model.addAttribute("pageTitle", "Quản lý khóa học");
        model.addAttribute("courseName", courseName);
        model.addAttribute("courseStatus", status);
        model.addAttribute("courses", courses);
        return "admin/course/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Prepare empty form state for create.
        model.addAttribute("course", new Course());
        model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
//        model.addAttribute("statuses", ClassStatus.values());
        model.addAttribute("formAction", "/admin/courses/create");
        model.addAttribute("pageTitle", "Thêm khóa học");
        return "admin/course/form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute("course") CourseDTO courseDTO, Model model) {
        Map<String, String> errors = new HashMap<>();
        if (courseDTO.getCode() == null || courseDTO.getCode().trim().isEmpty()) {
            errors.put("code", "Mã khóa học không được để trống");
        }
        if (courseDTO.getName() == null || courseDTO.getName().trim().isEmpty()) {
            errors.put("name", "Tên khóa học không được để trống");
        }
        if (courseDTO.getSessionsCount() == null || courseDTO.getSessionsCount() < 0) {
            errors.put("sessionsCount", "Số buổi phải là số dương");
        }
        if (courseDTO.getFee() == null || courseDTO.getFee().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("fee", "Học phí phải là số dương");
        }
        if (courseDTO.getDurationWeeks() == null || courseDTO.getDurationWeeks() <= 0) {
            errors.put("durationWeeks", "Thời lượng phải lớn hơn 0");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
            model.addAttribute("formAction", "/admin/courses/create");
            model.addAttribute("pageTitle", "Thêm khóa học");
            return "admin/course/form";
        }
        courseService.create(courseDTO);
        return "redirect:/admin/courses";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        CourseDTO course = courseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        model.addAttribute("course", course);
        model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
        model.addAttribute("formAction", "/admin/courses/" + id + "/edit");
        model.addAttribute("pageTitle", "Cập nhật khóa học");
        return "admin/course/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id, @ModelAttribute("course") CourseDTO courseDTO, Model model) {
        Map<String, String> errors = new HashMap<>();
        if (courseDTO.getCode() == null || courseDTO.getCode().trim().isEmpty()) {
            errors.put("code", "Mã khóa học không được để trống");
        }
        if (courseDTO.getName() == null || courseDTO.getName().trim().isEmpty()) {
            errors.put("name", "Tên khóa học không được để trống");
        }
        if (courseDTO.getSessionsCount() == null || courseDTO.getSessionsCount() < 0) {
            errors.put("sessionsCount", "Số buổi phải là số dương");
        }
        if (courseDTO.getFee() == null || courseDTO.getFee().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("fee", "Học phí phải là số dương");
        }
        if (courseDTO.getDurationWeeks() == null || courseDTO.getDurationWeeks() <= 0) {
            errors.put("durationWeeks", "Thời lượng phải lớn hơn 0");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("subjects", subjectRepository.findAll(Sort.by("name")));
            model.addAttribute("formAction", "/admin/courses/" + id + "/edit");
            model.addAttribute("pageTitle", "Cập nhật khóa học");
            return "admin/course/form";
        }
        courseService.update(id, courseDTO);
        return "redirect:/admin/courses";
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/courses";
    }
}
