package com.example.project.controller.teacher;

import com.example.project.entity.ClassStudent;
import com.example.project.entity.Teacher;
import com.example.project.entity.TrainingClass;
import com.example.project.repository.ClassStudentRepository;
import com.example.project.repository.TeacherRepository;
import com.example.project.repository.TrainingClassRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/teacher/classes")
public class TeacherClassController {

    private final TrainingClassRepository trainingClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final TeacherRepository teacherRepository;

    public TeacherClassController(TrainingClassRepository trainingClassRepository,
                                  ClassStudentRepository classStudentRepository,
                                  TeacherRepository teacherRepository) {
        this.trainingClassRepository = trainingClassRepository;
        this.classStudentRepository = classStudentRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    public String listClasses(Model model, Principal principal) {
        String identifier = resolveIdentifier(principal);
        if (identifier == null) {
            return "redirect:/login";
        }

        Optional<Teacher> teacherOpt = resolveTeacher(identifier);
        if (teacherOpt.isEmpty()) {
            model.addAttribute("pageTitle", "Lớp học của tôi");
            model.addAttribute("classes", List.of());
            model.addAttribute("studentCounts", Map.of());
            model.addAttribute("error", "Không tìm thấy giáo viên cho tài khoản đăng nhập.");
            return "teacher/class/list";
        }

        Teacher teacher = teacherOpt.get();
        List<TrainingClass> classes = trainingClassRepository
                .findByTeacher_IdOrderByCreatedAtDesc(teacher.getId());
        Map<Long, Long> studentCounts = new HashMap<>();
        for (TrainingClass clazz : classes) {
            long count = classStudentRepository.countByTrainingClass_IdAndLeaveDateIsNull(clazz.getId());
            studentCounts.put(clazz.getId(), count);
        }

        model.addAttribute("pageTitle", "Lớp học của tôi");
        model.addAttribute("classes", classes);
        model.addAttribute("studentCounts", studentCounts);
        return "teacher/class/list";
    }

    @GetMapping("/{id}")
    public String classDetail(@PathVariable Long id, Model model, Principal principal) {
        String identifier = resolveIdentifier(principal);
        if (identifier == null) {
            return "redirect:/login";
        }

        Optional<Teacher> teacherOpt = resolveTeacher(identifier);
        if (teacherOpt.isEmpty()) {
            return "error/403";
        }

        Teacher teacher = teacherOpt.get();
        Optional<TrainingClass> classOpt = trainingClassRepository.findByIdAndTeacher_Id(id, teacher.getId());
        if (classOpt.isEmpty()) {
            return "error/403";
        }

        TrainingClass trainingClass = classOpt.get();
        List<ClassStudent> members = classStudentRepository.findActiveMembersByClassId(id);

        model.addAttribute("pageTitle", "Chi tiết lớp học");
        model.addAttribute("trainingClass", trainingClass);
        model.addAttribute("members", members);
        model.addAttribute("studentCount", members.size());
        return "teacher/class/detail";
    }

    private String resolveIdentifier(Principal principal) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName().trim();
        }
        return null;
    }

    private Optional<Teacher> resolveTeacher(String identifier) {
        Optional<Teacher> byUsername = teacherRepository.findByUserUsername(identifier);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        Optional<Teacher> byCode = teacherRepository.findByCode(identifier);
        if (byCode.isPresent()) {
            return byCode;
        }
        Optional<Teacher> byEmployeeCode = teacherRepository.findByUser_EmployeeCode(identifier);
        if (byEmployeeCode.isPresent()) {
            return byEmployeeCode;
        }
        List<Teacher> byEmail = teacherRepository.findByEmail(identifier);
        if (!byEmail.isEmpty()) {
            return Optional.of(byEmail.get(0));
        }
        return Optional.empty();
    }
}
