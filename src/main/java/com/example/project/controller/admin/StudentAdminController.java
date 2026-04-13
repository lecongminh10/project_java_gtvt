package com.example.project.controller.admin;

import com.example.project.entity.ClassStudent;
import com.example.project.entity.ClassStudentId;
import com.example.project.entity.Student;
import com.example.project.entity.StudentStatus;
import com.example.project.entity.TrainingClass;
import com.example.project.repository.ClassStudentRepository;
import com.example.project.repository.StudentRepository;
import com.example.project.repository.TrainingClassRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/students")
public class StudentAdminController {

    private final StudentRepository studentRepository;
    private final TrainingClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;

    public StudentAdminController(StudentRepository studentRepository,
                                  TrainingClassRepository classRepository,
                                  ClassStudentRepository classStudentRepository) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.classStudentRepository = classStudentRepository;
    }

    @GetMapping
    public String listStudents(@RequestParam(required = false) String q,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String code,
                               @RequestParam(required = false) StudentStatus status,
                               @RequestParam(required = false) Long classId,
                               Model model) {
        List<Student> students;
        
        // Use multi-field filter if any filter is present, otherwise use standard search or find all
        boolean hasFilters = (name != null && !name.trim().isEmpty()) || 
                             (code != null && !code.trim().isEmpty()) || 
                             status != null || 
                             classId != null;
                             
        if (hasFilters) {
            students = studentRepository.filterStudents(
                    (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                    (code != null && !code.trim().isEmpty()) ? code.trim() : null,
                    status,
                    classId
            );
        } else if (q != null && !q.trim().isEmpty()) {
            students = studentRepository.search(q.trim());
        } else {
            students = studentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }

        model.addAttribute("students", students);
        model.addAttribute("keyword", q);
        model.addAttribute("filterName", name);
        model.addAttribute("filterCode", code);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterClassId", classId);
        
        model.addAttribute("classes", classRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("statuses", StudentStatus.values());
        
        model.addAttribute("currentClasses", buildCurrentClassMap(students));
        model.addAttribute("pageTitle", "Quản lý học viên");
        return "admin/student/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Student student = new Student();
        student.setStatus(StudentStatus.ACTIVE);
        populateForm(model, student, "/admin/students", "Thêm học viên", new HashMap<>(), null);
        return "admin/student/form";
    }

    @PostMapping
    public String createStudent(Model model,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String dob,
                                @RequestParam(required = false) String parentName,
                                @RequestParam(required = false) String parentPhone,
                                @RequestParam(required = false) String parentEmail,
                                @RequestParam(required = false) String parentAddress,
                                @RequestParam(required = false) Long classId,
                                @RequestParam(required = false) StudentStatus status) {
        ValidationResult result = buildStudentFromForm(new Student(), name, dob, parentName, parentPhone,
                parentEmail, parentAddress, classId, status);

        if (result.hasErrors()) {
            populateForm(model, result.student, "/admin/students", "Thêm học viên", result.errors, classId);
            return "admin/student/form";
        }

        result.student.setCode(generateStudentCode());
        result.student.setCreatedAt(LocalDateTime.now());
        result.student.setUpdatedAt(LocalDateTime.now());
        Student saved = studentRepository.save(result.student);
        syncClassAssignment(saved, classId);
        return "redirect:/admin/students";
    }

    @GetMapping("/{id}")
    public String viewStudent(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        List<ClassStudent> memberships = classStudentRepository.findMembershipsByStudentId(id);
        model.addAttribute("student", student);
        model.addAttribute("currentClass", findCurrentClass(memberships));
        model.addAttribute("memberships", memberships);
        model.addAttribute("pageTitle", "Chi tiết học viên");
        return "admin/student/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        populateForm(model, student, "/admin/students/" + id, "Cập nhật học viên", new HashMap<>(),
                extractCurrentClassId(id));
        return "admin/student/form";
    }

    @PostMapping("/{id}")
    public String updateStudent(@PathVariable Long id,
                                Model model,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String dob,
                                @RequestParam(required = false) String parentName,
                                @RequestParam(required = false) String parentPhone,
                                @RequestParam(required = false) String parentEmail,
                                @RequestParam(required = false) String parentAddress,
                                @RequestParam(required = false) Long classId,
                                @RequestParam(required = false) StudentStatus status) {
        Student existing = studentRepository.findById(id).orElseThrow();
        ValidationResult result = buildStudentFromForm(existing, name, dob, parentName, parentPhone,
                parentEmail, parentAddress, classId, status);

        if (result.hasErrors()) {
            populateForm(model, result.student, "/admin/students/" + id, "Cập nhật học viên", result.errors, classId);
            return "admin/student/form";
        }

        result.student.setUpdatedAt(LocalDateTime.now());
        Student saved = studentRepository.save(result.student);
        syncClassAssignment(saved, classId);
        return "redirect:/admin/students/" + id;
    }

    @RequestMapping(value = "/{id}/delete", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteStudent(@PathVariable Long id) {
        classStudentRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
        return "redirect:/admin/students";
    }

    private void populateForm(Model model, Student student, String formAction, String pageTitle,
                              Map<String, String> errors, Long selectedClassId) {
        model.addAttribute("student", student);
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("errors", errors);
        model.addAttribute("classes", classRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("selectedClassId", selectedClassId);
        model.addAttribute("statuses", StudentStatus.values());
    }

    private ValidationResult buildStudentFromForm(Student student,
                                                  String name,
                                                  String dob,
                                                  String parentName,
                                                  String parentPhone,
                                                  String parentEmail,
                                                  String parentAddress,
                                                  Long classId,
                                                  StudentStatus status) {
        Map<String, String> errors = new HashMap<>();

        String normalizedName = normalize(name);
        String normalizedParentName = normalize(parentName);
        String normalizedParentPhone = normalize(parentPhone);
        String normalizedParentEmail = normalize(parentEmail);
        String normalizedParentAddress = normalize(parentAddress);

        if (normalizedName.isEmpty()) {
            errors.put("name", "Họ tên học viên là bắt buộc.");
        }

        if (!normalizedParentEmail.isEmpty() && !normalizedParentEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.put("parentEmail", "Email phụ huynh không hợp lệ.");
        }

        if (classId != null && classRepository.findById(classId).isEmpty()) {
            errors.put("classId", "Lớp học không tồn tại.");
        }

        LocalDate parsedDob = parseDate(dob, "dob", errors);

        student.setName(normalizedName);
        student.setDob(parsedDob);
        student.setParentName(normalizedParentName.isEmpty() ? null : normalizedParentName);
        student.setParentPhone(normalizedParentPhone.isEmpty() ? null : normalizedParentPhone);
        student.setParentEmail(normalizedParentEmail.isEmpty() ? null : normalizedParentEmail);
        student.setParentAddress(normalizedParentAddress.isEmpty() ? null : normalizedParentAddress);
        student.setStatus(status != null ? status : StudentStatus.ACTIVE);

        return new ValidationResult(student, errors);
    }

    private void syncClassAssignment(Student student, Long selectedClassId) {
        List<ClassStudent> memberships = new ArrayList<>(classStudentRepository.findMembershipsByStudentId(student.getId()));
        boolean alreadyAssigned = false;
        List<ClassStudent> changed = new ArrayList<>();

        for (ClassStudent membership : memberships) {
            Long membershipClassId = membership.getTrainingClass() != null ? membership.getTrainingClass().getId() : null;
            if (selectedClassId != null && selectedClassId.equals(membershipClassId) && !alreadyAssigned) {
                if (membership.getLeaveDate() != null) {
                    membership.setJoinedDate(LocalDate.now());
                    membership.setLeaveDate(null);
                    membership.setRoleInClass("HOC_VIEN");
                    changed.add(membership);
                }
                alreadyAssigned = true;
                continue;
            }

            if (membership.getLeaveDate() == null) {
                membership.setLeaveDate(LocalDate.now());
                changed.add(membership);
            }
        }

        if (!changed.isEmpty()) {
            classStudentRepository.saveAll(changed);
        }

        if (selectedClassId != null && !alreadyAssigned) {
            TrainingClass trainingClass = classRepository.findById(selectedClassId).orElseThrow();
            ClassStudent classStudent = new ClassStudent();
            classStudent.setId(new ClassStudentId(trainingClass.getId(), student.getId()));
            classStudent.setTrainingClass(trainingClass);
            classStudent.setStudent(student);
            classStudent.setJoinedDate(LocalDate.now());
            classStudent.setRoleInClass("HOC_VIEN");
            classStudentRepository.save(classStudent);
        }
    }

    private Map<Long, TrainingClass> buildCurrentClassMap(List<Student> students) {
        Map<Long, TrainingClass> currentClasses = new LinkedHashMap<>();
        if (students == null || students.isEmpty()) {
            return currentClasses;
        }

        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .filter(id -> id != null)
                .toList();
        if (studentIds.isEmpty()) {
            return currentClasses;
        }

        for (ClassStudent membership : classStudentRepository.findActiveMembershipsByStudentIds(studentIds)) {
            Long studentId = membership.getStudent() != null ? membership.getStudent().getId() : null;
            if (studentId != null && !currentClasses.containsKey(studentId)) {
                currentClasses.put(studentId, membership.getTrainingClass());
            }
        }
        return currentClasses;
    }

    private TrainingClass findCurrentClass(List<ClassStudent> memberships) {
        for (ClassStudent membership : memberships) {
            if (membership.getLeaveDate() == null) {
                return membership.getTrainingClass();
            }
        }
        return null;
    }

    private Long extractCurrentClassId(Long studentId) {
        TrainingClass currentClass = findCurrentClass(classStudentRepository.findMembershipsByStudentId(studentId));
        return currentClass != null ? currentClass.getId() : null;
    }

    private String generateStudentCode() {
        long nextId = studentRepository.findNextId();
        String candidate = formatCode(nextId);
        while (studentRepository.findByCode(candidate).isPresent()) {
            nextId++;
            candidate = formatCode(nextId);
        }
        return candidate;
    }

    private String formatCode(long value) {
        return String.format("HV%04d", value);
    }

    private LocalDate parseDate(String value, String fieldKey, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            errors.put(fieldKey, "Ngày sinh không hợp lệ.");
            return null;
        }
    }

    private String normalize(String value) {
        return value != null ? value.trim() : "";
    }

    private static class ValidationResult {
        private final Student student;
        private final Map<String, String> errors;

        private ValidationResult(Student student, Map<String, String> errors) {
            this.student = student;
            this.errors = errors;
        }

        private boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }
}
