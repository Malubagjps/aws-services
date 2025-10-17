package com.srllc.aws_textract.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CelebrityDTO {
    private String name;
    private Float matchConfidence;
    private List<String> urls;
}