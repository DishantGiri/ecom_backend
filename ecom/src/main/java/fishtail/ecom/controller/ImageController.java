package fishtail.ecom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves uploaded images from the local upload directory.
 * Accessible at GET /api/images/{filename}
 * No authentication required so frontend can display product images freely.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * GET /api/images/{filename}
     * Returns the image file as a byte stream.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize().toAbsolutePath();
        FileSystemResource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists() || !resource.isReadable()) {
            System.err.println("Image not found or not readable: " + filePath);
            return ResponseEntity.notFound().build();
        }

        // Detect content type from extension
        String contentType = detectContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))
            return "image/png";
        if (lower.endsWith(".gif"))
            return "image/gif";
        if (lower.endsWith(".webp"))
            return "image/webp";
        if (lower.endsWith(".svg"))
            return "image/svg+xml";
        return "image/jpeg"; // default
    }
}
