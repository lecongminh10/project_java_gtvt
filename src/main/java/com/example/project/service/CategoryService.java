package com.example.project.service;

import com.example.project.dto.CategoryDTO;
import java.util.List;

/**
 * CategoryService mở rộng BaseService<CategoryDTO, Long>.
 * Đã có sẵn: findAll, findById, save, deleteById, existsById, count.
 * Khai báo thêm ở đây các nghiệp vụ đặc thù của Category.
 */
public interface CategoryService extends BaseService<CategoryDTO, Long> {

    /** Tìm danh mục theo tên (gợi ý tìm kiếm) */
    List<CategoryDTO> findByNameContaining(String keyword);

    /** Kiểm tra tên danh mục đã tồn tại chưa */
    boolean existsByName(String name);
}
