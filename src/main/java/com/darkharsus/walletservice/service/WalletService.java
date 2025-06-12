package com.darkharsus.walletservice.service;

import com.darkharsus.walletservice.dto.WalletBalanceResponse;
import com.darkharsus.walletservice.dto.WalletOperationRequest;

import java.util.UUID;

public interface WalletService {
    void performOperation(WalletOperationRequest request);
    WalletBalanceResponse getBalance(UUID walletId);
}
