package com.srllc.aws_textract.domain.service.impl;

import com.srllc.aws_textract.domain.dto.ReceiptDTO;
import com.srllc.aws_textract.domain.dto.ReceiptItemDTO;
import com.srllc.aws_textract.domain.entity.Receipt;
import com.srllc.aws_textract.domain.entity.ReceiptItem;
import com.srllc.aws_textract.domain.exception.ReceiptNotFoundException;
import com.srllc.aws_textract.domain.exception.TextractException;
import com.srllc.aws_textract.domain.record.ExtractTextResponse;
import com.srllc.aws_textract.domain.dao.ReceiptDAO;
import com.srllc.aws_textract.domain.service.TextractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TextractServiceImpl implements TextractService {

    private final TextractClient textractClient;
    private final ReceiptDAO receiptDAO;

    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$?\\s*(\\d+\\.\\d{2})");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("^(\\d{1,3})$");
    private static final Pattern SECTION_HEADER = Pattern.compile("^(name|qty|quantity|price)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBTOTAL_PATTERN = Pattern.compile("^(sub\\s*total|subtotal)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CASH_PATTERN = Pattern.compile("^cash$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHANGE_PATTERN = Pattern.compile("^change$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CASHIER_PATTERN = Pattern.compile("^cashier:?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MANAGER_PATTERN = Pattern.compile("^manager:?$", Pattern.CASE_INSENSITIVE);

    @Override
    public ExtractTextResponse extractTextFromImage(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();

            var request = DetectDocumentTextRequest.builder()
                    .document(Document.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .build();

            var response = textractClient.detectDocumentText(request);

            List<String> lines = response.blocks().stream()
                    .filter(block -> block.blockType() == BlockType.LINE)
                    .map(Block::text)
                    .toList();

            log.info("=== EXTRACTED {} LINES ===", lines.size());
            for (int i = 0; i < lines.size(); i++) {
                log.info("Line {}: '{}'", i, lines.get(i));
            }

            return new ExtractTextResponse(lines);
        } catch (IOException e) {
            throw new TextractException("Failed to read file bytes", e);
        } catch (Exception e) {
            throw new TextractException("Textract Failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ReceiptDTO processAndSaveReceipt(MultipartFile file) {
        log.info("Processing receipt image: {}", file.getOriginalFilename());

        ExtractTextResponse extractedText = extractTextFromImage(file);
        List<String> lines = extractedText.lines();

        Receipt receipt = parseReceiptData(lines);
        Receipt savedReceipt = receiptDAO.save(receipt);

        log.info("Receipt saved successfully with ID: {}", savedReceipt.getId());

        return convertToDTO(savedReceipt);
    }

    @Override
    public List<ReceiptDTO> getAllReceipts() {
        return receiptDAO.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReceiptDTO getReceiptById(Long id) {
        Receipt receipt = receiptDAO.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found with id: " + id));
        return convertToDTO(receipt);
    }

    private Receipt parseReceiptData(List<String> lines) {
        Receipt receipt = new Receipt();

        extractHeaderInfo(lines, receipt);
        int itemsSectionStart = findItemsSectionStart(lines);
        int itemsSectionEnd = findItemsSectionEnd(lines);

        if (itemsSectionStart >= 0 && itemsSectionEnd > itemsSectionStart) {
            List<ReceiptItem> items = parseItemsFromSection(lines, itemsSectionStart, itemsSectionEnd);
            items.forEach(receipt::addItem);
        }

        extractFinancialData(lines, receipt);
        applyDefaults(receipt);
        logParsedReceipt(receipt);

        return receipt;
    }

    private void extractHeaderInfo(List<String> lines, Receipt receipt) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            String lowerLine = line.toLowerCase();

            if (receipt.getCompanyName() == null && isLikelyCompanyName(line, i)) {
                receipt.setCompanyName(line);
                log.info("✓ Company: {}", line);
            }

            if (receipt.getBranch() == null && isLikelyBranch(line)) {
                receipt.setBranch(line);
                log.info("✓ Branch: {}", line);
            }

            if (CASHIER_PATTERN.matcher(lowerLine).matches()) {
                String cashierValue = extractValueFromSameLine(line, "cashier");
                if (cashierValue == null) {
                    cashierValue = extractNextNumericValue(lines, i);
                }
                if (cashierValue != null) {
                    receipt.setCashierNumber(cashierValue);
                    log.info("✓ Cashier: {}", cashierValue);
                }
            }

            if (MANAGER_PATTERN.matcher(lowerLine).matches()) {
                String managerValue = extractValueFromSameLine(line, "manager");
                if (managerValue == null) {
                    managerValue = extractNextNonNumericValue(lines, i);
                }
                if (managerValue != null) {
                    receipt.setManagerName(managerValue);
                    log.info("✓ Manager: {}", managerValue);
                }
            }
        }
    }

    private int findItemsSectionStart(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (SECTION_HEADER.matcher(lines.get(i).toLowerCase()).matches()) {
                return i + 1;
            }
        }
        return -1;
    }

    private int findItemsSectionEnd(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String lowerLine = lines.get(i).toLowerCase();
            if (SUBTOTAL_PATTERN.matcher(lowerLine).matches() ||
                    lowerLine.contains("total") ||
                    CASH_PATTERN.matcher(lowerLine).matches()) {
                return i;
            }
        }
        return lines.size();
    }

    private List<ReceiptItem> parseItemsFromSection(List<String> lines, int start, int end) {
        List<ReceiptItem> items = new ArrayList<>();
        int i = start;

        while (i < end) {
            String line = lines.get(i).trim();

            if (line.isEmpty() || SECTION_HEADER.matcher(line.toLowerCase()).matches() ||
                    SUBTOTAL_PATTERN.matcher(line.toLowerCase()).matches() ||
                    isLikelySeparator(line) || isNumericOnly(line) || isPriceOnly(line)) {
                i++;
                continue;
            }

            ReceiptItem item = tryParseItem(lines, i, end);
            if (item != null) {
                items.add(item);
                log.info("✓ Item: {} x{} = ${}", item.getProductName(), item.getQuantity(), item.getPrice());
                i = findNextProductStart(lines, i + 1, end);
            } else {
                i++;
            }
        }

        return items;
    }

    private ReceiptItem tryParseItem(List<String> lines, int startIdx, int endIdx) {
        String productName = lines.get(startIdx).trim();
        Integer quantity = null;
        Double price = null;

        for (int i = startIdx + 1; i < Math.min(startIdx + 5, endIdx); i++) {
            String line = lines.get(i).trim();

            if (quantity == null && QUANTITY_PATTERN.matcher(line).matches()) {
                try {
                    quantity = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse quantity: {}", line);
                }
            }

            if (price == null) {
                Matcher priceMatcher = PRICE_PATTERN.matcher(line);
                if (priceMatcher.find()) {
                    try {
                        price = Double.parseDouble(priceMatcher.group(1));
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse price: {}", line);
                    }
                }
            }

            if (quantity != null && price != null) {
                ReceiptItem item = new ReceiptItem();
                item.setProductName(productName);
                item.setQuantity(quantity);
                item.setPrice(price);
                return item;
            }
        }

        return null;
    }

    private void extractFinancialData(List<String> lines, Receipt receipt) {
        for (int i = 0; i < lines.size(); i++) {
            String lowerLine = lines.get(i).toLowerCase();

            if (SUBTOTAL_PATTERN.matcher(lowerLine).matches()) {
                Double value = extractNextPriceValue(lines, i);
                if (value != null) {
                    receipt.setSubTotal(value);
                    log.info("✓ SubTotal: ${}", value);
                }
            }

            if (CASH_PATTERN.matcher(lowerLine).matches()) {
                Double value = extractNextPriceValue(lines, i);
                if (value != null) {
                    receipt.setCash(value);
                    log.info("✓ Cash: ${}", value);
                }
            }

            if (CHANGE_PATTERN.matcher(lowerLine).matches()) {
                Double value = extractNextPriceValue(lines, i);
                if (value != null) {
                    receipt.setChangeAmount(value);
                    log.info("✓ Change: ${}", value);
                }
            }
        }
    }

    private boolean isLikelyCompanyName(String line, int idx) {
        return idx == 0 || (line.length() > 3 && !isPriceOnly(line) && !isNumericOnly(line) &&
                !line.toLowerCase().contains("city") && !line.toLowerCase().contains("address"));
    }

    private boolean isLikelyBranch(String line) {
        String lower = line.toLowerCase();
        return (lower.contains("city") && !lower.contains("index")) ||
                lower.contains("branch") || lower.contains("store") ||
                lower.contains("location") || lower.contains("outlet");
    }

    private boolean isLikelySeparator(String line) {
        return line.matches("^[-=*_]{3,}$");
    }

    private boolean isNumericOnly(String line) {
        return line.matches("^\\d+$");
    }

    private boolean isPriceOnly(String line) {
        return PRICE_PATTERN.matcher(line).matches();
    }

    private String extractNextNumericValue(List<String> lines, int startIdx) {
        for (int i = startIdx + 1; i < Math.min(startIdx + 3, lines.size()); i++) {
            String line = lines.get(i).trim();
            if (line.matches("^#?\\d+$")) {
                return line;
            }
        }
        return null;
    }

    private String extractNextNonNumericValue(List<String> lines, int startIdx) {
        for (int i = startIdx + 1; i < Math.min(startIdx + 3, lines.size()); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty() && !isNumericOnly(line) && !isPriceOnly(line)) {
                return line;
            }
        }
        return null;
    }

    private String extractValueFromSameLine(String line, String label) {
        String lowerLine = line.toLowerCase();
        int labelPos = lowerLine.indexOf(label);
        if (labelPos >= 0) {
            String remainder = line.substring(labelPos + label.length()).trim();
            remainder = remainder.replaceFirst("^:\\s*", "");
            if (!remainder.isEmpty()) {
                return remainder;
            }
        }
        return null;
    }

    private Double extractNextPriceValue(List<String> lines, int startIdx) {
        for (int i = startIdx + 1; i < Math.min(startIdx + 3, lines.size()); i++) {
            String line = lines.get(i).trim();
            Matcher matcher = PRICE_PATTERN.matcher(line);
            if (matcher.find()) {
                try {
                    return Double.parseDouble(matcher.group(1));
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse price value: {}", line);
                }
            }
        }
        return null;
    }

    private int findNextProductStart(List<String> lines, int startIdx, int endIdx) {
        for (int i = startIdx; i < endIdx; i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty() && !isNumericOnly(line) && !isPriceOnly(line)) {
                return i;
            }
        }
        return startIdx;
    }

    private void applyDefaults(Receipt receipt) {
        if (receipt.getCompanyName() == null) receipt.setCompanyName("Unknown Store");
        if (receipt.getBranch() == null) receipt.setBranch("Main Branch");
        if (receipt.getManagerName() == null) receipt.setManagerName("N/A");
        if (receipt.getCashierNumber() == null) receipt.setCashierNumber("N/A");
        if (receipt.getSubTotal() == null) receipt.setSubTotal(0.0);
        if (receipt.getCash() == null) receipt.setCash(0.0);
        if (receipt.getChangeAmount() == null) receipt.setChangeAmount(0.0);
    }

    private void logParsedReceipt(Receipt receipt) {
        log.info("=== PARSING COMPLETE ===");
        log.info("Company: {}", receipt.getCompanyName());
        log.info("Branch: {}", receipt.getBranch());
        log.info("Manager: {}", receipt.getManagerName());
        log.info("Cashier: {}", receipt.getCashierNumber());
        log.info("Items: {}", receipt.getItems().size());
        log.info("SubTotal: ${}", receipt.getSubTotal());
        log.info("Cash: ${}", receipt.getCash());
        log.info("Change: ${}", receipt.getChangeAmount());
    }

    private ReceiptDTO convertToDTO(Receipt receipt) {
        List<ReceiptItemDTO> itemDTOs = receipt.getItems().stream()
                .map(item -> ReceiptItemDTO.builder()
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return ReceiptDTO.builder()
                .id(receipt.getId())
                .companyName(receipt.getCompanyName())
                .branch(receipt.getBranch())
                .managerName(receipt.getManagerName())
                .cashierNumber(receipt.getCashierNumber())
                .items(itemDTOs)
                .subTotal(receipt.getSubTotal())
                .cash(receipt.getCash())
                .changeAmount(receipt.getChangeAmount())
                .build();
    }
}