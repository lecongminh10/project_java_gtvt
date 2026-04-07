package com.example.project.service;

import com.example.project.entity.Material;
import com.example.project.entity.User;
import com.example.project.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    public List<Material> getByTeacher(User teacher) {
        return materialRepository.findByTeacher(teacher);
    }

    public Material save(Material m) {
        return materialRepository.save(m);
    }

    public void delete(Long id) {
        materialRepository.deleteById(id);
    }

    public Material findById(Long id) {
        return materialRepository.findById(id).orElse(null);
    }
}