package com.example.project.repository;

import com.example.project.entity.ClassStudent;
import com.example.project.entity.ClassStudentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, ClassStudentId> {
}
