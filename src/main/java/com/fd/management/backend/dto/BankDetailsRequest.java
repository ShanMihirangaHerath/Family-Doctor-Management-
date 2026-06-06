package com.fd.management.backend.dto;

import lombok.Data;

@Data
public class BankDetailsRequest {
    private String bankName;
    private String branchName;
    private String accountName;
    private String accountNumber;
}