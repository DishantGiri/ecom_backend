package fishtail.ecom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * e.g., "Buy 1 Bottle", "Buy 2 Bottles Get 1 Free", "Buy 3 Bottles Get 2 Free"
     */
    @Column(nullable = false)
    private String label;

    /** How many units in this bundle (e.g., 1, 2, 3) */
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    /** Display/sort order (1 = first offer shown) */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * Per-offer feature/hero image filename (stored in uploads/ directory).
     * When a user selects this offer, the UI should display this image
     * as the primary product image (position 0 in the gallery).
     */
    @Column(name = "feature_image")
    private String featureImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
