package com.srllc.aws_textract.domain.service;

import com.srllc.aws_textract.domain.dto.ImageAnalysisDTO;
import org.springframework.web.multipart.MultipartFile;

public interface RekognitionService {
    ImageAnalysisDTO detectLabels(MultipartFile file, Float minConfidence);
    ImageAnalysisDTO recognizeCelebrities(MultipartFile file);
}