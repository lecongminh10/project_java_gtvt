package com.example.project.service;

import com.example.project.dto.SubjectDTO;

import java.util.List;
import java.util.Optional;


public interface SubjectService extends BaseService<SubjectDTO, Long> {

    SubjectDTO create(SubjectDTO subjectDTO);

    SubjectDTO update(Long id, SubjectDTO subjectDTO);

    List<SubjectDTO> findAll();

    Optional<SubjectDTO> findById(Long id);
}
