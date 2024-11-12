package com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out;

import java.util.Optional;
import java.util.Set;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;

public interface CompteCashPoolingRepository {
    Optional<Compte> getCompte(Long id);
    Set<Compte> getComptesParticipants(Long idCentralisateur);
    Compte save(Compte compte);
    public Optional<Compte> getCentralisateur();
}
