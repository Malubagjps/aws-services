package com.srllc.aws_textract.domain.service;

import com.srllc.aws_textract.domain.dto.ReceiptDTO;
import com.srllc.aws_textract.domain.record.ExtractTextResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TextractService {
    ExtractTextResponse extractTextFromImage(MultipartFile file);
    ReceiptDTO processAndSaveReceipt(MultipartFile file);
    List<ReceiptDTO> getAllReceipts();
    ReceiptDTO getReceiptById(Long id);
}