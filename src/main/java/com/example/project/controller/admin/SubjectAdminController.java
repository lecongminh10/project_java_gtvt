package com.example.project.controller.admin;

import com.example.project.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/subjects")
public class SubjectAdminController {

    @Autowired
    private SubjectService subjectService;

    // Return UI
    @GetMapping("")
    public String listSubjects(Model model) {
        model.addAttribute("pageTitle", "Subjects Management");
        model.addAttribute("subjects", subjectService.findAll());
        return "admin/subject/list";
    }

}
