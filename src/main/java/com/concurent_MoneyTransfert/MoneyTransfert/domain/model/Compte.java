package com.concurent_MoneyTransfert.MoneyTransfert.domain.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
public class Compte {

    private Long id;
    private String titulaire;
    private double solde;
    @Version
    private Integer version;
    @Transient
    private AtomicInteger atomicSoldde = new AtomicInteger(0);
    private String numeroCompte;
    private double limiteDecouvert;
    private double tauxInteret;
    private LocalDate dateOuverture;
    private String statut;
    private String typeCompte; // "INDIVIDUEL", "POOL", "CENTRALISATEUR"

    // Seuils de trésorerie pour l'équilibrage
    private double seuilMinimum;
    private double seuilMaximum;

    private Compte compteCentralisateur;

    private Set<Compte> comptesParticipants = new HashSet<>();

    @PostLoad
    private void initAtomicSolde() {
        atomicSoldde.set((int) solde);
    }

    public boolean retirer(double montant) {
        double soldeActuel;
        do {
            soldeActuel = atomicSoldde.get();
            if (soldeActuel < montant) {
                return false;
            }

        } while (!atomicSoldde.compareAndSet((int) soldeActuel, (int) (soldeActuel - montant)));
        this.solde = atomicSoldde.get();
        return true;
    }

    public void deposer(double montant) {
        atomicSoldde.getAndAdd((int) montant);
        this.solde = atomicSoldde.get();
    }

    public void crediter(double montant) {
        this.solde += montant;
    }

    public void debiter(double montant) {

        if (this.solde > montant) {
            this.solde -= montant;
        } else {
            throw new IllegalArgumentException("Fonds insuffisant.");
        }
    }

    public boolean estDefict() {
        return this.solde < seuilMinimum;

    }

    public boolean estExcedentaire() {
        return this.solde > seuilMinimum;
    }
}
