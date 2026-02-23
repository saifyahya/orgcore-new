package com.engineering.orgcore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // 16 chars for AES-128
    private final SecretKey secretKey;

    public FileStorageService( @Value("${file.upload-dir:uploads/products}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }

        // Initialize AES secret key
        this.secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
    }

    /**
     * Store file in the server
     * @param file MultipartFile to store
     * @return relative path of the stored file
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        // Normalize file name
        originalFileName = StringUtils.cleanPath(originalFileName);

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + originalFileName);
            }

            // Generate unique filename to avoid conflicts
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", uniqueFileName);
            return  uniqueFileName;

        } catch (IOException ex) {
            log.error("Could not store file " + originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    /**
     * Load file from storage
     * @param fileName name of the file to load
     * @return byte array of the file
     */
    public byte[] loadFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                log.warn("File not found: {}", fileName);
                return null;
            }
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            log.error("Could not read file " + fileName, ex);
            throw new RuntimeException("Could not read file " + fileName, ex);
        }
    }

    /**
     * Delete file from storage
     * @param fileName name of the file to delete
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully: {}", fileName);
        } catch (IOException ex) {
            log.error("Could not delete file " + fileName, ex);
        }
    }

    /**
     * Encrypt image data to base64 string for secure transmission
     * @param imagePath path to the image file
     * @return base64 encoded encrypted image data
     */
    public String getEncryptedImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            byte[] imageData = loadFile(imagePath);
            if (imageData == null) {
                return null;
            }

            // Encrypt the image data
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(imageData);

            // Encode to base64 for transmission
            return Base64.getEncoder().encodeToString(encryptedData);

        } catch (Exception ex) {
            log.error("Could not encrypt image " + imagePath, ex);
            return null;
        }
    }

    /**
     * Decrypt image data from base64 string
     * @param encryptedBase64 base64 encoded encrypted image data
     * @return decrypted image byte array
     */
    public byte[] decryptImage(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            return null;
        }

        try {
            // Decode from base64
            byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);

            // Decrypt the image data
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encryptedData);

        } catch (Exception ex) {
            log.error("Could not decrypt image data", ex);
            return null;
        }
    }

    /**
     * Get file storage location path
     * @return Path object of storage location
     */
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
