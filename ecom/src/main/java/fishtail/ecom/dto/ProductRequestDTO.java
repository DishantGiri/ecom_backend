package fishtail.ecom.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO used when creating or updating a product.
 * Sent as multipart/form-data — images are handled separately.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    private String title;

    private String ribbon;

    private Integer numberOfReviews;

    /** Star rating, e.g. 0.5, 1.0, 1.5 … 5.0 */
    private Double starRating;

    private BigDecimal originalPrice;

    private BigDecimal discountedPrice;

    private String productLink;

    private Long categoryId;
    private String category;

    /** List of offer/bundle types for this product */
    private List<ProductOfferDTO> offers;

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
}
