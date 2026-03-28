package com.deepflow.application.image;

import com.deepflow.application.port.out.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final ImageStorage imageStorage;

    public String upload(Long userId, String originalFilename, byte[] data, String contentType) {
        validate(originalFilename, data, contentType);
        String extension = extractExtension(originalFilename);
        String key = "images/" + userId + "/" + UUID.randomUUID() + extension;
        return imageStorage.upload(key, data, contentType);
    }

    public void deleteRemovedImages(List<String> oldUrls, List<String> newUrls) {
        if (oldUrls == null || oldUrls.isEmpty()) {
            return;
        }
        Set<String> kept = newUrls != null ? Set.copyOf(newUrls) : Set.of();
        for (String oldUrl : oldUrls) {
            if (!kept.contains(oldUrl)) {
                deleteQuietly(oldUrl);
            }
        }
    }

    public void deleteAll(List<String> urls) {
        if (urls == null) return;
        for (String url : urls) {
            deleteQuietly(url);
        }
    }

    private void deleteQuietly(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl);
            imageStorage.delete(key);
        } catch (Exception e) {
            log.warn("Failed to delete image from storage: {}", imageUrl, e);
        }
    }

    private void validate(String filename, byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }
        if (data.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType + ". Allowed: jpg, png, gif, webp");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String extractKeyFromUrl(String url) {
        int bucketPathIndex = url.indexOf("/images/");
        if (bucketPathIndex == -1) {
            throw new IllegalArgumentException("Invalid image URL: " + url);
        }
        return url.substring(bucketPathIndex + 1);
    }
}