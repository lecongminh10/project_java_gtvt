package com.example.project.service.impl;

import com.example.project.dto.CourseDTO;
import com.example.project.entity.ClassStatus;
import com.example.project.entity.Course;
import com.example.project.repository.CourseRepository;
import com.example.project.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Transactional
public class CourseServiceImpl extends AbstractBaseService<Course, CourseDTO, Long> implements CourseService {

    private final CourseRepository courseRepository;
    private final com.example.project.repository.SubjectRepository subjectRepository;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository, com.example.project.repository.SubjectRepository subjectRepository) {
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    protected JpaRepository<Course, Long> getRepository() {
        return courseRepository;
    }

    @Override
    protected CourseDTO toDTO(Course entity) {
        if (entity == null) return null;
        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setSubjectId(entity.getSubjectId());
        if (entity.getSubjectId() != null) {
            subjectRepository.findById(entity.getSubjectId())
                    .ifPresent(s -> dto.setSubjectName(s.getName()));
        }
        dto.setSessionsCount(entity.getSessionsCount());
        dto.setFee(entity.getFee());
        dto.setDurationWeeks(entity.getDurationWeeks());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    protected Course toEntity(CourseDTO dto) {
        if (dto == null) return null;
        Course course = new Course();
        course.setId(dto.getId());
        course.setCode(dto.getCode());
        course.setName(dto.getName());
        course.setSubjectId(dto.getSubjectId());
        course.setSessionsCount(dto.getSessionsCount());
        course.setFee(dto.getFee());
        course.setDurationWeeks(dto.getDurationWeeks());
        course.setDescription(dto.getDescription());
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : ClassStatus.MOI);

        if (dto.getId() == null) {
            course.setCreatedAt(LocalDateTime.now());
        } else {
            course.setCreatedAt(dto.getCreatedAt());
        }
        course.setUpdatedAt(LocalDateTime.now());
        return course;
    }

    @Override
    public CourseDTO create(CourseDTO courseDTO) {
        Course course = toEntity(courseDTO);
        if (course.getStatus() == null) {
            course.setStatus(ClassStatus.MOI);
        }
        if (course.getCreatedAt() == null) {
            course.setCreatedAt(LocalDateTime.now());
        }
        course.setUpdatedAt(LocalDateTime.now());
        return toDTO(courseRepository.save(course));
    }

    @Override
    public CourseDTO update(Long id, CourseDTO courseDTO) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        existing.setName(courseDTO.getName());
        existing.setSubjectId(courseDTO.getSubjectId());
        existing.setSessionsCount(courseDTO.getSessionsCount());
        existing.setFee(courseDTO.getFee());
        existing.setDurationWeeks(courseDTO.getDurationWeeks());
        existing.setDescription(courseDTO.getDescription());
        if (courseDTO.getStatus() != null) {
            existing.setStatus(courseDTO.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return toDTO(courseRepository.save(existing));
    }

    @Override
    public CourseDTO save(CourseDTO dto) {
        return null;
    }

    @Override
    public List<CourseDTO> findAll() {
        return courseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CourseDTO> findById(Long id) {
        return courseRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<CourseDTO> searchCourses(String name, ClassStatus status) {
        return courseRepository.searchCourses(name, status).stream().filter(s -> s.getName().equalsIgnoreCase(name) || s.getStatus() == status).map(this::toDTO).collect(Collectors.toList());

    }

}
