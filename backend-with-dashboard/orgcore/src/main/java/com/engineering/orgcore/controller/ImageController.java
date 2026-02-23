package com.engineering.orgcore.controller;

import com.engineering.orgcore.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImageController {

    private final FileStorageService fileStorageService;

    /**
     * Download/view image file
     * GET /api/images/{fileName}
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String fileName) {
        try {
            byte[] imageData = fileStorageService.loadFile(fileName);

            if (imageData == null) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(imageData);

            // Determine content type based on file extension
            String contentType = "application/octet-stream";
            if (fileName.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileName.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Decrypt and view encrypted image
     * POST /api/images/decrypt
     * Body: { "encryptedData": "base64encodedencrypteddata" }
     */
    @PostMapping("/decrypt")
    public ResponseEntity<Resource> decryptImage(@RequestBody DecryptImageRequest request) {
        try {
            byte[] imageData = fileStorageService.decryptImage(request.encryptedData());

            if (imageData == null) {
                return ResponseEntity.badRequest().build();
            }

            ByteArrayResource resource = new ByteArrayResource(imageData);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // Default to JPEG
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public record DecryptImageRequest(String encryptedData) {}
}
