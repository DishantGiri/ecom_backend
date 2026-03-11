package fishtail.ecom.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;

    private String title;

    private String ribbon;

    private Integer numberOfReviews;

    private Double starRating;

    private BigDecimal originalPrice;

    private BigDecimal discountedPrice;

    /** Full URL to the feature image served by /api/images/{filename} */
    private String featureImageUrl;

    /** Full URLs for gallery images */
    private List<String> galleryImageUrls;

    /** Full URLs for promotional / manufacturer images */
    private List<String> promotionalImageUrls;

    private String productLink;

    private CategoryDTO category;

    private List<ProductOfferDTO> offers;

    /** Recommended products ("You may also like") */
    private List<ProductResponseDTO> similarProducts;

    /** Analytics: Clicks per Country */
    private List<ProductClickStatDTO> clickStats;

    /** Admin-added customer reviews */
    private List<ProductReviewDTO> reviews;

    /** E.g., USD, EUR, GBP (defaults to USD) */
    private String currency;

    private String description;
    private String highlights;
    private String details;
    private String directions;
    private String benefits;
    private String guarantee;
    private String shippingInfo;

    /** List of dynamic custom fields created by an admin */
    private List<ProductCustomFieldDTO> customFields;

    /** Order of sections (Description, Highlights, etc.) */
    private List<String> sectionOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
