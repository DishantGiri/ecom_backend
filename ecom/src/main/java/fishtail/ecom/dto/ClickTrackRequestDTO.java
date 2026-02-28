package fishtail.ecom.dto;

import lombok.Data;

@Data
public class ClickTrackRequestDTO {
    private Long productId;
    private String ipAddress;
}
