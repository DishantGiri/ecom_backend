package fishtail.ecom.controller;

import fishtail.ecom.dto.ClickTrackRequestDTO;
import fishtail.ecom.service.ClickTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
@RequiredArgsConstructor
public class TrackingController {

    private final ClickTrackingService clickTrackingService;

    @PostMapping("/click")
    public ResponseEntity<String> trackClick(@RequestBody ClickTrackRequestDTO request) {
        clickTrackingService.trackClick(request);
        return ResponseEntity.ok("Click tracked successfully");
    }
}
