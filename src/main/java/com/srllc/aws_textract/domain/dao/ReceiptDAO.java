package com.srllc.aws_textract.domain.dao;

import com.srllc.aws_textract.domain.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptDAO extends JpaRepository<Receipt, Long> {
}