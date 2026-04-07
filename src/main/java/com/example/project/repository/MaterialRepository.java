package com.example.project.repository;

import com.example.project.entity.Material;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByTeacher(User teacher);
}