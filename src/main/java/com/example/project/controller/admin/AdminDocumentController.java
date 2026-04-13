package com.example.project.controller.admin;

import com.example.project.entity.Course;
import com.example.project.entity.Document;
import com.example.project.entity.DocumentType;
import com.example.project.entity.Subject;
import com.example.project.entity.User;
import com.example.project.repository.CourseRepository;
import com.example.project.repository.DocumentRepository;
import com.example.project.repository.SubjectRepository;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/documents")
public class AdminDocumentController {

    private final DocumentRepository documentRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Value("${document.storage.base-path:uploads}")
    private String basePath;

    public AdminDocumentController(DocumentRepository documentRepository,
                                   SubjectRepository subjectRepository,
                                   CourseRepository courseRepository,
                                   UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listDocuments(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "subjectId", required = false) Long subjectId,
                                @RequestParam(value = "courseId", required = false) Long courseId,
                                Model model) {
        String keywordValue = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<Document> documents = documentRepository.search(keywordValue, subjectId, courseId);

        model.addAttribute("pageTitle", "Quản lý tài liệu");
        model.addAttribute("documents", documents);
        model.addAttribute("keyword", keyword);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse());
        return "admin/documents/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("document", new Document());
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse());
        model.addAttribute("types", DocumentType.values());
        model.addAttribute("formAction", "/admin/documents");
        model.addAttribute("pageTitle", "Thêm tài liệu");
        return "admin/documents/form";
    }

    @PostMapping
    public String createDocument(
            Model model,
            @RequestParam String name,
            @RequestParam DocumentType type,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long courseId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        ValidationResult result = buildDocumentFromForm(new Document(), name, type, subjectId, courseId, file, true);
        if (result.hasErrors()) {
            populateFormModel(model, result.document, "/admin/documents", "Thêm tài liệu", result.errors);
            return "admin/documents/form";
        }

        try {
            String savedFilePath = saveFile(file);
            result.document.setFilePath(savedFilePath);
        } catch (IOException e) {
            result.errors.put("file", "Lỗi lưu file: " + e.getMessage());
            populateFormModel(model, result.document, "/admin/documents", "Thêm tài liệu", result.errors);
            return "admin/documents/form";
        }

        User uploader = getCurrentUser(authentication);
        result.document.setUploadBy(uploader);
        result.document.setCreatedAt(LocalDateTime.now());
        result.document.setUpdatedAt(LocalDateTime.now());

        documentRepository.save(result.document);
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã thêm tài liệu thành công.");
        return "redirect:/admin/documents";
    }

    @GetMapping("/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentRepository.findById(id).orElseThrow();
        model.addAttribute("document", document);
        model.addAttribute("pageTitle", "Chi tiết tài liệu");
        return "admin/documents/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Document document = documentRepository.findById(id).orElseThrow();
        model.addAttribute("document", document);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse());
        model.addAttribute("types", DocumentType.values());
        model.addAttribute("formAction", "/admin/documents/" + id);
        model.addAttribute("pageTitle", "Cập nhật tài liệu");
        return "admin/documents/form";
    }

    @PostMapping("/{id}")
    public String updateDocument(
            @PathVariable Long id,
            Model model,
            @RequestParam String name,
            @RequestParam DocumentType type,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        Document document = documentRepository.findById(id).orElseThrow();
        boolean mustHaveFile = (document.getFilePath() == null || document.getFilePath().isBlank());
        ValidationResult result = buildDocumentFromForm(document, name, type, subjectId, courseId, file, mustHaveFile);

        if (result.hasErrors()) {
            populateFormModel(model, result.document, "/admin/documents/" + id, "Cập nhật tài liệu", result.errors);
            return "admin/documents/form";
        }

        if (file != null && !file.isEmpty()) {
            try {
                // Remove old file
                if (document.getFilePath() != null) {
                    deleteFile(document.getFilePath());
                }
                String savedFilePath = saveFile(file);
                result.document.setFilePath(savedFilePath);
            } catch (IOException e) {
                result.errors.put("file", "Lỗi lưu file mới: " + e.getMessage());
                populateFormModel(model, result.document, "/admin/documents/" + id, "Cập nhật tài liệu", result.errors);
                return "admin/documents/form";
            }
        }

        if (result.document.getCreatedAt() == null) {
            result.document.setCreatedAt(LocalDateTime.now());
        }
        result.document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(result.document);
        redirectAttributes.addFlashAttribute("toastSuccess", "Đã cập nhật tài liệu thành công.");
        return "redirect:/admin/documents";
    }

    @PostMapping("/{id}/delete")
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isPresent()) {
            Document document = docOpt.get();
            if (document.getFilePath() != null) {
                deleteFile(document.getFilePath());
            }
            documentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("toastSuccess", "Đã xóa tài liệu thành công.");
        }
        return "redirect:/admin/documents";
    }
    
    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewInline(@PathVariable("id") Long id) throws IOException {
        return buildFileResponse(id, false);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws IOException {
        return buildFileResponse(id, true);
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

    private ValidationResult buildDocumentFromForm(
            Document document,
            String name,
            DocumentType type,
            Long subjectId,
            Long courseId,
            MultipartFile file,
            boolean requireFile
    ) {
        Map<String, String> errors = new HashMap<>();

        String normalizedName = name != null ? name.trim() : "";
        if (normalizedName.isEmpty()) {
            errors.put("name", "Tên tài liệu bắt buộc nhập.");
        }

        Subject subject = null;
        if (subjectId != null) {
            subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject == null) {
                errors.put("subjectId", "Môn học không tồn tại.");
            }
        }

        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId).orElse(null);
            if (course == null || course.isDeleted()) {
                errors.put("courseId", "Khóa học không tồn tại.");
            }
        }

        if (requireFile && (file == null || file.isEmpty())) {
            errors.put("file", "Vui lòng chọn file tài liệu.");
        }

        document.setName(normalizedName);
        document.setType(type);
        document.setSubject(subject);
        document.setCourse(course);

        return new ValidationResult(document, errors);
    }

    private void populateFormModel(Model model, Document document, String formAction, String pageTitle, Map<String, String> errors) {
        model.addAttribute("document", document);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("courses", courseRepository.findAllByDeletedFalse());
        model.addAttribute("types", DocumentType.values());
        model.addAttribute("formAction", formAction);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("errors", errors);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        Path uploadPath = resolvePath("");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    private void deleteFile(String filename) {
        Path filePath = resolvePath(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + filename);
        }
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

    private User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        return null;
    }

    private static class ValidationResult {
        private final Document document;
        private final Map<String, String> errors;

        private ValidationResult(Document document, Map<String, String> errors) {
            this.document = document;
            this.errors = errors;
        }

        private boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }
}
