package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "compte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulaire;
    private double solde;
    private String numeroCompte;
    private double limiteDecouvert;
    private double tauxInteret;
    private LocalDate dateOuverture;
    private String statut;
    private String typeCompte;
    private double seuilMinimum;
    private double seuilMaximum;

    @Version
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "centralisateur_id")
    private CompteEntity compteCentralisateur;

    @OneToMany(mappedBy = "compteCentralisateur")
    private Set<CompteEntity> comptesParticipants = new HashSet<>();
}