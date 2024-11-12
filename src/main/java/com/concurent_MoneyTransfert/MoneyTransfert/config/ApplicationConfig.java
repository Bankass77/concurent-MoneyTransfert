package com.concurent_MoneyTransfert.MoneyTransfert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.JpaCompteRepository;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service.CashPoolingService;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service.TransactionServiceImpl;

@Configuration
public class ApplicationConfig {

    @Bean
    public CashPoolingService cashPoolingService(JpaCompteRepository jpaCompteRepository, TransactionServiceImpl transactionService){
        return new CashPoolingService(jpaCompteRepository, transactionService);
    }
}
