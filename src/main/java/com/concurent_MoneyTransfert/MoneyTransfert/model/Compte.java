package com.concurent_MoneyTransfert.MoneyTransfert.model;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "compte")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulaire;
    private int solde;
    @Version
    private Integer version;
    @Transient
    private AtomicInteger atomicSoldde = new AtomicInteger(0);

@PostLoad
private void initAtomicSolde(){
    atomicSoldde.set(solde);
}
    public boolean retirer(int montant){
        int soldeActuel;
        do {
            soldeActuel = atomicSoldde.get();
            if (soldeActuel < montant){
                return  false;
            }

        }while (!atomicSoldde.compareAndSet(soldeActuel, soldeActuel -montant));
        this.solde=atomicSoldde.get();
        return true;
    }

    public void deposer( int montant){
        atomicSoldde.getAndAdd(montant);
        this.solde= atomicSoldde.get();
    }
}
