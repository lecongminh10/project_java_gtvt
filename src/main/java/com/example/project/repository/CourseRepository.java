package com.example.project.repository;

import com.example.project.entity.ClassStatus;
import com.example.project.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select c from Course c where c.name = :name or c.status = :status")
    List<Course> searchCourses(String name, ClassStatus status);
}
