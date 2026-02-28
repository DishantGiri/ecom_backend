package fishtail.ecom.service;

import fishtail.ecom.dto.ClickTrackRequestDTO;
import fishtail.ecom.entity.Product;
import fishtail.ecom.entity.ProductClickStat;
import fishtail.ecom.repository.ProductClickStatRepository;
import fishtail.ecom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClickTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackingService.class);
    private final ProductRepository productRepository;
    private final ProductClickStatRepository clickStatRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public void trackClick(ClickTrackRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String country = resolveCountryFromIp(request.getIpAddress());

        ProductClickStat stat = clickStatRepository.findByProductIdAndCountry(product.getId(), country)
                .orElse(ProductClickStat.builder()
                        .product(product)
                        .country(country)
                        .clickCount(0L)
                        .build());

        stat.setClickCount(stat.getClickCount() + 1);
        clickStatRepository.save(stat);
    }

    private String resolveCountryFromIp(String ip) {
        if (ip == null || ip.isBlank() || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "Unknown";
        }
        try {
            // Using ip-api.com free endpoint for demo
            String url = "http://ip-api.com/json/" + ip;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                return (String) response.get("country");
            }
        } catch (Exception e) {
            log.error("Failed to resolve IP: {}", e.getMessage());
        }
        return "Unknown";
    }
}
