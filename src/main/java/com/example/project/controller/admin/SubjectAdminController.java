package com.example.project.controller.admin;

import com.example.project.dto.SubjectDTO;
import com.example.project.entity.SubjectLevel;
import com.example.project.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/subjects")
public class SubjectAdminController {

    @Autowired
    private SubjectService subjectService;

    // Return UI
    @GetMapping("")
    public String listSubjects(@RequestParam(required = false) String subjectName, @RequestParam(required = false) SubjectLevel subjectLevel, Model model) {
        List<SubjectDTO> subjects = ((subjectName != null && !subjectName.isEmpty()) || subjectLevel != null)
                ? subjectService.searchSubject(subjectName, subjectLevel)
                : subjectService.findAll();
        model.addAttribute("pageTitle", "Subjects Management");
        model.addAttribute("subjectName", subjectName);
        model.addAttribute("subjectLevel", subjectLevel);
        model.addAttribute("subjects", subjects);
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
    public String createSubject(@ModelAttribute("subject") SubjectDTO subjectDTO, Model model,
                                RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        if (subjectDTO.getCode() == null || subjectDTO.getCode().trim().isEmpty()) {
            errors.put("code", "Mã môn học không được để trống");
        }
        if (subjectDTO.getName() == null || subjectDTO.getName().trim().isEmpty()) {
            errors.put("name", "Tên môn học không được để trống");
        }
        if (subjectDTO.getLevel() == null) {
            errors.put("level", "Cấp học không được để trống");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("pageTitle", "Create subject");
            model.addAttribute("subject", subjectDTO);
            model.addAttribute("actionUrl", "/admin/subjects/create");
            return "admin/subject/form";
        }
        subjectService.create(subjectDTO);
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã thêm môn học thành công.");
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
    public String updateSubject(@PathVariable Long id, @ModelAttribute("subject") SubjectDTO subjectDTO, Model model,
                                RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();
        if (subjectDTO.getCode() == null || subjectDTO.getCode().trim().isEmpty()) {
            errors.put("code", "Mã môn học không được để trống");
        }
        if (subjectDTO.getName() == null || subjectDTO.getName().trim().isEmpty()) {
            errors.put("name", "Tên môn học không được để trống");
        }
        if (subjectDTO.getLevel() == null) {
            errors.put("level", "Cấp học không được để trống");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("pageTitle", "Edit subject");
            model.addAttribute("subject", subjectDTO);
            model.addAttribute("actionUrl", "/admin/subjects/" + id + "/edit");
            return "admin/subject/form";
        }
        subjectService.update(id, subjectDTO);
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã cập nhật môn học thành công.");
        return "redirect:/admin/subjects";
    }

    @PostMapping("/{id}/delete")
    public String deleteSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            subjectService.deleteById(id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Đã xóa môn học thành công.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("toastError", ex.getMessage());
        }
        return "redirect:/admin/subjects";
    }
}