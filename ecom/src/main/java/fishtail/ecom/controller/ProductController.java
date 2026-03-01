package fishtail.ecom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fishtail.ecom.dto.ProductRequestDTO;
import fishtail.ecom.dto.ProductResponseDTO;
import fishtail.ecom.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/api/admin/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "featureImage", required = false) MultipartFile featureImage,
            @RequestPart(value = "galleryImages", required = false) List<MultipartFile> galleryImages,
            @RequestPart(value = "promotionalImages", required = false) List<MultipartFile> promotionalImages,
            // Per-offer images: offerImage_0, offerImage_1, offerImage_2 …
            @RequestPart(value = "offerImage_0", required = false) MultipartFile offerImg0,
            @RequestPart(value = "offerImage_1", required = false) MultipartFile offerImg1,
            @RequestPart(value = "offerImage_2", required = false) MultipartFile offerImg2,
            @RequestPart(value = "offerImage_3", required = false) MultipartFile offerImg3,
            @RequestPart(value = "offerImage_4", required = false) MultipartFile offerImg4,
            @RequestPart(value = "offerImage_5", required = false) MultipartFile offerImg5,
            @RequestPart(value = "offerImage_6", required = false) MultipartFile offerImg6,
            @RequestPart(value = "offerImage_7", required = false) MultipartFile offerImg7,
            @RequestPart(value = "offerImage_8", required = false) MultipartFile offerImg8) throws IOException {
        ProductRequestDTO dto = objectMapper.readValue(dataJson, ProductRequestDTO.class);
        Map<Integer, MultipartFile> offerImages = buildOfferImageMap(
                offerImg0, offerImg1, offerImg2, offerImg3, offerImg4,
                offerImg5, offerImg6, offerImg7, offerImg8);
        return ResponseEntity
                .ok(productService.createProduct(dto, featureImage, galleryImages, promotionalImages, offerImages));
    }

    @GetMapping("/api/admin/products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProductsAdmin() {
        return ResponseEntity.ok(productService.getAllProducts("USD"));
    }

    @GetMapping("/api/admin/products/{id}")
    public ResponseEntity<ProductResponseDTO> getProductByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id, "USD"));
    }

    @PutMapping(value = "/api/admin/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "featureImage", required = false) MultipartFile featureImage,
            @RequestPart(value = "galleryImages", required = false) List<MultipartFile> galleryImages,
            @RequestPart(value = "promotionalImages", required = false) List<MultipartFile> promotionalImages,
            // Per-offer images: upload only those you want to replace
            @RequestPart(value = "offerImage_0", required = false) MultipartFile offerImg0,
            @RequestPart(value = "offerImage_1", required = false) MultipartFile offerImg1,
            @RequestPart(value = "offerImage_2", required = false) MultipartFile offerImg2,
            @RequestPart(value = "offerImage_3", required = false) MultipartFile offerImg3,
            @RequestPart(value = "offerImage_4", required = false) MultipartFile offerImg4,
            @RequestPart(value = "offerImage_5", required = false) MultipartFile offerImg5,
            @RequestPart(value = "offerImage_6", required = false) MultipartFile offerImg6,
            @RequestPart(value = "offerImage_7", required = false) MultipartFile offerImg7,
            @RequestPart(value = "offerImage_8", required = false) MultipartFile offerImg8) throws IOException {
        ProductRequestDTO dto = objectMapper.readValue(dataJson, ProductRequestDTO.class);
        Map<Integer, MultipartFile> offerImages = buildOfferImageMap(
                offerImg0, offerImg1, offerImg2, offerImg3, offerImg4,
                offerImg5, offerImg6, offerImg7, offerImg8);
        return ResponseEntity
                .ok(productService.updateProduct(id, dto, featureImage, galleryImages, promotionalImages, offerImages));
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @DeleteMapping("/api/admin/products/{id}/gallery/{filename}")
    public ResponseEntity<ProductResponseDTO> removeGalleryImage(
            @PathVariable Long id,
            @PathVariable String filename) {
        return ResponseEntity.ok(productService.removeGalleryImage(id, filename));
    }

    @DeleteMapping("/api/admin/products/{id}/promotional/{filename}")
    public ResponseEntity<ProductResponseDTO> removePromotionalImage(
            @PathVariable Long id,
            @PathVariable String filename) {
        return ResponseEntity.ok(productService.removePromotionalImage(id, filename));
    }

    @GetMapping("/api/products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "USD", required = false) String currency) {
        return ResponseEntity.ok(productService.getAllProducts(currency));
    }

    /**
     * GET /api/products/popular
     * Returns products sorted by highest total clicks globally.
     */
    @GetMapping("/api/products/popular")
    public ResponseEntity<List<ProductResponseDTO>> getPopularProducts(
            @RequestParam(defaultValue = "USD", required = false) String currency) {
        return ResponseEntity.ok(productService.getPopularProducts(currency));
    }

    /**
     * GET /api/products/{id}
     */
    @GetMapping("/api/products/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "USD", required = false) String currency) {
        return ResponseEntity.ok(productService.getProductById(id, currency));
    }

    /**
     * GET /api/products/search?keyword=xyz
     */
    @GetMapping("/api/products/search")
    public ResponseEntity<List<ProductResponseDTO>> searchByTitle(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "USD", required = false) String currency) {
        return ResponseEntity.ok(productService.searchByTitle(keyword, currency));
    }

    /**
     * GET /api/products/category?name=supplements
     */
    @GetMapping("/api/products/category")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(
            @RequestParam String name,
            @RequestParam(defaultValue = "USD", required = false) String currency) {
        return ResponseEntity.ok(productService.getByCategory(name, currency));
    }

    private Map<Integer, MultipartFile> buildOfferImageMap(MultipartFile... files) {
        Map<Integer, MultipartFile> map = new HashMap<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null && !files[i].isEmpty()) {
                map.put(i, files[i]);
            }
        }
        return map;
    }
}
