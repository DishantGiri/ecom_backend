package fishtail.ecom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "number_of_reviews")
    private Integer numberOfReviews;

    @Column(name = "star_rating", nullable = false)
    private Double starRating; // e.g., 4.5, 3.0, 5.0 — increments of 0.5

    @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "feature_image")
    private String featureImage; // stored file path / URL

    @Column(name = "product_link")
    private String productLink;

    @Column(name = "category")
    private String category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    @Column(name = "directions", columnDefinition = "TEXT")
    private String directions;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "guarantee", columnDefinition = "TEXT")
    private String guarantee;

    @Column(name = "shipping_info", columnDefinition = "TEXT")
    private String shippingInfo;
 
    /** Order of sections (Description, Highlights, etc.) */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_section_order", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "section_key")
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> sectionOrder = new ArrayList<>();

    /** Gallery images */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_gallery_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> galleryImages = new ArrayList<>();

    /** Promotional / Manufacturer Description Images */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_promotional_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> promotionalImages = new ArrayList<>();

    /** Bundle / offer types (e.g. 1 bottle, 2 bottles, 3 bottles) */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductOffer> offers = new ArrayList<>();

    /** Click analytics per country */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductClickStat> clickStats = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
