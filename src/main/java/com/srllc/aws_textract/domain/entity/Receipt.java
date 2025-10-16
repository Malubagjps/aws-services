package com.srllc.aws_textract.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "branch")
    private String branch;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "cashier_number")
    private String cashierNumber;

    @Column(name = "sub_total")
    private Double subTotal;

    @Column(name = "cash")
    private Double cash;

    @Column(name = "change_amount")
    private Double changeAmount;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptItem> items = new ArrayList<>();

    public void addItem(ReceiptItem item) {
        items.add(item);
        item.setReceipt(this);
    }
}