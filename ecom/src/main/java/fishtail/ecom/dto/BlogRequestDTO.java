package fishtail.ecom.dto;

import lombok.*;

/**
 * Used for receiving Blog creation or update requests.
 * The featureImage is received as a separate MultipartFile part.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogRequestDTO {

    private String title;

    // HTML string from the rich text editor (e.g., Quill, TinyMCE, TipTap)
    private String content;

    // Short blockquote / intro section displayed prominently below the header image
    private String intro;

    private String author;

    // Optional SEO Overrides (if left blank, we can auto-generate them in service)
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
}
