package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence;

import java.util.HashSet;
import java.util.Set;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;

public class CompteMapper {

    // Convertit CompteEntity en Compte (domaine), avec gestion des relations cycliques
    public static Compte toDomain(CompteEntity compteEntity, Set<Long> convertedIds) {

        if (compteEntity == null || convertedIds.contains(compteEntity.getId())) {

            return null;  // evite la conversion si le compte est déjàa traité.
        }
        convertedIds.add(compteEntity.getId());
        Compte compte = new Compte();
        compte.setId(compteEntity.getId());
        compteEntity.setNumeroCompte(compteEntity.getNumeroCompte());
        compte.setTypeCompte(compteEntity.getTypeCompte());
        compte.setSolde(compteEntity.getSolde());
        compte.setStatut(compteEntity.getStatut());
        compte.setDateOuverture(compteEntity.getDateOuverture());
        compte.setLimiteDecouvert(compteEntity.getLimiteDecouvert());
        compte.setSeuilMaximum(compteEntity.getSeuilMaximum());
        compte.setSeuilMinimum(compteEntity.getSeuilMinimum());
        compte.setVersion(compteEntity.getVersion());
        //Conversion du compte centralisateur
        compte.setCompteCentralisateur(toDomain(compteEntity.getCompteCentralisateur(), convertedIds));
        if (compteEntity.getComptesParticipants() != null) {
            Set<Compte> participants = new HashSet<>();
            for (CompteEntity compteEntity1 : compteEntity.getComptesParticipants()) {
                participants.add(toDomain(compteEntity1, convertedIds));
            }
            compte.setComptesParticipants(participants);
        }
        return compte;
    }

    // Méthode d'assistance pour initialiser la conversion avec un Set de suivi vide
    public static Compte toDomain(CompteEntity compteEntity) {
        return toDomain(compteEntity, new HashSet<>());
    }

    // Convertit Compte (domaine) en CompteEntity
    public static CompteEntity toEntity(Compte compte, Set<Long> convertedIds) {
        if (compte == null || convertedIds.contains(compte.getId())) {

            return null; // Evite la reconverso=ion si le compte est déjà traité.
        }

        //Marque ce compte comme converti
        convertedIds.add(compte.getId());

        CompteEntity compteEntity = new CompteEntity();
        compteEntity.setId(compte.getId());
        compteEntity.setNumeroCompte(compte.getNumeroCompte());
        compteEntity.setSeuilMaximum(compte.getSeuilMaximum());
        compteEntity.setSeuilMinimum(compte.getSeuilMinimum());
        compteEntity.setTypeCompte(compte.getTypeCompte());
        compteEntity.setSolde(compte.getSolde());
        compteEntity.setStatut(compte.getStatut());
        compteEntity.setDateOuverture(compte.getDateOuverture());
        compteEntity.setLimiteDecouvert(compte.getLimiteDecouvert());
        compteEntity.setTauxInteret(compte.getTauxInteret());
        compteEntity.setVersion(compte.getVersion());
        compteEntity.setCompteCentralisateur(toEntity(compte.getCompteCentralisateur(), convertedIds));

        if (compte.getComptesParticipants() != null) {
            Set<CompteEntity> compteParticipantsEntities = new HashSet<>();

            for (Compte participant : compte.getComptesParticipants()) {

                compteParticipantsEntities.add(toEntity(participant, convertedIds));
            }
            compteEntity.setComptesParticipants(compteParticipantsEntities);
        }

        return compteEntity;
    }


    //Methode d'assistance pour initialiser la conversion avec un Set de  suivi vide
    public static CompteEntity toEntity(Compte compte) {

        return toEntity(compte, new HashSet<>());
    }
}
