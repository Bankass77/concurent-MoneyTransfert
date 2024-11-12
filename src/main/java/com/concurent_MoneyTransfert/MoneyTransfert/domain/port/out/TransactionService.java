package com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out;

public interface TransactionService {
    void executeTransaction(Runnable action);
}
