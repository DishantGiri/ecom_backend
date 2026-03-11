package fishtail.ecom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentSnippetDTO {
    private Long id;
    private String name;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
