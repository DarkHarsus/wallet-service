package com.darkharsus.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class WalletBalanceResponse {
    private UUID walletId;
    private long balance;
}
