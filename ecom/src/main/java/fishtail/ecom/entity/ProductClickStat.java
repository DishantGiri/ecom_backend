package fishtail.ecom.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_click_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product_id", "country" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductClickStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String country;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;
}
