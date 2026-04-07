package com.example.project.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping({ "", "/", "/dashboard" })
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/subjects")
    public String listSubjects(Model model) {
        model.addAttribute("pageTitle", "Subjects Management");
        return "admin/subject/list";
    }

    @GetMapping("/courses-dashboard")
    public String listCourses(Model model) {
        model.addAttribute("pageTitle", "Courses Management");
        return "admin/course/list";
    }
}
