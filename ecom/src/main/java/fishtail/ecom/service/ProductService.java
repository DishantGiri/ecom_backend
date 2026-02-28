package fishtail.ecom.service;

import fishtail.ecom.dto.ProductOfferDTO;
import fishtail.ecom.dto.ProductRequestDTO;
import fishtail.ecom.dto.ProductResponseDTO;
import fishtail.ecom.entity.Product;
import fishtail.ecom.entity.ProductOffer;
import fishtail.ecom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CurrencyService currencyService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ──────────────────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * @param offerImages Map of (offer list-index → image file).
     *                    Key 0 = first offer in dto.getOffers(), key 1 = second,
     *                    etc.
     *                    Pass an empty map or null if no per-offer images are
     *                    uploaded.
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto,
            MultipartFile featureImage,
            List<MultipartFile> galleryImages,
            Map<Integer, MultipartFile> offerImages) throws IOException {

        validateStarRating(dto.getStarRating());

        Product product = Product.builder()
                .title(dto.getTitle())
                .numberOfReviews(dto.getNumberOfReviews())
                .starRating(dto.getStarRating())
                .originalPrice(dto.getOriginalPrice())
                .discountedPrice(dto.getDiscountedPrice())
                .productLink(dto.getProductLink())
                .category(dto.getCategory())
                .galleryImages(new ArrayList<>())
                .offers(new ArrayList<>())
                .build();

        // Save product-level feature image
        if (featureImage != null && !featureImage.isEmpty()) {
            product.setFeatureImage(saveFile(featureImage));
        }

        // Save gallery images
        if (galleryImages != null) {
            for (MultipartFile file : galleryImages) {
                if (file != null && !file.isEmpty()) {
                    product.getGalleryImages().add(saveFile(file));
                }
            }
        }

        // Attach offers (with optional per-offer feature images)
        if (dto.getOffers() != null) {
            for (int i = 0; i < dto.getOffers().size(); i++) {
                ProductOfferDTO offerDTO = dto.getOffers().get(i);
                String offerImageFilename = null;
                if (offerImages != null) {
                    MultipartFile offerImg = offerImages.get(i);
                    if (offerImg != null && !offerImg.isEmpty()) {
                        offerImageFilename = saveFile(offerImg);
                    }
                }
                product.getOffers().add(buildOffer(offerDTO, product, offerImageFilename));
            }
        }

        product = productRepository.save(product);
        // We pass "USD" natively when returning from Admin update actions,
        // as admins are managing USD prices.
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // READ ALL & POPULAR
    // ──────────────────────────────────────────────────────────────────────────

    public List<ProductResponseDTO> getAllProducts(String currency) {
        return productRepository.findAll()
                .stream()
                .map(p -> toResponseDTO(p, false, currency))
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getPopularProducts(String currency) {
        return productRepository.findAllOrderByTotalClicksDesc()
                .stream()
                .map(p -> toResponseDTO(p, false, currency))
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // READ ONE
    // ──────────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────────
    // READ ONE
    // ──────────────────────────────────────────────────────────────────────────

    public ProductResponseDTO getProductById(Long id, String currency) {
        Product product = findOrThrow(id);

        java.math.BigDecimal minPrice = product.getDiscountedPrice() != null
                ? product.getDiscountedPrice().multiply(java.math.BigDecimal.valueOf(0.5))
                : java.math.BigDecimal.ZERO;
        java.math.BigDecimal maxPrice = product.getDiscountedPrice() != null
                ? product.getDiscountedPrice().multiply(java.math.BigDecimal.valueOf(1.5))
                : java.math.BigDecimal.valueOf(999999);

        List<Product> similarProducts = productRepository.findSimilarProducts(
                product.getCategory(),
                product.getId(),
                minPrice,
                maxPrice);

        // Map main product WITH similar products list (includeSimilar=true)
        ProductResponseDTO response = toResponseDTO(product, true, currency);

        // Map similar products WITHOUT nesting more similar ones
        List<ProductResponseDTO> similarDTOs = similarProducts.stream()
                .limit(4)
                .map(p -> toResponseDTO(p, false, currency))
                .collect(Collectors.toList());

        response.setSimilarProducts(similarDTOs);
        return response;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * @param offerImages Map of (offer list-index → image file).
     *                    Provide only the offers whose image you want to replace.
     *                    Existing offer images not present in the map are kept
     *                    as-is.
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long id,
            ProductRequestDTO dto,
            MultipartFile featureImage,
            List<MultipartFile> galleryImages,
            Map<Integer, MultipartFile> offerImages) throws IOException {

        validateStarRating(dto.getStarRating());
        Product product = findOrThrow(id);

        product.setTitle(dto.getTitle());
        product.setNumberOfReviews(dto.getNumberOfReviews());
        product.setStarRating(dto.getStarRating());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setDiscountedPrice(dto.getDiscountedPrice());
        product.setProductLink(dto.getProductLink());
        product.setCategory(dto.getCategory());

        // Replace product-level feature image if a new one is provided
        if (featureImage != null && !featureImage.isEmpty()) {
            deleteFileIfExists(product.getFeatureImage());
            product.setFeatureImage(saveFile(featureImage));
        }

        // Append new gallery images (keep existing ones)
        if (galleryImages != null) {
            for (MultipartFile file : galleryImages) {
                if (file != null && !file.isEmpty()) {
                    product.getGalleryImages().add(saveFile(file));
                }
            }
        }

        // Replace offers completely (delete old per-offer images)
        product.getOffers().forEach(o -> deleteFileIfExists(o.getFeatureImage()));
        product.getOffers().clear();
        if (dto.getOffers() != null) {
            for (int i = 0; i < dto.getOffers().size(); i++) {
                ProductOfferDTO offerDTO = dto.getOffers().get(i);
                String offerImageFilename = null;
                if (offerImages != null) {
                    MultipartFile offerImg = offerImages.get(i);
                    if (offerImg != null && !offerImg.isEmpty()) {
                        offerImageFilename = saveFile(offerImg);
                    }
                }
                product.getOffers().add(buildOffer(offerDTO, product, offerImageFilename));
            }
        }

        product = productRepository.save(product);
        // We pass "USD" natively when returning from Admin update actions,
        // as admins are managing USD prices.
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findOrThrow(id);
        // Delete product-level feature image
        deleteFileIfExists(product.getFeatureImage());
        // Delete gallery images
        product.getGalleryImages().forEach(this::deleteFileIfExists);
        // Delete per-offer feature images
        product.getOffers().forEach(o -> deleteFileIfExists(o.getFeatureImage()));
        productRepository.delete(product);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // REMOVE SINGLE GALLERY IMAGE
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public ProductResponseDTO removeGalleryImage(Long productId, String filename) {
        Product product = findOrThrow(productId);
        boolean removed = product.getGalleryImages().remove(filename);
        if (removed) {
            deleteFileIfExists(filename);
        }
        product = productRepository.save(product);
        // We pass "USD" natively when returning from Admin update actions,
        // as admins are managing USD prices.
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SEARCH
    // ──────────────────────────────────────────────────────────────────────────

    public List<ProductResponseDTO> searchByTitle(String keyword, String currency) {
        return productRepository.findByTitleFuzzy(keyword)
                .stream()
                .map(p -> toResponseDTO(p, false, currency))
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getByCategory(String category, String currency) {
        return productRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(p -> toResponseDTO(p, false, currency))
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────────────────

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    private void validateStarRating(Double rating) {
        if (rating == null)
            return;
        if (rating < 0.5 || rating > 5.0) {
            throw new IllegalArgumentException("Star rating must be between 0.5 and 5.0");
        }
        // Ensure it's a multiple of 0.5
        if ((rating * 10) % 5 != 0) {
            throw new IllegalArgumentException("Star rating must be in increments of 0.5 (e.g., 0.5, 1.0, 1.5 … 5.0)");
        }
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

    private void deleteFileIfExists(String filename) {
        if (filename == null || filename.isBlank())
            return;
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't rethrow – deletion failure shouldn't break the flow
            System.err.println("Warning: Could not delete file: " + filename + " — " + e.getMessage());
        }
    }

    private ProductOffer buildOffer(ProductOfferDTO dto, Product product, String offerImageFilename) {
        return ProductOffer.builder()
                .label(dto.getLabel())
                .quantity(dto.getQuantity())
                .originalPrice(dto.getOriginalPrice())
                .discountedPrice(dto.getDiscountedPrice())
                .displayOrder(dto.getDisplayOrder())
                .featureImage(offerImageFilename)
                .product(product)
                .build();
    }

    private ProductOfferDTO toOfferDTO(ProductOffer offer, String currency) {
        String offerImageUrl = (offer.getFeatureImage() != null && !offer.getFeatureImage().isBlank())
                ? baseUrl + "/api/images/" + offer.getFeatureImage()
                : null;

        java.math.BigDecimal originalConverted = currencyService.convertFromUsd(offer.getOriginalPrice(), currency);
        java.math.BigDecimal discountedConverted = currencyService.convertFromUsd(offer.getDiscountedPrice(), currency);

        return ProductOfferDTO.builder()
                .id(offer.getId())
                .label(offer.getLabel())
                .quantity(offer.getQuantity())
                .originalPrice(originalConverted)
                .discountedPrice(discountedConverted)
                .displayOrder(offer.getDisplayOrder())
                .featureImageUrl(offerImageUrl)
                .build();
    }

    private ProductResponseDTO toResponseDTO(Product product, boolean includeSimilar, String requestCurrency) {
        // Resolve target currency string, default to USD
        String currency = (requestCurrency == null || requestCurrency.isBlank()) ? "USD"
                : requestCurrency.toUpperCase();

        // Sort offers by displayOrder
        List<ProductOffer> sortedOffers = product.getOffers() == null ? Collections.emptyList()
                : product.getOffers().stream()
                        .sorted(Comparator.comparingInt(o -> (o.getDisplayOrder() == null ? 99 : o.getDisplayOrder())))
                        .collect(Collectors.toList());

        List<ProductOfferDTO> offerDTOs = sortedOffers.stream()
                .map(o -> toOfferDTO(o, currency))
                .collect(Collectors.toList());

        String featureUrl = (product.getFeatureImage() != null && !product.getFeatureImage().isBlank())
                ? baseUrl + "/api/images/" + product.getFeatureImage()
                : null;

        List<String> galleryUrls = new ArrayList<>();
        if (product.getGalleryImages() != null) {
            product.getGalleryImages().stream()
                    .map(f -> baseUrl + "/api/images/" + f)
                    .forEach(galleryUrls::add);
        }

        List<fishtail.ecom.dto.ProductClickStatDTO> stats = product.getClickStats() == null ? Collections.emptyList()
                : product.getClickStats().stream()
                        .map(s -> fishtail.ecom.dto.ProductClickStatDTO.builder()
                                .country(s.getCountry())
                                .clickCount(s.getClickCount())
                                .build())
                        .collect(Collectors.toList());

        return ProductResponseDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .numberOfReviews(product.getNumberOfReviews())
                .starRating(product.getStarRating())
                .originalPrice(currencyService.convertFromUsd(product.getOriginalPrice(), currency))
                .discountedPrice(currencyService.convertFromUsd(product.getDiscountedPrice(), currency))
                .currency(currency)
                .featureImageUrl(featureUrl)
                .galleryImageUrls(galleryUrls)
                .productLink(product.getProductLink())
                .category(product.getCategory())
                .offers(offerDTOs)
                .clickStats(stats)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
