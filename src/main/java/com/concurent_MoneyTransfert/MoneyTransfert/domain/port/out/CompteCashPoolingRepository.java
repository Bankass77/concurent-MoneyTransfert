package com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out;

import java.util.Optional;
import java.util.Set;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteEntity;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;

/**
 * Interface de repository pour les opérations de cash pooling sur les comptes.
 * Définit les méthodes nécessaires pour manipuler les comptes dans un système de cash pooling,
 * incluant les opérations de récupération, de mise à jour, et de recherche de comptes centralisateurs et participants.
 */
public interface CompteCashPoolingRepository {

    /**
     * Récupère un compte spécifique à partir de son ID.
     *
     * @param id l'ID du compte à récupérer
     * @return un Optional contenant l'entité CompteEntity si le compte est trouvé, sinon un Optional vide
     */
    Optional<Compte> getCompte(Long id);

    /**
     * Récupère tous les comptes participants associés à un compte centralisateur donné.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     * @return un ensemble de comptes participants sous forme de CompteEntity associés au centralisateur
     */
    Set<Compte> getComptesParticipants(Long idCentralisateur);

    /**
     * Sauvegarde un compte dans le système.
     * Insère un nouveau compte si l'ID est absent, ou met à jour un compte existant si un ID est présent.
     *
     * @param compteEntity l'entité CompteEntity à sauvegarder
     * @return l'entité CompteEntity sauvegardée
     */
    Compte save(CompteEntity compteEntity);

    /**
     * Récupère un compte centralisateur dans le système.
     * Si plusieurs comptes centralisateurs existent, retourne le premier trouvé.
     *
     * @return un Optional contenant un compte centralisateur sous forme de CompteEntity s'il est trouvé, sinon un Optional vide
     */
    Optional<Compte> getCentralisateur();
}
