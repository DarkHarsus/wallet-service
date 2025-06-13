package com.darkharsus.walletservice.service.impl;

import com.darkharsus.walletservice.dto.WalletBalanceResponse;
import com.darkharsus.walletservice.dto.WalletOperationRequest;
import com.darkharsus.walletservice.entity.Wallet;
import com.darkharsus.walletservice.enumeration.OperationType;
import com.darkharsus.walletservice.exception.WalletException;
import com.darkharsus.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.darkharsus.walletservice.constants.Constants.INSUFFICIENT_FUNDS;
import static com.darkharsus.walletservice.constants.Constants.WALLET_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID walletId;
    private Wallet wallet;
    private WalletOperationRequest request;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(1000);
        wallet.setVersion(0);

        request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setAmount(500);
    }

    @Test
    void testPerformOperation_Deposit_Success() {
        request.setOperationType(OperationType.DEPOSIT);
        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        walletService.performOperation(request);

        assertEquals(1500, wallet.getBalance());
        verify(walletRepository, times(1)).findByIdWithLock(walletId);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void testPerformOperation_Withdraw_Success() {
        request.setOperationType(OperationType.WITHDRAW);
        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        walletService.performOperation(request);

        assertEquals(500, wallet.getBalance());
        verify(walletRepository, times(1)).findByIdWithLock(walletId);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void testPerformOperation_WalletNotFound() {
        request.setOperationType(OperationType.DEPOSIT);
        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.empty());

        WalletException exception = assertThrows(WalletException.class, () -> walletService.performOperation(request));
        assertEquals(WALLET_NOT_FOUND + walletId, exception.getMessage());
        verify(walletRepository, times(1)).findByIdWithLock(walletId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void testPerformOperation_Withdraw_InsufficientFunds() {
        request.setOperationType(OperationType.WITHDRAW);
        request.setAmount(2000);
        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(wallet));

        WalletException exception = assertThrows(WalletException.class, () -> walletService.performOperation(request));
        assertEquals(INSUFFICIENT_FUNDS + walletId, exception.getMessage());
        verify(walletRepository, times(1)).findByIdWithLock(walletId);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void testGetBalance_Success() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletBalanceResponse response = walletService.getBalance(walletId);

        assertEquals(walletId, response.getWalletId());
        assertEquals(1000, response.getBalance());
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    void testGetBalance_WalletNotFound() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        WalletException exception = assertThrows(WalletException.class, () -> walletService.getBalance(walletId));
        assertEquals(WALLET_NOT_FOUND + walletId, exception.getMessage());
        verify(walletRepository, times(1)).findById(walletId);
    }
}
