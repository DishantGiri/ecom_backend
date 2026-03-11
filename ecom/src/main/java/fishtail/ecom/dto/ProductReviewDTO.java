package fishtail.ecom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewDTO {
    private Long id;
    private String reviewerName;
    private String reviewText;
    private String imageUrl; // Full URL mapped in response
    private Double starRating;
    private LocalDateTime createdAt;
}
