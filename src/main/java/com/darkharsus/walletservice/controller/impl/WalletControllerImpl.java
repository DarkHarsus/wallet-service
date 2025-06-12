package com.darkharsus.walletservice.controller.impl;

import com.darkharsus.walletservice.controller.WalletController;
import com.darkharsus.walletservice.dto.WalletBalanceResponse;
import com.darkharsus.walletservice.dto.WalletOperationRequest;
import com.darkharsus.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.darkharsus.walletservice.constants.Constants.OPERATION_SUCCESSFUL;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletControllerImpl implements WalletController {

    private final WalletService walletService;

    @Override
    @PostMapping("/wallet")
    public ResponseEntity<String> performOperation(@Valid @RequestBody WalletOperationRequest request) {
        walletService.performOperation(request);
        return ResponseEntity.ok(OPERATION_SUCCESSFUL);
    }

    @Override
    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }
}
