package com.example.project.controller.admin;

import com.example.project.dto.CategoryDTO;
import com.example.project.handler.CategoryHandlerResult;
import com.example.project.handler.CategoryRequestHandler;
import com.example.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller CRUD cho Category (Admin).
 *
 * URL map:
 *   GET  /admin/categories          → danh sách
 *   GET  /admin/categories/create   → form tạo mới
 *   POST /admin/categories/create   → lưu mới
 *   GET  /admin/categories/{id}/edit → form sửa
 *   POST /admin/categories/{id}/edit → lưu sửa
 *   POST /admin/categories/{id}/delete → xóa
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRequestHandler categoryRequestHandler;

    // ══════════════════════════════════════════════
    //  LIST — GET /admin/categories
    // ══════════════════════════════════════════════
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("pageTitle", "Quản lý danh mục");
        return "admin/category/list"; // → templates/admin/category/list.html
    }

    // ══════════════════════════════════════════════
    //  CREATE (hiển thị form) — GET /admin/categories/create
    // ══════════════════════════════════════════════
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CategoryDTO()); // form rỗng
        model.addAttribute("pageTitle", "Thêm danh mục");
        return "admin/category/form"; // → templates/admin/category/form.html
    }

    // ══════════════════════════════════════════════
    //  CREATE (xử lý form) — POST /admin/categories/create
    // ══════════════════════════════════════════════
    @PostMapping("/create")
    public String create(@ModelAttribute CategoryDTO categoryDTO,
                         RedirectAttributes redirectAttributes) {
        // Gửi request tới handler xử lý
        CategoryHandlerResult result = categoryRequestHandler.handleCreateRequest(
                categoryDTO,
                redirectAttributes
        );
        return "redirect:" + result.getRedirectUrl();
    }

    // ══════════════════════════════════════════════
    //  UPDATE (hiển thị form) — GET /admin/categories/{id}/edit
    // ══════════════════════════════════════════════
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getById(id); // ném 404 nếu không có
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", "Sửa danh mục");
        return "admin/category/form"; // Dùng chung form với CREATE
    }

    // ══════════════════════════════════════════════
    //  UPDATE (xử lý form) — POST /admin/categories/{id}/edit
    // ══════════════════════════════════════════════
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute CategoryDTO categoryDTO,
                         RedirectAttributes redirectAttributes) {
        // Gửi request tới handler xử lý
        CategoryHandlerResult result = categoryRequestHandler.handleUpdateRequest(
                id,
                categoryDTO,
                redirectAttributes
        );
        return "redirect:" + result.getRedirectUrl();
    }

    // ══════════════════════════════════════════════
    //  DELETE — POST /admin/categories/{id}/delete
    // ══════════════════════════════════════════════
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        // Gửi request tới handler xử lý
        CategoryHandlerResult result = categoryRequestHandler.handleDeleteRequest(
                id,
                redirectAttributes
        );
        return "redirect:" + result.getRedirectUrl();
    }
}
