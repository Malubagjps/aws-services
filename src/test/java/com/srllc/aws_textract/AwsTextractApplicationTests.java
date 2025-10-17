package com.srllc.aws_textract;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.textract.TextractClient;

@SpringBootTest
class AwsTextractApplicationTests {

    @MockitoBean
    private TextractClient textractClient;

    @MockitoBean
    private RekognitionClient rekognitionClient;

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}