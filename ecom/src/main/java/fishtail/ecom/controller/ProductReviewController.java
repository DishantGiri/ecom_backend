package fishtail.ecom.controller;

import fishtail.ecom.dto.ProductReviewDTO;
import fishtail.ecom.entity.Product;
import fishtail.ecom.entity.ProductReview;
import fishtail.ecom.repository.ProductRepository;
import fishtail.ecom.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping(value = "/{productId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ProductReviewDTO> addReview(
            @PathVariable Long productId,
            @RequestParam("reviewerName") String reviewerName,
            @RequestParam("reviewText") String reviewText,
            @RequestParam(value = "starRating", required = false) Double starRating,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductReview review = ProductReview.builder()
                .reviewerName(reviewerName)
                .reviewText(reviewText)
                .starRating(starRating)
                .product(product)
                .build();

        if (image != null && !image.isEmpty()) {
            review.setImageUrl(saveFile(image));
        }

        review = reviewRepository.save(review);
        return ResponseEntity.ok(toDTO(review));
    }

    @PutMapping(value = "/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ProductReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @RequestParam("reviewerName") String reviewerName,
            @RequestParam("reviewText") String reviewText,
            @RequestParam(value = "starRating", required = false) Double starRating,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setReviewerName(reviewerName);
        review.setReviewText(reviewText);
        review.setStarRating(starRating);

        if (image != null && !image.isEmpty()) {
            // Delete old image if exists
            if (review.getImageUrl() != null && !review.getImageUrl().isBlank()) {
                try {
                    Path oldPath = Paths.get(uploadDir).resolve(review.getImageUrl());
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    System.err.println("Warning: Could not delete old review image: " + e.getMessage());
                }
            }
            // Save new image
            review.setImageUrl(saveFile(image));
        }

        review = reviewRepository.save(review);
        return ResponseEntity.ok(toDTO(review));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Transactional
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (review.getImageUrl() != null && !review.getImageUrl().isBlank()) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(review.getImageUrl());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Could not delete review image: " + review.getImageUrl());
            }
        }

        reviewRepository.delete(review);
        return ResponseEntity.ok("Review deleted successfully");
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
        String filename = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    private ProductReviewDTO toDTO(ProductReview r) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return ProductReviewDTO.builder()
                .id(r.getId())
                .reviewerName(r.getReviewerName())
                .reviewText(r.getReviewText())
                .starRating(r.getStarRating())
                .imageUrl(r.getImageUrl() != null && !r.getImageUrl().isBlank()
                        ? cleanBaseUrl + "/api/images/" + r.getImageUrl()
                        : null)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
