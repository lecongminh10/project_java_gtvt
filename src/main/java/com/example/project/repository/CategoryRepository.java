package com.example.project.repository;

import com.example.project.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Tìm danh mục theo tên (không phân biệt hoa thường) */
    List<Category> findByNameContainingIgnoreCase(String keyword);

    /** Kiểm tra tên đã tồn tại chưa */
    boolean existsByNameIgnoreCase(String name);
}
