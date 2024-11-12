package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out.CompteCashPoolingRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class JpaCompteRepository implements CompteCashPoolingRepository {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Optional<Compte> getCompte(final Long id) {
        CompteEntity compteEntity = entityManager.find(CompteEntity.class, id);
        return Optional.ofNullable(CompteMapper.toDomain(compteEntity));
    }

    @Override
    public Set<Compte> getComptesParticipants(final Long idCentralisateur) {
        Set<CompteEntity> compteEntities = new HashSet<>(entityManager.createQuery("SELECT c FROM compteEntity c WHERE c.compteCentralisateur.id = :id", CompteEntity.class)
                .setParameter("id", idCentralisateur)
                .getResultList());

        return compteEntities.stream().map(CompteMapper::toDomain).collect(Collectors.toSet());
    }

    @Override
    public Compte save(final Compte compte) {

        //Convertit compte en CompteEntity via CompteMapper
        CompteEntity compteEntity = CompteMapper.toEntity(compte);

        // Sauvegarder ou met à jour l'entité dans la BDD
        if (compteEntity.getId() == null) {
            entityManager.persist(compteEntity); // enregiste un nouveau compte.
        } else {
            compteEntity = entityManager.merge(compteEntity); // Met à jour un compte existant
        }

        // Retourne l'objet Compte à partir de l'entité sauvegarder en base.
        return CompteMapper.toDomain(compteEntity);
    }

    @Override
    public Optional<Compte> getCentralisateur() {
        return entityManager.createQuery("SELECT c FROM CompteEntity c WHERE c.typeCompte = :typeCompte", CompteEntity.class)
                .setParameter("typeCompte", "CENTRALISATEUR").setMaxResults(1).getResultList().stream().findFirst().map(CompteMapper::toDomain);
    }
}
