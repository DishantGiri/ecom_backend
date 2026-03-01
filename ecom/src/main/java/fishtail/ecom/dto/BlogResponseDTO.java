package fishtail.ecom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogResponseDTO {

    private Long id;

    private String title;

    private String slug;

    private String content;

    private String featureImageUrl;

    private String author;

    private String metaTitle;

    private String metaDescription;

    private String metaKeywords;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
