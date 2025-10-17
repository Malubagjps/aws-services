package com.srllc.aws_textract.domain.service.impl;

import com.srllc.aws_textract.domain.dto.CelebrityDTO;
import com.srllc.aws_textract.domain.dto.DetectedLabelDTO;
import com.srllc.aws_textract.domain.dto.ImageAnalysisDTO;
import com.srllc.aws_textract.domain.exception.RekognitionException;
import com.srllc.aws_textract.domain.service.RekognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RekognitionServiceImpl implements RekognitionService {

    private final RekognitionClient rekognitionClient;

    @Override
    public ImageAnalysisDTO detectLabels(MultipartFile file, Float minConfidence) {
        try {
            log.info("Detecting labels in image: {}", file.getOriginalFilename());
            byte[] imageBytes = file.getBytes();

            DetectLabelsRequest request = DetectLabelsRequest.builder()
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .minConfidence(minConfidence)
                    .maxLabels(50)
                    .build();

            DetectLabelsResponse response = rekognitionClient.detectLabels(request);

            List<DetectedLabelDTO> labels = response.labels().stream()
                    .map(label -> DetectedLabelDTO.builder()
                            .name(label.name())
                            .confidence(label.confidence())
                            .build())
                    .collect(Collectors.toList());

            log.info("Detected {} labels", labels.size());

            return ImageAnalysisDTO.builder()
                    .labels(labels)
                    .totalDetections(labels.size())
                    .build();

        } catch (IOException e) {
            throw new RekognitionException("Failed to read image bytes", e);
        } catch (Exception e) {
            throw new RekognitionException("Label detection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ImageAnalysisDTO recognizeCelebrities(MultipartFile file) {
        try {
            log.info("Recognizing celebrities in image: {}", file.getOriginalFilename());
            byte[] imageBytes = file.getBytes();

            RecognizeCelebritiesRequest request = RecognizeCelebritiesRequest.builder()
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .build();

            RecognizeCelebritiesResponse response = rekognitionClient.recognizeCelebrities(request);

            List<CelebrityDTO> celebrities = response.celebrityFaces().stream()
                    .map(celebrity -> CelebrityDTO.builder()
                            .name(celebrity.name())
                            .matchConfidence(celebrity.matchConfidence())
                            .urls(celebrity.urls())
                            .build())
                    .collect(Collectors.toList());

            log.info("Recognized {} celebrities", celebrities.size());

            return ImageAnalysisDTO.builder()
                    .celebrities(celebrities)
                    .totalDetections(celebrities.size())
                    .build();

        } catch (IOException e) {
            throw new RekognitionException("Failed to read image bytes", e);
        } catch (Exception e) {
            throw new RekognitionException("Celebrity recognition failed: " + e.getMessage(), e);
        }
    }
}