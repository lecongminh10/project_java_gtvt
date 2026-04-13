package com.example.project.repository;

import com.example.project.entity.ClassStatus;
import com.example.project.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select c from Course c where c.deleted = false and (c.name = :name or c.status = :status)")
    List<Course> searchCourses(String name, ClassStatus status);

    List<Course> findAllByDeletedFalse();

    List<Course> findAllByDeletedFalse(Sort sort);

    long countBySubjectIdAndDeletedFalse(Long subjectId);
}
