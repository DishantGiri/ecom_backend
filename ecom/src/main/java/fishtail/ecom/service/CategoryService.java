package fishtail.ecom.service;

import fishtail.ecom.dto.CategoryDTO;
import fishtail.ecom.entity.Category;
import fishtail.ecom.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public CategoryDTO createCategory(String name, MultipartFile image) throws IOException {
        Category category = Category.builder()
                .name(name)
                .build();

        if (image != null && !image.isEmpty()) {
            category.setImageUrl(saveFile(image));
        }

        category = categoryRepository.save(category);
        return toDTO(category);
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, String name, MultipartFile image) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (name != null && !name.isBlank()) {
            category.setName(name);
        }

        if (image != null && !image.isEmpty()) {
            if (category.getImageUrl() != null && !category.getImageUrl().isBlank()) {
                deleteFileIfExists(category.getImageUrl());
            }
            category.setImageUrl(saveFile(image));
        }

        category = categoryRepository.save(category);
        return toDTO(category);
    }

    private void deleteFileIfExists(String filename) {
        if (filename == null || filename.isBlank())
            return;
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Warning: Could not delete file: " + filename + " - " + e.getMessage());
        }
    }

    private CategoryDTO toDTO(Category category) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String fullImageUrl = (category.getImageUrl() != null && !category.getImageUrl().isBlank())
                ? cleanBaseUrl + "/api/images/" + category.getImageUrl()
                : null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(fullImageUrl)
                .build();
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";
        String filename = "cat_" + UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}
