package com.deepflow.api.controller.image;

import com.deepflow.api.dto.CommonResponse;
import com.deepflow.api.security.CustomUserDetails;
import com.deepflow.application.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Image", description = "Image Upload API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Upload Images (single or multiple)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<List<String>>> upload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = imageService.upload(
                    userDetails.getUserId(),
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getContentType()
            );
            urls.add(url);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(urls));
    }
}