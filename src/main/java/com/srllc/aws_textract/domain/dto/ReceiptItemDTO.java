package com.srllc.aws_textract.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemDTO {
    private String productName;
    private Integer quantity;
    private Double price;
}