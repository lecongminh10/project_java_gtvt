package com.example.project.controller.admin;

import com.example.project.dto.SubjectDTO;
import com.example.project.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/create")
    public String createSubjectForm(Model model) {
        SubjectDTO subject = new SubjectDTO();
        model.addAttribute("pageTitle", "Create subject");
        model.addAttribute("subject", subject);
        model.addAttribute("actionUrl", "/admin/subjects/create");
        return "admin/subject/form";
    }

    @PostMapping("/create")
    public String createSubject(@ModelAttribute("subject") SubjectDTO subjectDTO) {
        subjectService.create(subjectDTO);
        return "redirect:/admin/subjects";
    }

    @GetMapping("/{id}/edit")
    public String editSubjectForm(@PathVariable Long id, Model model) {
        SubjectDTO subject = subjectService.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
        model.addAttribute("pageTitle", "Edit subject");
        model.addAttribute("subject", subject);
        model.addAttribute("actionUrl", "/admin/subjects/" + id + "/edit");
        return "admin/subject/form";
    }

    @PostMapping("/{id}/edit")
    public String updateSubject(@PathVariable Long id, @ModelAttribute("subject") SubjectDTO subjectDTO) {
        subjectService.update(id, subjectDTO);
        return "redirect:/admin/subjects";
    }
}
