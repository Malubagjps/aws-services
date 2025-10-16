package com.srllc.aws_textract.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsConfig {

    @Bean
    public TextractClient textractClient(
            @Value("${aws.region}") String region,
            @Value("${aws.accessKey}") String accessKeyId,
            @Value("${aws.secretKey}") String secretAccessKey) {

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKeyId,
                secretAccessKey
        );

        return TextractClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}