package com.darkharsus.walletservice.controller;

import com.darkharsus.walletservice.dto.WalletBalanceResponse;
import com.darkharsus.walletservice.dto.WalletOperationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface WalletController {
    ResponseEntity<String> performOperation(@Valid WalletOperationRequest request);
    ResponseEntity<WalletBalanceResponse> getBalance(UUID walletId);
}
