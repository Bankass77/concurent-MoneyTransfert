package com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service;

import org.springframework.stereotype.Service;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Override
    public void executeTransaction(final Runnable action) {
        action.run(); // Exécute le bloc de code dans une transaction
    }
}
