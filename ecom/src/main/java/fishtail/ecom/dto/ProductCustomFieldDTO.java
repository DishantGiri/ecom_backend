package fishtail.ecom.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCustomFieldDTO {
    private Long id;
    private String fieldName;
    private String fieldValue;
    private Integer displayOrder;
}
