package com.example.project.controller.teacher;

import com.example.project.entity.Document;
import com.example.project.repository.CourseRepository;
import com.example.project.repository.DocumentRepository;
import com.example.project.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher/documents")
public class TeacherDocumentController {

    private final DocumentRepository documentRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    @Value("${document.storage.base-path:uploads}")
    private String basePath;

    public TeacherDocumentController(DocumentRepository documentRepository,
                                     SubjectRepository subjectRepository,
                                     CourseRepository courseRepository) {
        this.documentRepository = documentRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public String listDocuments(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "subjectId", required = false) Long subjectId,
                                @RequestParam(value = "courseId", required = false) Long courseId,
                                Model model) {
        String keywordValue = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<Document> documents = documentRepository.search(keywordValue, subjectId, courseId);

        model.addAttribute("pageTitle", "Tài liệu đào tạo");
        model.addAttribute("documents", documents);
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse());
        return "teacher/documents/list";
    }

    @GetMapping("/{id}")
    public String viewDocument(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("docError", "Không tìm thấy tài liệu.");
            return "redirect:/teacher/documents";
        }

        model.addAttribute("pageTitle", "Chi tiết tài liệu");
        model.addAttribute("document", docOpt.get());
        return "teacher/documents/detail";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws IOException {
        return buildFileResponse(id, true);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewInline(@PathVariable("id") Long id) throws IOException {
        return buildFileResponse(id, false);
    }

    private ResponseEntity<Resource> buildFileResponse(Long id, boolean attachment) throws IOException {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document document = docOpt.get();
        if (document.getFilePath() == null || document.getFilePath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = resolvePath(document.getFilePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        String filename = filePath.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, (attachment ? "attachment" : "inline") + "; filename=\"" + filename + "\"")
                .body(resource);
    }

    private Path resolvePath(String storedPath) {
        Path path = Paths.get(storedPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        String normalizedBase = basePath == null ? "" : basePath.replace("\\", "/");
        String normalizedStored = storedPath.replace("\\", "/");
        if (!normalizedBase.isBlank() && normalizedStored.startsWith(normalizedBase + "/")) {
            return Paths.get(storedPath).normalize();
        }
        return Paths.get(basePath).resolve(storedPath).normalize();
    }
}
