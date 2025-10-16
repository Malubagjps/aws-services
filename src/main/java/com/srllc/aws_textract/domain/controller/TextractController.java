package com.srllc.aws_textract.domain.controller;

import com.srllc.aws_textract.domain.dto.ReceiptDTO;
import com.srllc.aws_textract.domain.record.ExtractTextResponse;
import com.srllc.aws_textract.domain.service.TextractService;
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

import java.util.List;

@RestController
@RequestMapping("api/v1/textract")
@Tag(name = "AWS Textract Controller", description = "Operations for managing AWS Textract and receipt processing")
@RequiredArgsConstructor
public class TextractController {

    private final TextractService textractService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract raw text from an uploaded image or document")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Text extracted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ExtractTextResponse> extractText(
            @Parameter(description = "Input file to extract text from", required = true)
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(textractService.extractTextFromImage(file));
    }

    @PostMapping(value = "/receipts/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Process receipt image and save parsed data to database")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receipt processed and saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or parsing error"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ReceiptDTO> processReceipt(
            @Parameter(description = "Receipt image file (PNG, JPG, PDF)", required = true)
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(textractService.processAndSaveReceipt(file));
    }

    @GetMapping("/receipts")
    @Operation(summary = "Get all stored receipts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receipts retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<ReceiptDTO>> getAllReceipts() {
        return ResponseEntity.ok(textractService.getAllReceipts());
    }

    @GetMapping("/receipts/{id}")
    @Operation(summary = "Get receipt by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receipt found"),
            @ApiResponse(responseCode = "404", description = "Receipt not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<ReceiptDTO> getReceiptById(
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(textractService.getReceiptById(id));
    }
}