package com.example.project.controller.teacher;

import com.example.project.entity.Teacher;
import com.example.project.entity.User;
import com.example.project.repository.TeacherRepository;
import com.example.project.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/teacher/account")
public class TeacherAccountController {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public TeacherAccountController(TeacherRepository teacherRepository, UserRepository userRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewAccount(@RequestParam(value = "id", required = false) Long id,
                              @RequestParam(value = "username", required = false) String username,
                              Model model) {
        Optional<Teacher> teacherOpt = resolveTeacher(id, username);
        if (teacherOpt.isEmpty()) {
            model.addAttribute("pageTitle", "Tài khoản giáo viên");
            model.addAttribute("hasTeacher", false);
            return "teacher/account/profile";
        }

        Teacher teacher = teacherOpt.get();
        model.addAttribute("pageTitle", "Tài khoản giáo viên");
        model.addAttribute("teacher", teacher);
        model.addAttribute("hasTeacher", true);
        model.addAttribute("form", TeacherAccountForm.fromTeacher(teacher));
        return "teacher/account/profile";
    }

    @PostMapping
    public String updateAccount(@RequestParam(value = "id", required = false) Long id,
                                @RequestParam(value = "username", required = false) String username,
                                @ModelAttribute("form") TeacherAccountForm form,
                                RedirectAttributes redirectAttributes) {
        Optional<Teacher> teacherOpt = resolveTeacher(id, username);
        if (teacherOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giáo viên để cập nhật.");
            return "redirect:/teacher/account";
        }

        Teacher teacher = teacherOpt.get();
        teacher.setName(form.getName());
        teacher.setEmail(form.getEmail());
        teacher.setPhone(form.getPhone());
        teacher.setDob(form.getDob());
        teacher.setUpdatedAt(LocalDateTime.now());
        teacherRepository.save(teacher);

        User user = teacher.getUser();
        if (user != null) {
            user.setFullName(form.getName());
            user.setEmail(form.getEmail());
            user.setPhone(form.getPhone());
            userRepository.save(user);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công.");
        return buildRedirectUrl(id, username);
    }

    private Optional<Teacher> resolveTeacher(Long id, String username) {
        if (username != null && !username.isBlank()) {
            return teacherRepository.findByUserUsername(username.trim());
        }
        if (id != null) {
            return teacherRepository.findById(id);
        }
        return teacherRepository.findFirstByOrderByIdAsc();
    }

    private String buildRedirectUrl(Long id, String username) {
        if (username != null && !username.isBlank()) {
            return "redirect:/teacher/account?username=" + username.trim();
        }
        if (id != null) {
            return "redirect:/teacher/account?id=" + id;
        }
        return "redirect:/teacher/account";
    }

    public static class TeacherAccountForm {
        private String name;
        private String email;
        private String phone;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate dob;

        private String username;

        public static TeacherAccountForm fromTeacher(Teacher teacher) {
            TeacherAccountForm form = new TeacherAccountForm();
            form.setName(teacher.getName());
            form.setEmail(teacher.getEmail());
            form.setPhone(teacher.getPhone());
            form.setDob(teacher.getDob());
            if (teacher.getUser() != null) {
                form.setUsername(teacher.getUser().getUsername());
            }
            return form;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public LocalDate getDob() {
            return dob;
        }

        public void setDob(LocalDate dob) {
            this.dob = dob;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
