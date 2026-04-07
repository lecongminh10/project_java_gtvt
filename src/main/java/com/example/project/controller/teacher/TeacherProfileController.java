package com.example.project.controller.teacher;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/teacher/profile")
public class TeacherProfileController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Xem thông tin cá nhân
    @GetMapping
    public String profile(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "teacher/profile";
    }

    // Đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(Principal principal,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword) {

        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        // 1. Kiểm tra mật khẩu cũ (đúng chuẩn BCrypt)
        if (!encoder.matches(oldPassword, user.getPassword())) {
            return "redirect:/teacher/profile?error";
        }

        // 2. Cập nhật mật khẩu mới (sẽ được encode trong entity)
        user.setPassword(newPassword);
        user.setPasswordChanged(true);

        userRepository.save(user);

        return "redirect:/teacher/profile?success";
    }

    // API test
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        System.out.println(">>> CONTROLLER RUNNING <<<");
        return "OK";
    }
}