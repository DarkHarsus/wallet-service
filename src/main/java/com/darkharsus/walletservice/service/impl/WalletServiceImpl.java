package com.darkharsus.walletservice.service.impl;

import com.darkharsus.walletservice.dto.WalletBalanceResponse;
import com.darkharsus.walletservice.dto.WalletOperationRequest;
import com.darkharsus.walletservice.entity.Wallet;
import com.darkharsus.walletservice.exception.WalletException;
import com.darkharsus.walletservice.repository.WalletRepository;
import com.darkharsus.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotSerializeTransactionException;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.darkharsus.walletservice.constants.Constants.INSUFFICIENT_FUNDS;
import static com.darkharsus.walletservice.constants.Constants.INVALID_OPERATION_TYPE;
import static com.darkharsus.walletservice.constants.Constants.INVALID_OPERATION_TYPE_NULL;
import static com.darkharsus.walletservice.constants.Constants.WALLET_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(
            retryFor = {
                    JpaOptimisticLockingFailureException.class,
                    CannotSerializeTransactionException.class,
                    DataAccessException.class
            },
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, maxDelay = 1000, multiplier = 2, random = true)
    )
    public void performOperation(WalletOperationRequest request) {
        log.debug("Выполнение операции для walletId: {}, type: {}, amount: {}",
                request.getWalletId(), request.getOperationType(), request.getAmount());
        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletException(WALLET_NOT_FOUND + request.getWalletId()));

        if (request.getOperationType() == null) {
            throw new WalletException(INVALID_OPERATION_TYPE_NULL);
        }

        switch (request.getOperationType()) {
            case DEPOSIT:
                wallet.setBalance(wallet.getBalance() + request.getAmount());
                break;
            case WITHDRAW:
                if (wallet.getBalance() < request.getAmount()) {
                    throw new WalletException(INSUFFICIENT_FUNDS + request.getWalletId());
                }
                wallet.setBalance(wallet.getBalance() - request.getAmount());
                break;
            default:
                throw new WalletException(INVALID_OPERATION_TYPE + request.getOperationType());
        }
        walletRepository.save(wallet);
        log.debug("Операция завершена для walletId: {}, new balance: {}",
                request.getWalletId(), wallet.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException(WALLET_NOT_FOUND + walletId));

        return new WalletBalanceResponse(walletId, wallet.getBalance());
    }
}
