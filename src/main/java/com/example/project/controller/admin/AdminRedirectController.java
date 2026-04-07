package com.example.project.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminRedirectController {

    @GetMapping("/admin/go-teacher")
    public String goTeacher() {
        return "redirect:/teacher/profile";
    }
}