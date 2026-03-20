package com.example.project.service.impl;

import com.example.project.dto.CategoryDTO;
import com.example.project.entity.Category;
import com.example.project.repository.CategoryRepository;
import com.example.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CategoryServiceImpl kế thừa AbstractBaseService.
 * → Toàn bộ CRUD (findAll, findById, save, deleteById...) đã được xử lý sẵn.
 * → Chỉ cần implement: getRepository(), toDTO(), toEntity()
 *    + các nghiệp vụ đặc thù của CategoryService.
 */
@Service
public class CategoryServiceImpl
        extends AbstractBaseService<Category, CategoryDTO, Long>
        implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // ── Bắt buộc override: cung cấp repository ──────────────────────────────

    @Override
    protected JpaRepository<Category, Long> getRepository() {
        return categoryRepository;
    }

    // ── Bắt buộc override: mapping Entity ↔ DTO ─────────────────────────────

    @Override
    protected CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    @Override
    protected Category toEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    /**
     * Khi UPDATE: giữ nguyên ID của entity gốc, chỉ cập nhật name/description.
     */
    @Override
    protected Category mergeEntity(Category existing, CategoryDTO dto) {
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        return existing; // giữ nguyên ID, không tạo entity mới
    }

    // ── Nghiệp vụ riêng của Category ────────────────────────────────────────

    @Override
    public List<CategoryDTO> findByNameContaining(String keyword) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}
