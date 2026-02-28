package fishtail.ecom.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOfferDTO {

    private Long id;

    /** Descriptive label: "Buy 1 Bottle", "Buy 2 Bottles Get 1 Free", etc. */
    private String label;

    /** Number of units in this bundle */
    private Integer quantity;

    private BigDecimal originalPrice;

    private BigDecimal discountedPrice;

    /** Order in which this offer is displayed (1 = most prominent) */
    private Integer displayOrder;

    /**
     * Full URL to this offer's feature/hero image.
     * Frontend: when user clicks this offer, set the displayed
     * product image to this URL (and prepend it to the gallery list).
     * Null if no offer-specific image was uploaded.
     */
    private String featureImageUrl;
}
