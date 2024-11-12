package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out.CompteCashPoolingRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Repository JPA pour gérer les opérations de persistance des comptes dans le système de cash pooling.
 * Implémente l'interface CompteCashPoolingRepository et fournit des méthodes pour créer, mettre à jour,
 * et rechercher des comptes dans la base de données.
 */
@Repository
public class JpaCompteRepository implements CompteCashPoolingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Récupère un compte à partir de son ID.
     *
     * @param id l'ID du compte à récupérer
     * @return un Optional contenant le compte sous forme d'entité CompteEntity si trouvé, sinon un Optional vide
     */
    @Override
    public Optional<Compte> getCompte(final Long id) {
        CompteEntity compteEntity = entityManager.find(CompteEntity.class, id);
        return Optional.ofNullable(CompteMapper.toDomain(compteEntity));
    }

    /**
     * Récupère tous les comptes participants associés à un compte centralisateur donné.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     * @return un ensemble de comptes participants sous forme de CompteEntity associés au centralisateur
     */
    @Override
    public Set<Compte> getComptesParticipants(final Long idCentralisateur) {

        Set<CompteEntity> compteEntities = new HashSet<>(entityManager.createQuery(
                        "SELECT c FROM CompteEntity c WHERE c.compteCentralisateur.id = :id", CompteEntity.class)
                .setParameter("id", idCentralisateur)
                .getResultList());
        Set<Compte> comptes= new HashSet<>();
        for ( CompteEntity compteEntity: compteEntities){
            Compte compte = CompteMapper.toDomain(compteEntity);
            comptes.add(compte);
        }
        return  comptes ;
    }

    /**
     * Sauvegarde un compte en base de données. Si le compte n'a pas d'ID, il est inséré en tant que nouveau compte.
     * Si un ID est présent, le compte est mis à jour.
     *
     * @param compteEntity le compte à sauvegarder
     * @return le compte sauvegardé sous forme d'entité CompteEntity
     */
    @Override
    public Compte save(CompteEntity compteEntity) {
        if (compteEntity.getId() == null) {
            entityManager.persist(compteEntity); // Enregistrement d’un nouveau compte
        } else {
            compteEntity = entityManager.merge(compteEntity); // Mise à jour d'un compte existant
        }
        return CompteMapper.toDomain(compteEntity);
    }

    /**
     * Récupère un compte centralisateur dans le système.
     * Si plusieurs comptes centralisateurs existent, le premier trouvé est retourné.
     *
     * @return un Optional contenant un compte centralisateur sous forme de CompteEntity si trouvé, sinon un Optional vide
     */
    @Override
    public Optional<Compte> getCentralisateur() {
        return Optional.ofNullable(CompteMapper.toDomain(entityManager.createQuery(
                        "SELECT c FROM CompteEntity c WHERE c.typeCompte = :typeCompte", CompteEntity.class)
                .setParameter("typeCompte", "CENTRALISATEUR")
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst().get()));
    }
}
