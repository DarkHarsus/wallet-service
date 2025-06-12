package com.darkharsus.walletservice.dto;

import com.darkharsus.walletservice.enumeration.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class WalletOperationRequest {
    @NotNull
    private UUID walletId;

    @NotNull
    private OperationType operationType;

    @Positive
    private long amount;
}
