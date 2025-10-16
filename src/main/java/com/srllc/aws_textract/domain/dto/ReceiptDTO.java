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
public class ReceiptDTO {
    private Long id;
    private String companyName;
    private String branch;
    private String managerName;
    private String cashierNumber;
    private List<ReceiptItemDTO> items;
    private Double subTotal;
    private Double cash;
    private Double changeAmount;
}