package fishtail.ecom.service;

import fishtail.ecom.dto.BlogRequestDTO;
import fishtail.ecom.dto.BlogResponseDTO;
import fishtail.ecom.entity.Blog;
import fishtail.ecom.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public BlogResponseDTO createBlog(BlogRequestDTO dto, MultipartFile featureImage) throws IOException {
        String slug = generateSlug(dto.getTitle());

        Blog blog = Blog.builder()
                .title(dto.getTitle())
                .slug(slug)
                .intro(dto.getIntro())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .metaTitle(dto.getMetaTitle() != null && !dto.getMetaTitle().isBlank() ? dto.getMetaTitle()
                        : dto.getTitle())
                .metaDescription(
                        dto.getMetaDescription() != null ? dto.getMetaDescription() : generateExcerpt(dto.getContent()))
                .metaKeywords(dto.getMetaKeywords())
                .build();

        if (featureImage != null && !featureImage.isEmpty()) {
            blog.setFeatureImage(saveFile(featureImage));
        }

        blog = blogRepository.save(blog);
        return toResponseDTO(blog);
    }

    @Transactional
    public BlogResponseDTO updateBlog(Long id, BlogRequestDTO dto, MultipartFile featureImage) throws IOException {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getTitle().equals(dto.getTitle())) {
            blog.setTitle(dto.getTitle());
            blog.setSlug(generateSlug(dto.getTitle())); // Regenerate slug if title changes
        }

        blog.setIntro(dto.getIntro());
        blog.setContent(dto.getContent());
        blog.setAuthor(dto.getAuthor());

        if (dto.getMetaTitle() != null && !dto.getMetaTitle().isBlank()) {
            blog.setMetaTitle(dto.getMetaTitle());
        } else {
            blog.setMetaTitle(blog.getTitle());
        }

        if (dto.getMetaDescription() != null && !dto.getMetaDescription().isBlank()) {
            blog.setMetaDescription(dto.getMetaDescription());
        } else {
            blog.setMetaDescription(generateExcerpt(dto.getContent()));
        }

        if (dto.getMetaKeywords() != null) {
            blog.setMetaKeywords(dto.getMetaKeywords());
        }

        if (featureImage != null && !featureImage.isEmpty()) {
            deleteFileIfExists(blog.getFeatureImage());
            blog.setFeatureImage(saveFile(featureImage));
        }

        blog = blogRepository.save(blog);
        return toResponseDTO(blog);
    }

    public List<BlogResponseDTO> getAllBlogs() {
        return blogRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BlogResponseDTO getBlogBySlug(String slug) {
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Blog not found by slug: " + slug));
        return toResponseDTO(blog);
    }

    public BlogResponseDTO getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found by id: " + id));
        return toResponseDTO(blog);
    }

    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        deleteFileIfExists(blog.getFeatureImage());
        blogRepository.delete(blog);
    }

    // --- Helpers ---

    private String generateSlug(String title) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove invalid characters entirely
                .trim()
                .replaceAll("\\s+", "-"); // Replace spaces with hyphens

        String slug = baseSlug;
        int counter = 1;
        while (blogRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private String generateExcerpt(String content) {
        if (content == null)
            return "";
        // Extremely crude HTML tag stripper to pull pure text for SEO
        String plain = content.replaceAll("<[^>]*>", "");
        if (plain.length() > 155) {
            return plain.substring(0, 155).trim() + "...";
        }
        return plain.trim();
    }

    private BlogResponseDTO toResponseDTO(Blog blog) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String featureUrl = (blog.getFeatureImage() != null && !blog.getFeatureImage().isBlank())
                ? cleanBaseUrl + "/api/images/" + blog.getFeatureImage()
                : null;

        return BlogResponseDTO.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .intro(blog.getIntro())
                .content(blog.getContent())
                .featureImageUrl(featureUrl)
                .author(blog.getAuthor())
                .metaTitle(blog.getMetaTitle())
                .metaDescription(blog.getMetaDescription())
                .metaKeywords(blog.getMetaKeywords())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.lastIndexOf('.') > 0) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return newFilename;
    }

    private void deleteFileIfExists(String filename) {
        if (filename == null || filename.isBlank())
            return;
        Path filePath = Paths.get(uploadDir).resolve(filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
