package com.srllc.aws_textract.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.accessKey}")
    private String accessKeyId;

    @Value("${aws.secretKey}")
    private String secretAccessKey;

    private StaticCredentialsProvider credentialsProvider() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return StaticCredentialsProvider.create(awsCredentials);
    }

    private Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public TextractClient textractClient() {
        return TextractClient.builder()
                .region(awsRegion())
                .credentialsProvider(credentialsProvider())
                .build();
    }

    @Bean
    public RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(awsRegion())
                .credentialsProvider(credentialsProvider())
                .build();
    }
}
