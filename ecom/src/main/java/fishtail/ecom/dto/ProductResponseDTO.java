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

    private Integer numberOfReviews;

    private Double starRating;

    private BigDecimal originalPrice;

    private BigDecimal discountedPrice;

    /** Full URL to the feature image served by /api/images/{filename} */
    private String featureImageUrl;

    /** Full URLs for gallery images */
    private List<String> galleryImageUrls;

    private String productLink;

    private String category;

    private List<ProductOfferDTO> offers;

    /** Recommended products ("You may also like") */
    private List<ProductResponseDTO> similarProducts;

    /** Analytics: Clicks per Country */
    private List<ProductClickStatDTO> clickStats;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
