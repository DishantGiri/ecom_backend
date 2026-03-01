package fishtail.ecom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fishtail.ecom.dto.BlogRequestDTO;
import fishtail.ecom.dto.BlogResponseDTO;
import fishtail.ecom.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final ObjectMapper objectMapper;

    // --- ADMIN ENDPOINTS (Secured) ---

    @PostMapping(value = "/api/admin/blogs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponseDTO> createBlogAdmin(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "featureImage", required = false) MultipartFile featureImage) throws IOException {
        BlogRequestDTO dto = objectMapper.readValue(dataJson, BlogRequestDTO.class);
        return ResponseEntity.ok(blogService.createBlog(dto, featureImage));
    }

    @PutMapping(value = "/api/admin/blogs/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogResponseDTO> updateBlogAdmin(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "featureImage", required = false) MultipartFile featureImage) throws IOException {
        BlogRequestDTO dto = objectMapper.readValue(dataJson, BlogRequestDTO.class);
        return ResponseEntity.ok(blogService.updateBlog(id, dto, featureImage));
    }

    @DeleteMapping("/api/admin/blogs/{id}")
    public ResponseEntity<String> deleteBlogAdmin(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok("Blog deleted successfully");
    }

    @GetMapping("/api/admin/blogs")
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogsAdmin() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    @GetMapping("/api/admin/blogs/{id}")
    public ResponseEntity<BlogResponseDTO> getBlogByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    // --- PUBLIC ENDPOINTS (No Auth Required) ---

    @GetMapping("/api/blogs")
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogsPublic() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    @GetMapping("/api/blogs/{slug}")
    public ResponseEntity<BlogResponseDTO> getBlogBySlugPublic(@PathVariable String slug) {
        return ResponseEntity.ok(blogService.getBlogBySlug(slug));
    }
}
