package com.example.project.controller.admin;

import com.example.project.repository.CourseRepository;
import com.example.project.repository.DocumentRepository;
import com.example.project.repository.StudentRepository;
import com.example.project.repository.SubjectRepository;
import com.example.project.repository.TeacherRepository;
import com.example.project.repository.TrainingClassRepository;
import com.example.project.repository.UserRepository;
import com.example.project.entity.Student;
import com.example.project.entity.TrainingClass;
import com.example.project.entity.ClassStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final TrainingClassRepository trainingClassRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public AdminDashboardController(SubjectRepository subjectRepository,
                                    CourseRepository courseRepository,
                                    TrainingClassRepository trainingClassRepository,
                                    StudentRepository studentRepository,
                                    TeacherRepository teacherRepository,
                                    DocumentRepository documentRepository,
                                    UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.trainingClassRepository = trainingClassRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("subjectCount", subjectRepository.count());
        model.addAttribute("courseCount", courseRepository.countByDeletedFalse());
        model.addAttribute("classCount", trainingClassRepository.count());
        model.addAttribute("studentCount", studentRepository.count());
        model.addAttribute("teacherCount", teacherRepository.count());
        model.addAttribute("documentCount", documentRepository.count());
        model.addAttribute("userCount", userRepository.count());

        DashboardData data = buildDashboardData();
        model.addAttribute("latestClasses", data.latestClasses());
        model.addAttribute("activities", data.activities());
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/data")
    @ResponseBody
    public DashboardData dashboardData() {
        return buildDashboardData();
    }

    private DashboardData buildDashboardData() {
        List<TrainingClass> latestClasses = trainingClassRepository.findTop3ByOrderByCreatedAtDesc();
        List<Student> latestStudents = studentRepository.findTop3ByOrderByCreatedAtDesc();
        List<LatestClassItem> classItems = buildLatestClasses(latestClasses);
        List<ActivityItem> activities = buildActivities(latestClasses, latestStudents);
        DashboardCounts counts = new DashboardCounts(
            studentRepository.count(),
            trainingClassRepository.count(),
            teacherRepository.count(),
            courseRepository.countByDeletedFalse()
        );
        return new DashboardData(classItems, activities, counts);
    }

    private List<LatestClassItem> buildLatestClasses(List<TrainingClass> classes) {
        List<LatestClassItem> items = new ArrayList<>();
        if (classes == null) {
            return items;
        }
        for (TrainingClass clazz : classes) {
            if (clazz == null) {
                continue;
            }
            String name = clazz.getName() != null ? clazz.getName() : "-";
            String courseName = clazz.getCourse() != null && clazz.getCourse().getName() != null
                    ? clazz.getCourse().getName()
                    : "-";
            String teacherName = clazz.getTeacher() != null && clazz.getTeacher().getName() != null
                    ? clazz.getTeacher().getName()
                    : "-";
            String statusLabel = getStatusLabel(clazz.getStatus());
            String statusClass = getStatusClass(clazz.getStatus());
            String createdAt = clazz.getCreatedAt() != null
                    ? clazz.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "-";
            items.add(new LatestClassItem(name, courseName, teacherName, statusLabel, statusClass, createdAt));
        }
        return items;
    }

    private List<ActivityItem> buildActivities(List<TrainingClass> classes, List<Student> students) {
        List<ActivityItem> items = new ArrayList<>();
        if (classes != null) {
            for (TrainingClass clazz : classes) {
                if (clazz == null) {
                    continue;
                }
                String title = "Lớp học mới: " + (clazz.getName() != null ? clazz.getName() : "-");
                String detail = clazz.getCourse() != null && clazz.getCourse().getName() != null
                        ? "Khóa học: " + clazz.getCourse().getName()
                        : "";
                items.add(new ActivityItem(title, detail, clazz.getCreatedAt(), "fas fa-chalkboard"));
            }
        }
        if (students != null) {
            for (Student student : students) {
                if (student == null) {
                    continue;
                }
                String title = "Học viên mới: " + (student.getName() != null ? student.getName() : "-");
                String detail = student.getCode() != null ? "Mã học viên: " + student.getCode() : "";
                items.add(new ActivityItem(title, detail, student.getCreatedAt(), "fas fa-user-graduate"));
            }
        }

        items.sort(Comparator.comparing(ActivityItem::timestamp, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return items.size() > 6 ? items.subList(0, 6) : items;
    }

    private String getStatusLabel(ClassStatus status) {
        if (status == null) {
            return "Mới";
        }
        return switch (status) {
            case DANG_HOC -> "Đang học";
            case KET_THUC -> "Kết thúc";
            default -> "Mới";
        };
    }

    private String getStatusClass(ClassStatus status) {
        if (status == null) {
            return "badge-info";
        }
        return switch (status) {
            case DANG_HOC -> "badge-success";
            case KET_THUC -> "badge-danger";
            default -> "badge-info";
        };
    }

    public record DashboardData(List<LatestClassItem> latestClasses,
                                List<ActivityItem> activities,
                                DashboardCounts counts) {
    }

    public record DashboardCounts(long studentCount, long classCount, long teacherCount, long courseCount) {
    }

    public record LatestClassItem(String name, String courseName, String teacherName,
                                  String statusLabel, String statusClass, String createdAt) {
    }

    public record ActivityItem(String title, String detail, LocalDateTime timestamp, String iconClass) {
    }
}
