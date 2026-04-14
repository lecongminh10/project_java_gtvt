package com.example.project.service;

import com.example.project.dto.CourseDTO;
import com.example.project.entity.ClassStatus;

import java.util.List;
import java.util.Optional;

public interface CourseService extends BaseService<CourseDTO, Long> {
    CourseDTO create(CourseDTO courseDTO);

    CourseDTO update(Long id, CourseDTO courseDTO);

    void markDeleted(Long id);

    Optional<CourseDTO> findById(Long id);

    List<CourseDTO> searchCourses(String courseName, ClassStatus status);
}
