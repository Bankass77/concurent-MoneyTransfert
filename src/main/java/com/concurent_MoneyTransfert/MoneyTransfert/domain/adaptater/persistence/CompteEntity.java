package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "compte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // Active l'inclusion explicite des champs
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include  // Inclut seulement l'ID dans equals et hashCode
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
    // @JsonBackReference  // Empêche la sérialisation récursive vers le parent
   // @JsonIgnore
    private CompteEntity compteCentralisateur;

    @OneToMany(mappedBy = "compteCentralisateur")
   // @JsonManagedReference  // Indique que cette collection est la relation "enfant"
    private Set<CompteEntity> comptesParticipants = new HashSet<>();

}