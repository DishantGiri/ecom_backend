package fishtail.ecom.controller;

import fishtail.ecom.dto.ProductClickReportDTO;
import fishtail.ecom.service.ClickTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ClickTrackingService clickTrackingService;

    /**
     * GET /api/admin/analytics/clicks
     * Returns a detailed click report for admins (Product Titles + Detailed
     * Countries).
     */
    @GetMapping("/clicks")
    public ResponseEntity<List<ProductClickReportDTO>> getDetailedClickReport() {
        return ResponseEntity.ok(clickTrackingService.getClickReport());
    }
}
