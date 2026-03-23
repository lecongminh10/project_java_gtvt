package com.example.project.handler;

import com.example.project.dto.CategoryDTO;
import com.example.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handler xử lý tất cả request liên quan đến Category.
 * Tách biệt logic từ Controller.
 */
@Component
public class CategoryRequestHandler {

    @Autowired
    private CategoryService categoryService;

    /**
     * Xử lý request CREATE category
     * - Kiểm tra tên đã tồn tại
     * - Lưu vào database
     * - Trả về result
     */
    public CategoryHandlerResult handleCreateRequest(CategoryDTO categoryDTO,
                                                      RedirectAttributes redirectAttributes) {
        // Validation: Kiểm tra tên đã tồn tại chưa
        if (categoryService.existsByName(categoryDTO.getName())) {
            String errorMsg = "Tên danh mục '" + categoryDTO.getName() + "' đã tồn tại!";
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return CategoryHandlerResult.redirect("/admin/categories/create", false);
        }

        try {
            // Xử lý: Lưu category vào database
            categoryService.create(categoryDTO);
            redirectAttributes.addFlashAttribute("success", "Tạo danh mục thành công!");
            return CategoryHandlerResult.redirect("/admin/categories", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return CategoryHandlerResult.redirect("/admin/categories/create", false);
        }
    }

    /**
     * Xử lý request UPDATE category
     * - Kiểm tra ID có tồn tại
     * - Cập nhật dữ liệu
     * - Trả về result
     */
    public CategoryHandlerResult handleUpdateRequest(Long id,
                                                      CategoryDTO categoryDTO,
                                                      RedirectAttributes redirectAttributes) {
        try {
            // Validation: Kiểm tra category có tồn tại không
            CategoryDTO existing = categoryService.getById(id);
            if (existing == null) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                return CategoryHandlerResult.redirect("/admin/categories", false);
            }

            // Xử lý: Cập nhật category
            categoryService.update(id, categoryDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
            return CategoryHandlerResult.redirect("/admin/categories", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return CategoryHandlerResult.redirect("/admin/categories/" + id + "/edit", false);
        }
    }

    /**
     * Xử lý request DELETE category
     * - Kiểm tra ID có tồn tại
     * - Xóa khỏi database
     * - Trả về result
     */
    public CategoryHandlerResult handleDeleteRequest(Long id,
                                                      RedirectAttributes redirectAttributes) {
        try {
            // Validation: Kiểm tra category có tồn tại không
            CategoryDTO existing = categoryService.getById(id);
            if (existing == null) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                return CategoryHandlerResult.redirect("/admin/categories", false);
            }

            // Xử lý: Xóa category
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công!");
            return CategoryHandlerResult.redirect("/admin/categories", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return CategoryHandlerResult.redirect("/admin/categories", false);
        }
    }
}
