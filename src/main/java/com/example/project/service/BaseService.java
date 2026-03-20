package com.example.project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Generic Base Service Interface — định nghĩa đầy đủ các nhóm thao tác chuẩn.
 *
 * @param <DTO> Kiểu DTO (CategoryDTO, ProductDTO, ...)
 * @param <ID>  Kiểu khóa chính (Long, Integer, String, ...)
 */
public interface BaseService<DTO, ID> {

    // ══════════════════════════════════════════════════════════
    //  LIST — Lấy danh sách
    // ══════════════════════════════════════════════════════════

    /** Lấy toàn bộ danh sách (không sắp xếp) */
    List<DTO> findAll();

    /** Lấy toàn bộ danh sách theo thứ tự sắp xếp */
    List<DTO> findAll(Sort sort);

    /** Lấy danh sách theo trang (phân trang + sắp xếp) */
    Page<DTO> findAll(Pageable pageable);

    /** Lấy danh sách theo một tập hợp ID */
    List<DTO> findAllById(Iterable<ID> ids);


    // ══════════════════════════════════════════════════════════
    //  FIND — Tìm kiếm đơn lẻ
    // ══════════════════════════════════════════════════════════

    /** Tìm một bản ghi theo ID, trả Optional (tránh null) */
    Optional<DTO> findById(ID id);

    /**
     * Tìm một bản ghi theo ID, ném ngoại lệ nếu không tìm thấy.
     * Tiện dụng hơn findById() khi chắc chắn bản ghi phải tồn tại.
     */
    DTO getById(ID id);


    // ══════════════════════════════════════════════════════════
    //  CREATE — Tạo mới
    // ══════════════════════════════════════════════════════════

    /** Tạo và lưu một bản ghi mới */
    DTO create(DTO dto);

    /** Tạo và lưu nhiều bản ghi mới cùng lúc */
    List<DTO> createAll(Iterable<DTO> dtos);


    // ══════════════════════════════════════════════════════════
    //  UPDATE — Cập nhật
    // ══════════════════════════════════════════════════════════

    /**
     * Cập nhật bản ghi đã tồn tại theo ID.
     * @throws jakarta.persistence.EntityNotFoundException nếu không tìm thấy
     */
    DTO update(ID id, DTO dto);

    /**
     * Lưu bất kể bản ghi đã tồn tại hay chưa.
     * Dùng khi không cần phân biệt create/update (upsert).
     */
    DTO save(DTO dto);


    // ══════════════════════════════════════════════════════════
    //  DELETE — Xóa
    // ══════════════════════════════════════════════════════════

    /** Xóa theo ID */
    void deleteById(ID id);

    /** Xóa nhiều bản ghi theo danh sách ID */
    void deleteAllById(Iterable<ID> ids);

    /** Xóa toàn bộ bản ghi trong bảng (cẩn thận!) */
    void deleteAll();


    // ══════════════════════════════════════════════════════════
    //  CHECK / COUNT — Kiểm tra & đếm
    // ══════════════════════════════════════════════════════════

    /** Kiểm tra bản ghi có tồn tại theo ID không */
    boolean existsById(ID id);

    /** Đếm tổng số bản ghi */
    long count();
}
