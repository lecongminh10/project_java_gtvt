package com.example.project.controller.teacher;

import com.example.project.entity.Material;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/teacher/materials")
public class TeacherMaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private UserRepository userRepository;

    private final String UPLOAD_DIR = "uploads/";

    // LIST
    @GetMapping
    public String list(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("materials", materialService.getByTeacher(user));
        return "teacher/materials";
    }

    // FORM ADD
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("material", new Material());
        return "teacher/material_form";
    }

    // SAVE
    @PostMapping("/save")
    public String save(@ModelAttribute Material material,
                       @RequestParam("file") MultipartFile file,
                       Principal principal) throws IOException {

        User user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            material.setFilePath(fileName);
        }

        material.setTeacher(user);
        materialService.save(material);

        return "redirect:/teacher/materials";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        materialService.delete(id);
        return "redirect:/teacher/materials";
    }
}