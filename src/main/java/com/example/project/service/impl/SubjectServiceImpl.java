package com.example.project.service.impl;

import com.example.project.dto.SubjectDTO;
import com.example.project.entity.Subject;
import com.example.project.entity.SubjectLevel;
import com.example.project.repository.CourseRepository;
import com.example.project.repository.DocumentRepository;
import com.example.project.repository.SubjectRepository;
import com.example.project.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectServiceImpl extends AbstractBaseService<Subject, SubjectDTO, Long> implements SubjectService {

    @Autowired
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final DocumentRepository documentRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository,
                              CourseRepository courseRepository,
                              DocumentRepository documentRepository) {
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    protected JpaRepository<Subject, Long> getRepository() {
        return subjectRepository;
    }

    @Override
    protected SubjectDTO toDTO(Subject entity) {
        if (entity == null) return null;
        SubjectDTO dto = new SubjectDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setLevel(entity.getLevel());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    @Override
    protected Subject toEntity(SubjectDTO dto) {
        if (dto == null) return null;
        Subject subject = new Subject();
        subject.setId(dto.getId());
        subject.setCode(dto.getCode());
        subject.setName(dto.getName());
        subject.setLevel(dto.getLevel());
        subject.setDescription(dto.getDescription());
        return subject;
    }

    @Override
    public SubjectDTO create(SubjectDTO subjectDTO) {
        Subject subject = toEntity(subjectDTO);
        return toDTO(subjectRepository.save(subject));
    }

    @Override
    public SubjectDTO update(Long id, SubjectDTO subjectDTO) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));

        existing.setCode(subjectDTO.getCode());
        existing.setName(subjectDTO.getName());
        existing.setLevel(subjectDTO.getLevel());
        existing.setDescription(subjectDTO.getDescription());

        return toDTO(subjectRepository.save(existing));
    }

    @Override
    public List<SubjectDTO> findAll() {
        return subjectRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC,
                "id"))
            .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SubjectDTO> findById(Long id) {
        return subjectRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<SubjectDTO> searchSubject(String name, SubjectLevel level) {
        String keyword = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        return subjectRepository.searchSubjects(keyword, level).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("Subject not found with id: " + id);
        }
        long courseCount = courseRepository.countBySubjectIdAndDeletedFalse(id);
        long documentCount = documentRepository.countBySubject_Id(id);
        if (courseCount > 0 || documentCount > 0) {
            StringBuilder message = new StringBuilder("Không thể xóa môn học vì đang được sử dụng");
            if (courseCount > 0 && documentCount > 0) {
                message.append(" bởi khóa học và tài liệu.");
            } else if (courseCount > 0) {
                message.append(" bởi khóa học.");
            } else {
                message.append(" bởi tài liệu.");
            }
            throw new IllegalStateException(message.toString());
        }
        subjectRepository.deleteById(id);
    }
}