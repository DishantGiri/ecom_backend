package fishtail.ecom.service;

import fishtail.ecom.dto.CategoryDTO;
import fishtail.ecom.dto.ProductOfferDTO;
import fishtail.ecom.dto.ProductRequestDTO;
import fishtail.ecom.dto.ProductResponseDTO;
import fishtail.ecom.entity.Category;
import fishtail.ecom.entity.Product;
import fishtail.ecom.entity.ProductOffer;
import fishtail.ecom.repository.CategoryRepository;
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
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CurrencyService currencyService;
    private final CategoryRepository categoryRepository;

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
            List<MultipartFile> promotionalImages,
            Map<Integer, MultipartFile> offerImages) throws IOException {

        validateStarRating(dto.getStarRating());

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
        } else if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            category = categoryRepository.findByNameIgnoreCase(dto.getCategory()).orElse(null);
        }

        Product product = Product.builder()
                .title(dto.getTitle())
                .ribbon(dto.getRibbon())
                .numberOfReviews(dto.getNumberOfReviews())
                .starRating(dto.getStarRating())
                .originalPrice(dto.getOriginalPrice())
                .discountedPrice(dto.getDiscountedPrice())
                .productLink(dto.getProductLink())
                .category(category)
                .description(dto.getDescription())
                .highlights(dto.getHighlights())
                .details(dto.getDetails())
                .directions(dto.getDirections())
                .benefits(dto.getBenefits())
                .guarantee(dto.getGuarantee())
                .shippingInfo(dto.getShippingInfo())
                .sectionOrder(
                        dto.getSectionOrder() != null ? new ArrayList<>(dto.getSectionOrder()) : new ArrayList<>())
                .galleryImages(new ArrayList<>())
                .promotionalImages(new ArrayList<>())
                .offers(new ArrayList<>())
                .customFields(new ArrayList<>())
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

        // Save promotional images
        if (promotionalImages != null) {
            for (MultipartFile file : promotionalImages) {
                if (file != null && !file.isEmpty()) {
                    product.getPromotionalImages().add(saveFile(file));
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

        // Attach dynamic custom fields
        if (dto.getCustomFields() != null) {
            for (fishtail.ecom.dto.ProductCustomFieldDTO cfDTO : dto.getCustomFields()) {
                product.getCustomFields().add(buildCustomField(cfDTO, product));
            }
        }

        product = productRepository.save(product);
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BULK UPLOAD
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<ProductResponseDTO> bulkUpload(MultipartFile file) throws IOException {
        List<Product> products = new ArrayList<>();
        List<String> allImages = productRepository.findAllFeatureImages();
        Random random = new Random();

        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(fileReader,
                        CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                // Title
                String title = csvRecord.isMapped("title") ? csvRecord.get("title") : null;
                if (title == null || title.isBlank())
                    continue;

                // Prices
                BigDecimal originalPrice = BigDecimal.ZERO;
                if (csvRecord.isMapped("original_price") && !csvRecord.get("original_price").isBlank()) {
                    originalPrice = new BigDecimal(csvRecord.get("original_price"));
                } else if (csvRecord.isMapped("originalPrice") && !csvRecord.get("originalPrice").isBlank()) {
                    originalPrice = new BigDecimal(csvRecord.get("originalPrice"));
                }

                BigDecimal discountedPrice = null;
                if (csvRecord.isMapped("discounted_price") && !csvRecord.get("discounted_price").isBlank()) {
                    discountedPrice = new BigDecimal(csvRecord.get("discounted_price"));
                } else if (csvRecord.isMapped("discountedPrice") && !csvRecord.get("discountedPrice").isBlank()) {
                    discountedPrice = new BigDecimal(csvRecord.get("discountedPrice"));
                }

                // Reviews & Rating
                Integer numberOfReviews = 0;
                if (csvRecord.isMapped("number_of_reviews") && !csvRecord.get("number_of_reviews").isBlank()) {
                    numberOfReviews = Integer.parseInt(csvRecord.get("number_of_reviews"));
                } else if (csvRecord.isMapped("numberOfReviews") && !csvRecord.get("numberOfReviews").isBlank()) {
                    numberOfReviews = Integer.parseInt(csvRecord.get("numberOfReviews"));
                }

                Double starRating = 5.0;
                if (csvRecord.isMapped("star_rating") && !csvRecord.get("star_rating").isBlank()) {
                    starRating = Double.parseDouble(csvRecord.get("star_rating"));
                } else if (csvRecord.isMapped("starRating") && !csvRecord.get("starRating").isBlank()) {
                    starRating = Double.parseDouble(csvRecord.get("starRating"));
                }

                String productLink = csvRecord.isMapped("product_link") ? csvRecord.get("product_link") : null;
                if (productLink == null && csvRecord.isMapped("productLink")) {
                    productLink = csvRecord.get("productLink");
                }

                // Category
                Category category = null;
                if (csvRecord.isMapped("category_id") && !csvRecord.get("category_id").isBlank()) {
                    category = categoryRepository.findById(Long.parseLong(csvRecord.get("category_id"))).orElse(null);
                } else if (csvRecord.isMapped("categoryId") && !csvRecord.get("categoryId").isBlank()) {
                    category = categoryRepository.findById(Long.parseLong(csvRecord.get("categoryId"))).orElse(null);
                } else if (csvRecord.isMapped("category") && !csvRecord.get("category").isBlank()) {
                    category = categoryRepository.findByNameIgnoreCase(csvRecord.get("category")).orElse(null);
                }

                String featureImage = null;
                if (allImages != null && !allImages.isEmpty()) {
                    featureImage = allImages.get(random.nextInt(allImages.size()));
                }

                Product product = Product.builder()
                        .title(title)
                        .originalPrice(originalPrice)
                        .discountedPrice(discountedPrice)
                        .numberOfReviews(numberOfReviews)
                        .starRating(starRating)
                        .productLink(productLink)
                        .category(category)
                        .featureImage(featureImage)
                        .description(csvRecord.isMapped("description") ? csvRecord.get("description") : "")
                        .highlights(csvRecord.isMapped("highlights") ? csvRecord.get("highlights") : "")
                        .details(csvRecord.isMapped("details") ? csvRecord.get("details") : "")
                        .sectionOrder(new ArrayList<>())
                        .galleryImages(new ArrayList<>())
                        .promotionalImages(new ArrayList<>())
                        .offers(new ArrayList<>())
                        .customFields(new ArrayList<>())
                        .build();

                products.add(product);
            }
        }

        List<Product> savedProducts = productRepository.saveAll(products);
        return savedProducts.stream().map(p -> toResponseDTO(p, false, "USD")).collect(Collectors.toList());
    }

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

    public ProductResponseDTO getProductById(Long id, String currency) {
        Product product = findOrThrow(id);

        BigDecimal minPrice = product.getDiscountedPrice() != null
                ? product.getDiscountedPrice().multiply(BigDecimal.valueOf(0.5))
                : BigDecimal.ZERO;
        BigDecimal maxPrice = product.getDiscountedPrice() != null
                ? product.getDiscountedPrice().multiply(BigDecimal.valueOf(1.5))
                : BigDecimal.valueOf(999999);

        List<Product> similarProducts = productRepository.findSimilarProducts(
                product.getCategory() != null ? product.getCategory().getId() : null,
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
            List<MultipartFile> promotionalImages,
            Map<Integer, MultipartFile> offerImages) throws IOException {

        validateStarRating(dto.getStarRating());
        Product product = findOrThrow(id);

        product.setTitle(dto.getTitle());
        product.setRibbon(dto.getRibbon());
        product.setNumberOfReviews(dto.getNumberOfReviews());
        product.setStarRating(dto.getStarRating());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setDiscountedPrice(dto.getDiscountedPrice());
        product.setProductLink(dto.getProductLink());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        } else if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            Category category = categoryRepository.findByNameIgnoreCase(dto.getCategory()).orElse(null);
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        product.setDescription(dto.getDescription());
        product.setHighlights(dto.getHighlights());
        product.setDetails(dto.getDetails());
        product.setDirections(dto.getDirections());
        product.setBenefits(dto.getBenefits());
        product.setGuarantee(dto.getGuarantee());
        product.setShippingInfo(dto.getShippingInfo());
        if (dto.getSectionOrder() != null) {
            product.setSectionOrder(new ArrayList<>(dto.getSectionOrder()));
        }

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

        // Append new promotional images (keep existing ones)
        if (promotionalImages != null) {
            for (MultipartFile file : promotionalImages) {
                if (file != null && !file.isEmpty()) {
                    product.getPromotionalImages().add(saveFile(file));
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

        // Replace custom fields
        product.getCustomFields().clear();
        if (dto.getCustomFields() != null) {
            for (fishtail.ecom.dto.ProductCustomFieldDTO cfDTO : dto.getCustomFields()) {
                product.getCustomFields().add(buildCustomField(cfDTO, product));
            }
        }

        product = productRepository.save(product);
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findOrThrow(id);
        deleteFileIfExists(product.getFeatureImage());
        product.getGalleryImages().forEach(this::deleteFileIfExists);
        product.getPromotionalImages().forEach(this::deleteFileIfExists);
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
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // REMOVE SINGLE PROMOTIONAL IMAGE
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public ProductResponseDTO removePromotionalImage(Long productId, String filename) {
        Product product = findOrThrow(productId);
        boolean removed = product.getPromotionalImages().remove(filename);
        if (removed) {
            deleteFileIfExists(filename);
        }
        product = productRepository.save(product);
        return toResponseDTO(product, true, "USD");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SEARCH
    // ──────────────────────────────────────────────────────────────────────────

    public List<ProductResponseDTO> searchByTitle(String keyword, String currency) {
        if (keyword == null || keyword.isBlank())
            return List.of();

        String lowerKeyword = keyword.trim().toLowerCase();

        // 1. Exact / substring matches (fast DB query)
        List<Product> exactMatches = productRepository.findByTitleContainingIgnoreCase(lowerKeyword);
        Set<Long> exactIds = exactMatches.stream().map(Product::getId).collect(Collectors.toSet());

        // 2. Levenshtein fuzzy matching against all product titles
        List<Product> allProducts = productRepository.findAll();
        List<Product> fuzzyMatches = allProducts.stream()
                .filter(p -> !exactIds.contains(p.getId()))
                .filter(p -> {
                    String title = p.getTitle().toLowerCase();
                    String[] titleWords = title.split("\\s+");
                    for (String word : titleWords) {
                        int maxAllowedEdits = Math.max(1, (int) Math.floor(word.length() * 0.35));
                        if (levenshtein(lowerKeyword, word) <= maxAllowedEdits) {
                            return true;
                        }
                    }
                    if (lowerKeyword.length() <= title.length()) {
                        int maxAllowedEdits = Math.max(1, (int) Math.floor(lowerKeyword.length() * 0.35));
                        if (levenshtein(lowerKeyword, title) <= maxAllowedEdits) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<Product> combined = new ArrayList<>(exactMatches);
        combined.addAll(fuzzyMatches);

        return combined.stream()
                .map(p -> toResponseDTO(p, false, currency))
                .collect(Collectors.toList());
    }

    /**
     * Classic dynamic-programming Levenshtein (edit) distance.
     * Lower = more similar. 0 means exact match.
     */
    private int levenshtein(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++)
            dp[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                }
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    public List<ProductResponseDTO> getByCategory(String category, String currency) {
        return productRepository.findByCategoryNameIgnoreCase(category)
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

    private fishtail.ecom.entity.ProductCustomField buildCustomField(fishtail.ecom.dto.ProductCustomFieldDTO dto,
            Product product) {
        return fishtail.ecom.entity.ProductCustomField.builder()
                .fieldName(dto.getFieldName())
                .fieldValue(dto.getFieldValue())
                .displayOrder(dto.getDisplayOrder())
                .product(product)
                .build();
    }

    private ProductOfferDTO toOfferDTO(ProductOffer offer, String currency) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String offerImageUrl = (offer.getFeatureImage() != null && !offer.getFeatureImage().isBlank())
                ? cleanBaseUrl + "/api/images/" + offer.getFeatureImage()
                : null;

        BigDecimal originalConverted = currencyService.convertFromUsd(offer.getOriginalPrice(), currency);
        BigDecimal discountedConverted = currencyService.convertFromUsd(offer.getDiscountedPrice(), currency);

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
        String currency = (requestCurrency == null || requestCurrency.isBlank()) ? "USD"
                : requestCurrency.toUpperCase();

        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        // Sort offers by displayOrder
        List<ProductOffer> sortedOffers = product.getOffers() == null ? Collections.emptyList()
                : product.getOffers().stream()
                        .sorted(Comparator.comparingInt(o -> (o.getDisplayOrder() == null ? 99 : o.getDisplayOrder())))
                        .collect(Collectors.toList());

        List<ProductOfferDTO> offerDTOs = sortedOffers.stream()
                .map(o -> toOfferDTO(o, currency))
                .collect(Collectors.toList());

        String featureUrl = (product.getFeatureImage() != null && !product.getFeatureImage().isBlank())
                ? cleanBaseUrl + "/api/images/" + product.getFeatureImage()
                : null;

        List<String> galleryUrls = new ArrayList<>();
        if (product.getGalleryImages() != null) {
            product.getGalleryImages().stream()
                    .map(f -> cleanBaseUrl + "/api/images/" + f)
                    .forEach(galleryUrls::add);
        }

        List<String> promotionalUrls = new ArrayList<>();
        if (product.getPromotionalImages() != null) {
            product.getPromotionalImages().stream()
                    .map(f -> cleanBaseUrl + "/api/images/" + f)
                    .forEach(promotionalUrls::add);
        }

        List<fishtail.ecom.dto.ProductClickStatDTO> stats = product.getClickStats() == null ? Collections.emptyList()
                : product.getClickStats().stream()
                        .map(s -> fishtail.ecom.dto.ProductClickStatDTO.builder()
                                .country(s.getCountry())
                                .clickCount(s.getClickCount())
                                .build())
                        .collect(Collectors.toList());

        CategoryDTO categoryDTO = null;
        if (product.getCategory() != null) {
            String catImageUrl = (product.getCategory().getImageUrl() != null
                    && !product.getCategory().getImageUrl().isBlank())
                            ? cleanBaseUrl + "/api/images/" + product.getCategory().getImageUrl()
                            : null;
            categoryDTO = CategoryDTO.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .imageUrl(catImageUrl)
                    .build();
        }

        List<fishtail.ecom.dto.ProductCustomFieldDTO> customFieldDTOs = product.getCustomFields() == null
                ? Collections.emptyList()
                : product.getCustomFields().stream()
                        .sorted(Comparator
                                .comparingInt(cf -> (cf.getDisplayOrder() == null ? 99 : cf.getDisplayOrder())))
                        .map(cf -> fishtail.ecom.dto.ProductCustomFieldDTO.builder()
                                .id(cf.getId())
                                .fieldName(cf.getFieldName())
                                .fieldValue(cf.getFieldValue())
                                .displayOrder(cf.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList());

        List<fishtail.ecom.dto.ProductReviewDTO> reviewDTOs = product.getReviews() == null ? Collections.emptyList()
                : product.getReviews().stream()
                        .map(r -> fishtail.ecom.dto.ProductReviewDTO.builder()
                                .id(r.getId())
                                .reviewerName(r.getReviewerName())
                                .reviewText(r.getReviewText())
                                .starRating(r.getStarRating())
                                .imageUrl(r.getImageUrl() != null && !r.getImageUrl().isBlank()
                                        ? cleanBaseUrl + "/api/images/" + r.getImageUrl()
                                        : null)
                                .createdAt(r.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());

        return ProductResponseDTO.builder()
                .id(product.getId())
                .title(product.getTitle())
                .ribbon(product.getRibbon())
                .numberOfReviews(product.getNumberOfReviews())
                .starRating(product.getStarRating())
                .originalPrice(currencyService.convertFromUsd(product.getOriginalPrice(), currency))
                .discountedPrice(currencyService.convertFromUsd(product.getDiscountedPrice(), currency))
                .currency(currency)
                .featureImageUrl(featureUrl)
                .galleryImageUrls(galleryUrls)
                .promotionalImageUrls(promotionalUrls)
                .productLink(product.getProductLink())
                .category(categoryDTO)
                .description(product.getDescription())
                .highlights(product.getHighlights())
                .details(product.getDetails())
                .directions(product.getDirections())
                .benefits(product.getBenefits())
                .guarantee(product.getGuarantee())
                .shippingInfo(product.getShippingInfo())
                .sectionOrder(product.getSectionOrder() != null ? new ArrayList<>(product.getSectionOrder())
                        : new ArrayList<>())
                .offers(offerDTOs)
                .customFields(customFieldDTOs)
                .clickStats(stats)
                .reviews(reviewDTOs)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}