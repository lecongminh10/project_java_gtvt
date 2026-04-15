package com.example.project.controller.admin;

import com.example.project.entity.ClassStatus;
import com.example.project.entity.ClassStudent;
import com.example.project.entity.ClassStudentId;
import com.example.project.entity.Course;
import com.example.project.entity.Student;
import com.example.project.entity.StudentStatus;
import com.example.project.entity.Teacher;
import com.example.project.entity.TrainingClass;
import com.example.project.repository.ClassStudentRepository;
import com.example.project.repository.CourseRepository;
import com.example.project.repository.StudentRepository;
import com.example.project.repository.TeacherRepository;
import com.example.project.repository.TrainingClassRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/admin/classes")
public class ClassAdminController {

    private final TrainingClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ClassStudentRepository classStudentRepository;

    public ClassAdminController(TrainingClassRepository classRepository,
                                CourseRepository courseRepository,
                                TeacherRepository teacherRepository,
                                StudentRepository studentRepository,
                                ClassStudentRepository classStudentRepository) {
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.classStudentRepository = classStudentRepository;
    }

    @GetMapping
    public String listClasses(@RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
        // Search by keyword when provided; otherwise list newest classes.
        List<TrainingClass> allClasses;
        if (q != null && !q.trim().isEmpty()) {
            allClasses = classRepository.search(q.trim());
        } else {
            allClasses = classRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }

        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, allClasses.size());
        List<TrainingClass> pageContent = allClasses.subList(start, end);

        model.addAttribute("classes", pageContent);
        model.addAttribute("keyword", q);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalElements", allClasses.size());
        model.addAttribute("totalPages", (allClasses.size() + size - 1) / size);
        model.addAttribute("pageTitle", "Quản lý lớp học");
        return "admin/class/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Initialize default draft for create form.
        TrainingClass draft = new TrainingClass();
        draft.setStatus(ClassStatus.MOI);
        populateClassForm(model, draft, "/admin/classes", "Thêm lớp học", new HashMap<>(), new HashSet<>());
        return "admin/class/form";
    }

    @PostMapping
    public String createClass(Model model,
                              @RequestParam(required = false) String code,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) Long courseId,
                              @RequestParam(required = false) Long teacherId,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              @RequestParam(required = false) String totalSessions,
                              @RequestParam(required = false) String room,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) ClassStatus status,
                      @RequestParam(required = false) List<Long> studentIds,
                      RedirectAttributes redirectAttributes) {
        // Validate form data and keep user input on errors.
        ValidationResult result = buildClassFromForm(new TrainingClass(), code, name, courseId, teacherId,
                startDate, endDate, totalSessions, room, description, status);

        if (result.hasErrors()) {
            Set<Long> selectedIds = resolveSelectedStudentIds(studentIds);
            populateClassForm(model, result.trainingClass, "/admin/classes", "Thêm lớp học", result.errors, selectedIds);
            return "admin/class/form";
        }

        // Persist class and link selected students.
        result.trainingClass.setAutoCreated(false);
        result.trainingClass.setCreatedAt(LocalDateTime.now());
        result.trainingClass.setUpdatedAt(LocalDateTime.now());
        TrainingClass saved = classRepository.save(result.trainingClass);

        Set<Long> selectedIds = resolveSelectedStudentIds(studentIds);
        if (!selectedIds.isEmpty()) {
            attachStudents(saved, selectedIds, new HashSet<>());
        }
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã thêm lớp học thành công.");
        return "redirect:/admin/classes";
    }

    @GetMapping("/{id}")
    public String viewClass(@PathVariable Long id, Model model) {
        // Show read-only class detail.
        TrainingClass trainingClass = classRepository.findById(id).orElseThrow();
        List<ClassStudent> classStudents = classStudentRepository.findActiveMembersByClassId(id);
        model.addAttribute("clazz", trainingClass);
        model.addAttribute("classStudents", classStudents);
        model.addAttribute("pageTitle", "Chi tiết lớp học");
        return "admin/class/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Prevent edits on finished classes.
        TrainingClass trainingClass = classRepository.findById(id).orElseThrow();
        if (trainingClass.getStatus() == ClassStatus.KET_THUC) {
            return "redirect:/admin/classes";
        }
        populateClassForm(model, trainingClass, "/admin/classes/" + id, "Cập nhật lớp học", new HashMap<>(), null);
        return "admin/class/form";
    }

    @PostMapping("/{id}")
    public String updateClass(@PathVariable Long id,
                              Model model,
                              @RequestParam(required = false) String code,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) Long courseId,
                              @RequestParam(required = false) Long teacherId,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              @RequestParam(required = false) String totalSessions,
                              @RequestParam(required = false) String room,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) ClassStatus status,
                              @RequestParam(required = false) List<Long> studentIds,
                              RedirectAttributes redirectAttributes) {
        // Validate updates and block edits when class finished.
        TrainingClass existing = classRepository.findById(id).orElseThrow();
        if (existing.getStatus() == ClassStatus.KET_THUC) {
            return "redirect:/admin/classes";
        }

        ValidationResult result = buildClassFromForm(existing, code, name, courseId, teacherId,
                startDate, endDate, totalSessions, room, description, status);

        if (result.hasErrors()) {
            Set<Long> selectedIds = resolveSelectedStudentIds(studentIds);
            populateClassForm(model, result.trainingClass, "/admin/classes/" + id, "Cập nhật lớp học", result.errors, selectedIds);
            return "admin/class/form";
        }

        // Save changes and attach new students if provided.
        result.trainingClass.setUpdatedAt(LocalDateTime.now());
        classRepository.save(result.trainingClass);

        Set<Long> selectedIds = resolveSelectedStudentIds(studentIds);
        if (!selectedIds.isEmpty()) {
            Set<Long> existingIds = extractStudentIds(existing);
            attachStudents(result.trainingClass, selectedIds, existingIds);
        }
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã cập nhật lớp học thành công.");
        return "redirect:/admin/classes";
    }

    @PostMapping("/{id}/delete")
    public String deleteClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Disallow delete when class is finished.
        TrainingClass trainingClass = classRepository.findById(id).orElseThrow();
        if (trainingClass.getStatus() == ClassStatus.DANG_HOC) {
            redirectAttributes.addFlashAttribute("toastError", "Không thể xóa lớp khi đang học.");
            return "redirect:/admin/classes";
        }
        classRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã xóa lớp học thành công.");
        return "redirect:/admin/classes";
    }

    @GetMapping("/{id}/transfer")
    public String showTransferForm(@PathVariable Long id, Model model) {
        // Build transfer draft from source class and validate eligibility.
        TrainingClass source = classRepository.findById(id).orElseThrow();
        TrainingClass draft = buildTransferDraft(source);
        Map<String, String> errors = new HashMap<>();
        boolean locked = source.getStatus() != ClassStatus.KET_THUC;
        if (locked) {
            errors.put("transfer", "Chỉ được chuyển lớp khi trạng thái là Kết thúc.");
        }
        populateTransferModel(model, source, draft, errors);
        model.addAttribute("locked", locked);
        model.addAttribute("pageTitle", "Chuyển lớp");
        return "admin/class/transfer";
    }

    @PostMapping("/{id}/transfer")
    public String submitTransfer(@PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(required = false) String code,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) Long courseId,
                                 @RequestParam(required = false) Long teacherId,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 @RequestParam(required = false) String totalSessions,
                                 @RequestParam(required = false) String room,
                                 @RequestParam(required = false) String description) {
        // Validate transfer input and create a new class derived from source.
        TrainingClass source = classRepository.findById(id).orElseThrow();
        Map<String, String> errors = new HashMap<>();

        if (source.getStatus() != ClassStatus.KET_THUC) {
            errors.put("transfer", "Chỉ được chuyển lớp khi trạng thái là Kết thúc.");
        }

        String normalizedCode = code != null ? code.trim() : "";
        String normalizedName = name != null ? name.trim() : "";

        if (normalizedCode.isEmpty()) {
            errors.put("code", "Mã lớp bắt buộc.");
        }
        if (!normalizedCode.isEmpty()) {
            if (classRepository.existsByCode(normalizedCode)) {
                errors.put("code", "Mã lớp đã tồn tại.");
            }
        }
        if (normalizedName.isEmpty()) {
            errors.put("name", "Tên lớp bắt buộc.");
        }

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            errors.put("courseId", "Vui lòng chọn khóa học.");
        }

        Teacher teacher = null;
        if (teacherId != null) {
            teacher = teacherRepository.findById(teacherId).orElse(null);
            if (teacher == null) {
                errors.put("teacherId", "Giáo viên không tồn tại.");
            }
        }

        LocalDate parsedStartDate = parseDate(startDate, "startDate", errors);
        LocalDate parsedEndDate = parseDate(endDate, "endDate", errors);
        Integer parsedTotalSessions = parseNonNegativeInteger(totalSessions, "totalSessions", errors);

        TrainingClass draft = buildTransferDraft(source);
        draft.setCode(normalizedCode);
        draft.setName(normalizedName);
        draft.setCourse(course);
        draft.setTeacher(teacher);
        draft.setStartDate(parsedStartDate);
        draft.setEndDate(parsedEndDate);
        draft.setTotalSessions(parsedTotalSessions);
        draft.setRoom(room != null ? room.trim() : null);
        draft.setDescription(description != null ? description.trim() : null);

        if (!errors.isEmpty()) {
            populateTransferModel(model, source, draft, errors);
            model.addAttribute("pageTitle", "Chuyển lớp");
            return "admin/class/transfer";
        }

        // Create new class with MOI status and link to source.
        TrainingClass newClass = new TrainingClass();
        newClass.setCode(normalizedCode);
        newClass.setName(normalizedName);
        newClass.setCourse(course);
        newClass.setTeacher(teacher);
        newClass.setStartDate(parsedStartDate);
        newClass.setEndDate(parsedEndDate);
        newClass.setTotalSessions(parsedTotalSessions);
        newClass.setRoom(room != null ? room.trim() : null);
        newClass.setDescription(description != null ? description.trim() : null);
        newClass.setStatus(ClassStatus.MOI);
        newClass.setAutoCreated(true);
        newClass.setSourceClassId(source.getId());
        newClass.setCreatedAt(LocalDateTime.now());
        newClass.setUpdatedAt(LocalDateTime.now());

        classRepository.save(newClass);

        // Touch source class to reflect transfer operation.
        source.setUpdatedAt(LocalDateTime.now());
        classRepository.save(source);
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã chuyển lớp và tạo lớp mới.");
        return "redirect:/admin/classes";
    }

    private TrainingClass buildTransferDraft(TrainingClass source) {
        // Pre-fill draft values based on source class.
        TrainingClass draft = new TrainingClass();
        draft.setCode(source.getCode() + "-NEXT");
        draft.setName(source.getName() + " (khóa tiếp)");
        draft.setCourse(source.getCourse());
        draft.setTeacher(source.getTeacher());
        draft.setTotalSessions(source.getTotalSessions());
        draft.setRoom(source.getRoom());
        draft.setDescription(source.getDescription());
        return draft;
    }

    private void populateTransferModel(Model model, TrainingClass source, TrainingClass draft, Map<String, String> errors) {
        // Provide transfer form data and reference lists.
        model.addAttribute("sourceClass", source);
        model.addAttribute("draft", draft);
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse(Sort.by("name")));
        model.addAttribute("teachers", teacherRepository.findAll(Sort.by("name")));
        model.addAttribute("errors", errors);
    }

    private void populateClassForm(Model model, TrainingClass trainingClass, String formAction,
                                   String pageTitle, Map<String, String> errors,
                                   Set<Long> selectedStudentIds) {
        // Rehydrate class form with reference data and selections.
        model.addAttribute("clazz", trainingClass);
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse(Sort.by("name")));
        model.addAttribute("teachers", teacherRepository.findAll(Sort.by("name")));
        model.addAttribute("statuses", ClassStatus.values());
        model.addAttribute("students", studentRepository.findByStatusOrderByNameAsc(StudentStatus.ACTIVE));
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("errors", errors);
        model.addAttribute("selectedStudentIds", selectedStudentIds != null ? selectedStudentIds : extractStudentIds(trainingClass));
    }

    private ValidationResult buildClassFromForm(TrainingClass trainingClass,
                                                String code,
                                                String name,
                                                Long courseId,
                                                Long teacherId,
                                                String startDate,
                                                String endDate,
                                                String totalSessions,
                                                String room,
                                                String description,
                                                ClassStatus status) {
        // Normalize input and collect validation errors.
        Map<String, String> errors = new HashMap<>();

        String normalizedCode = code != null ? code.trim() : "";
        String normalizedName = name != null ? name.trim() : "";

        if (normalizedCode.isEmpty()) {
            errors.put("code", "Mã lớp bắt buộc.");
        }
        if (!normalizedCode.isEmpty()) {
            Long currentId = trainingClass.getId();
            boolean duplicate = currentId == null
                    ? classRepository.existsByCode(normalizedCode)
                    : classRepository.existsByCodeAndIdNot(normalizedCode, currentId);
            if (duplicate) {
                errors.put("code", "Mã lớp đã tồn tại.");
            }
        }
        if (normalizedName.isEmpty()) {
            errors.put("name", "Tên lớp bắt buộc.");
        }

        Course course = null;
        if (courseId == null) {
            errors.put("courseId", "Vui lòng chọn khóa học.");
        } else {
            course = courseRepository.findById(courseId).orElse(null);
            if (course == null || course.isDeleted()) {
                errors.put("courseId", "Khóa học không tồn tại.");
            }
        }

        Teacher teacher = null;
        if (teacherId != null) {
            teacher = teacherRepository.findById(teacherId).orElse(null);
            if (teacher == null) {
                errors.put("teacherId", "Giáo viên không tồn tại.");
            }
        }

        // Parse date and numeric fields.
        LocalDate parsedStartDate = parseDate(startDate, "startDate", errors);
        LocalDate parsedEndDate = parseDate(endDate, "endDate", errors);
        Integer parsedTotalSessions = parseNonNegativeInteger(totalSessions, "totalSessions", errors);

        trainingClass.setCode(normalizedCode);
        trainingClass.setName(normalizedName);
        trainingClass.setCourse(course);
        trainingClass.setTeacher(teacher);
        trainingClass.setStartDate(parsedStartDate);
        trainingClass.setEndDate(parsedEndDate);
        trainingClass.setTotalSessions(parsedTotalSessions);
        trainingClass.setRoom(room != null ? room.trim() : null);
        trainingClass.setDescription(description != null ? description.trim() : null);
        trainingClass.setStatus(status != null ? status : ClassStatus.MOI);

        return new ValidationResult(trainingClass, errors);
    }

    private static class ValidationResult {
        private final TrainingClass trainingClass;
        private final Map<String, String> errors;

        private ValidationResult(TrainingClass trainingClass, Map<String, String> errors) {
            this.trainingClass = trainingClass;
            this.errors = errors;
        }

        private boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }

    private Set<Long> extractStudentIds(TrainingClass trainingClass) {
        // Extract selected student ids from class entity.
        if (trainingClass == null || trainingClass.getStudents() == null) {
            return new HashSet<>();
        }
        Set<Long> ids = new HashSet<>();
        for (Student student : trainingClass.getStudents()) {
            if (student.getId() != null) {
                ids.add(student.getId());
            }
        }
        return ids;
    }

    private Set<Long> resolveSelectedStudentIds(List<Long> studentIds) {
        // Normalize nullable list to a set.
        Set<Long> selected = new HashSet<>();
        if (studentIds != null) {
            selected.addAll(studentIds);
        }
        return selected;
    }

    private void attachStudents(TrainingClass trainingClass, Set<Long> studentIds, Set<Long> existingIds) {
        // Create join entities for new students only.
        List<ClassStudent> toSave = new ArrayList<>();
        for (Long studentId : studentIds) {
            if (existingIds != null && existingIds.contains(studentId)) {
                continue;
            }
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                continue;
            }
            ClassStudentId id = new ClassStudentId(trainingClass.getId(), studentId);
            ClassStudent classStudent = new ClassStudent();
            classStudent.setId(id);
            classStudent.setTrainingClass(trainingClass);
            classStudent.setStudent(student);
            classStudent.setJoinedDate(LocalDate.now());
            classStudent.setRoleInClass("HOC_VIEN");
            toSave.add(classStudent);
        }
        if (!toSave.isEmpty()) {
            classStudentRepository.saveAll(toSave);
        }
    }

    private LocalDate parseDate(String value, String fieldKey, Map<String, String> errors) {
        // Accept blank as null, otherwise validate ISO date.
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            errors.put(fieldKey, "Ngày không hợp lệ.");
            return null;
        }
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
}
