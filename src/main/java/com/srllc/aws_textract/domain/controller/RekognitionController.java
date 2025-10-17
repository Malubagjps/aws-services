package com.srllc.aws_textract.domain.controller;

import com.srllc.aws_textract.domain.dto.ImageAnalysisDTO;
import com.srllc.aws_textract.domain.service.RekognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/rekognition")
@Tag(name = "AWS Rekognition Controller", description = "Image analysis using AWS Rekognition")
@RequiredArgsConstructor
public class RekognitionController {

    private final RekognitionService rekognitionService;

    @PostMapping(value = "/labels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Detect labels and objects in an image")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Labels detected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ImageAnalysisDTO> detectLabels(
            @Parameter(description = "Image file to analyze", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Minimum confidence level (0-100)", required = false)
            @RequestParam(defaultValue = "80.0") Float minConfidence) {
        return ResponseEntity.ok(rekognitionService.detectLabels(file, minConfidence));
    }

    @PostMapping(value = "/celebrities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Recognize celebrities in an image")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Celebrity recognition completed"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ImageAnalysisDTO> recognizeCelebrities(
            @Parameter(description = "Image file containing celebrities", required = true)
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(rekognitionService.recognizeCelebrities(file));
    }
}