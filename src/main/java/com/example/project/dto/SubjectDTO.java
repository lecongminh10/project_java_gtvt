package com.example.project.dto;

import com.example.project.entity.SubjectLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectDTO {
    private Long id;
    private String code;
    private String name;
    private SubjectLevel level;
    private String description;
}
