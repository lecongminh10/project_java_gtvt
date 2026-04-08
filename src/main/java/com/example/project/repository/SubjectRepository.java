package com.example.project.repository;

import com.example.project.entity.Subject;
import com.example.project.entity.SubjectLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("select s from Subject s where s.name = :name or s.level = :level")
    List<Subject> searchSubjects(String name, SubjectLevel level);
}
