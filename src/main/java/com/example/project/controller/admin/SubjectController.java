package com.example.project.controller.admin;

import com.example.project.dto.SubjectDTO;
import com.example.project.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // Rest API
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDTO>> getSubjects() {
        return new ResponseEntity<>(subjectService.findAll(), HttpStatus.OK);
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectDTO> createSubject(@RequestBody SubjectDTO subjectDTO) {
        return new ResponseEntity<>(subjectService.create(subjectDTO), HttpStatus.CREATED);
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<SubjectDTO> updateSubject(@PathVariable Long id, @RequestBody SubjectDTO subjectDTO) {
        return new ResponseEntity<>(subjectService.update(id, subjectDTO), HttpStatus.OK);
    }
}
