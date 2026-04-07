package com.example.project.service;

import com.example.project.dto.SubjectDTO;

import java.util.List;
import java.util.Optional;

public interface SubjectService {
    SubjectDTO create(SubjectDTO subject);

    SubjectDTO update(Long id, SubjectDTO subject);

    List<SubjectDTO> findAll();

    Optional<SubjectDTO> findById(Long id);
}
