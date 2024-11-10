package com.concurent_MoneyTransfert.MoneyTransfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.concurent_MoneyTransfert.MoneyTransfert.model.Compte;

@Repository
public interface CompteRepository  extends JpaRepository<Compte, Long> {
}
