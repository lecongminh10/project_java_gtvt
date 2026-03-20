package com.example.project.service.impl;

import com.example.project.service.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Abstract generic service — implement mặc định toàn bộ BaseService.
 * Subclass chỉ cần:
 *   1. Override getRepository()        → cung cấp Repository tương ứng
 *   2. Override toDTO(entity)          → chuyển Entity thành DTO
 *   3. Override toEntity(dto)          → chuyển DTO thành Entity
 *   4. Override mergeEntity() (tuỳ chọn) → áp dụng thay đổi khi UPDATE
 *   5. Implement thêm nghiệp vụ đặc thù của từng module (nếu có)
 *
 * @param <ENTITY> Entity JPA type
 * @param <DTO>    DTO type
 * @param <ID>     Kiểu khóa chính
 */
public abstract class AbstractBaseService<ENTITY, DTO, ID> implements BaseService<DTO, ID> {

    // ── Subclass phải cung cấp ──────────────────────────────────────────────

    /** Cung cấp JPA Repository tương ứng */
    protected abstract JpaRepository<ENTITY, ID> getRepository();

    /** Chuyển Entity → DTO */
    protected abstract DTO toDTO(ENTITY entity);

    /** Chuyển DTO → Entity (dùng khi CREATE / upsert) */
    protected abstract ENTITY toEntity(DTO dto);

    /**
     * Áp dụng dữ liệu từ DTO lên Entity đã tồn tại (dùng khi UPDATE).
     * Mặc định: dùng toEntity(dto) đơn giản nhất.
     * Override lại nếu cần giữ nguyên một số trường (createdAt, password, ...)
     */
    protected ENTITY mergeEntity(ENTITY existingEntity, DTO dto) {
        return toEntity(dto);
    }

    // ══════════════════════════════════════════════════════════
    //  LIST
    // ══════════════════════════════════════════════════════════

    @Override
    public List<DTO> findAll() {
        return getRepository().findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DTO> findAll(Sort sort) {
        return getRepository().findAll(sort)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DTO> findAll(Pageable pageable) {
        return getRepository().findAll(pageable)
                .map(this::toDTO);
    }

    @Override
    public List<DTO> findAllById(Iterable<ID> ids) {
        return getRepository().findAllById(ids)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════
    //  FIND
    // ══════════════════════════════════════════════════════════

    @Override
    public Optional<DTO> findById(ID id) {
        return getRepository().findById(id)
                .map(this::toDTO);
    }

    @Override
    public DTO getById(ID id) {
        return getRepository().findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy bản ghi với ID: " + id));
    }

    // ══════════════════════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════════════════════

    @Override
    public DTO create(DTO dto) {
        ENTITY entity = toEntity(dto);
        ENTITY saved  = getRepository().save(entity);
        return toDTO(saved);
    }

    @Override
    public List<DTO> createAll(Iterable<DTO> dtos) {
        List<ENTITY> entities = StreamSupport.stream(dtos.spliterator(), false)
                .map(this::toEntity)
                .collect(Collectors.toList());
        return getRepository().saveAll(entities)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════════════════════

    @Override
    public DTO update(ID id, DTO dto) {
        ENTITY existing = getRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy bản ghi với ID: " + id));
        ENTITY merged = mergeEntity(existing, dto);
        ENTITY saved  = getRepository().save(merged);
        return toDTO(saved);
    }

    @Override
    public DTO save(DTO dto) {
        ENTITY entity = toEntity(dto);
        ENTITY saved  = getRepository().save(entity);
        return toDTO(saved);
    }

    // ══════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════

    @Override
    public void deleteById(ID id) {
        if (!getRepository().existsById(id)) {
            throw new EntityNotFoundException(
                    "Không tìm thấy bản ghi với ID: " + id);
        }
        getRepository().deleteById(id);
    }

    @Override
    public void deleteAllById(Iterable<ID> ids) {
        getRepository().deleteAllById(ids);
    }

    @Override
    public void deleteAll() {
        getRepository().deleteAll();
    }

    // ══════════════════════════════════════════════════════════
    //  CHECK / COUNT
    // ══════════════════════════════════════════════════════════

    @Override
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Override
    public long count() {
        return getRepository().count();
    }
}
