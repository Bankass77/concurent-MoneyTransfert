package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.JpaCompteRepository;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.in.CashPoolingUseCase;

@Component
public class cashPoolingScheduler {
    private final CashPoolingUseCase cashPoolingUseCase;

    private final JpaCompteRepository jpaCompteRepository;

    public cashPoolingScheduler(final CashPoolingUseCase cashPoolingUseCase, JpaCompteRepository jpaCompteRepository) {
        this.cashPoolingUseCase = cashPoolingUseCase;
        this.jpaCompteRepository = jpaCompteRepository;
    }

    @Scheduled(cron = "${cashpooling.equilibrage.cron}")
    public void equilibrerCompteQuotidiennement() {
        Long idCentralisateur = obtenirIdCompteCentralisateur();
        cashPoolingUseCase.equilibrerCompte(idCentralisateur);
    }

    private Long obtenirIdCompteCentralisateur() {

        return jpaCompteRepository.getCentralisateur().map(Compte::getId)
                .orElseThrow(() -> new IllegalStateException("Compte centralisateur introuvable"));
    }


}
