package com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteEntity;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteMapper;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.JpaCompteRepository;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.in.CashPoolingUseCase;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out.TransactionService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour gérer les opérations de cash pooling et les transferts entre comptes.
 * Implémente l'interface CashPoolingUseCase et inclut les fonctionnalités de transfert, consolidation
 * de solde, calcul des intérêts, et équilibrage des comptes entre les centralisateurs et les participants.
 */
@Service
@Slf4j
public class CashPoolingService implements CashPoolingUseCase {

    private final JpaCompteRepository jpaCompteRepository;
    private final TransactionService transactionService;

    /**
     * Constructeur pour injecter les dépendances nécessaires.
     *
     * @param jpaCompteRepository le repository pour gérer les comptes
     * @param transactionService le service pour gérer les transactions
     */
    public CashPoolingService(JpaCompteRepository jpaCompteRepository, TransactionService transactionService) {
        this.jpaCompteRepository = jpaCompteRepository;
        this.transactionService = transactionService;
    }

    /**
     * Transfère un montant spécifié d'un compte source vers un compte de destination.
     *
     * @param idSource l'ID du compte source
     * @param idDestination l'ID du compte de destination
     * @param montant le montant à transférer
     * @return true si le transfert est réussi, false en cas d'échec (fonds insuffisants ou comptes introuvables)
     */
    @Transactional
    public boolean transferer(Long idSource, Long idDestination, int montant) {
        log.info("Début du transfert de {} euros de {} à {}", montant, idSource, idDestination);
        Optional<Compte> compteSourceOpt = jpaCompteRepository.getCompte(idSource);
        Optional<Compte> compteDestinationOpt = jpaCompteRepository.getCompte(idDestination);

        if (compteSourceOpt.isPresent() && compteDestinationOpt.isPresent()) {
            CompteEntity compteSource = CompteMapper.toEntity(compteSourceOpt.get());
            CompteEntity compteDestination =CompteMapper.toEntity( compteDestinationOpt.get());
            if (CompteMapper.toDomain(compteSource).retirer(montant)) {
                CompteMapper.toDomain(compteDestination).deposer(montant);
                jpaCompteRepository.save(compteDestination);
                jpaCompteRepository.save(compteSource);
                log.info("Fin du transfert de {} de {} à {}", montant, compteSource.getTitulaire(), compteDestination.getTitulaire());
                return true;
            }
        }
        return false;
    }

    /**
     * Crée un nouveau compte ou met à jour un compte existant.
     *
     * @param compteEntity l'entité du compte à créer ou mettre à jour
     * @return l'entité du compte enregistrée
     */
    @Transactional
    public Compte createCompte(CompteEntity compteEntity) {
        return creerOuMettreAJourCompte(compteEntity);
    }

    /**
     * Obtient un compte par son ID.
     *
     * @param id l'ID du compte
     * @return un Optional contenant le compte s'il est trouvé, sinon un Optional vide
     */
    public Optional<Compte> obtenirCompte(Long id) {
        return jpaCompteRepository.getCompte(id);
    }

    /**
     * Applique une opération sur chaque compte participant d'un centralisateur donné.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     * @param operation l'opération à appliquer sur chaque compte participant
     */
    private void appliquerSurChaqueCompteParticipant(Long idCentralisateur, BiConsumer<CompteEntity, CompteEntity> operation) {
        Optional<Compte> centralisateur = jpaCompteRepository.getCompte(idCentralisateur);

        if (centralisateur.isEmpty()) {
            throw new IllegalArgumentException("Compte centralisateur introuvable.");
        }

        for (Compte participant : jpaCompteRepository.getComptesParticipants(idCentralisateur)) {
            operation.accept(CompteMapper.toEntity(centralisateur.get()), CompteMapper.toEntity(participant));
            creerOuMettreAJourCompte( CompteMapper.toEntity(participant));
        }
        creerOuMettreAJourCompte(CompteMapper.toEntity(centralisateur.get()));
    }

    /**
     * Calcule l'intérêt total accumulé par tous les comptes participants d'un centralisateur.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     * @param calculInterert la fonction de calcul d'intérêt appliquée sur chaque participant
     * @return la somme des intérêts calculés
     */
    public double calculerInteretTotal(Long idCentralisateur, Function<Compte, Double> calculInterert) {
        Optional<Compte> centralisateur = jpaCompteRepository.getCompte(idCentralisateur);
        if (centralisateur.isPresent()){
            return centralisateur.get().getComptesParticipants().stream()
                    .map(calculInterert)
                    .reduce(0.0, Double::sum);
        }
        return 0;
    }

    /**
     * Consolide les soldes de tous les comptes participants en les transférant vers le compte centralisateur.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     */
    @Transactional
    public void consoliderSoldes(Long idCentralisateur) {
        Optional<Compte> centralisateur = jpaCompteRepository.getCompte(idCentralisateur);
        if (centralisateur.isPresent()){
            if (!"CENTRALISATEUR".equals(centralisateur.get().getTypeCompte())) {
                throw new IllegalArgumentException("Le compte n'est pas un centralisateur.");
            }
        }
        double totalSolde = 0.0;
       if (centralisateur.isPresent()){
            totalSolde= centralisateur.get().getSolde();
           for (Compte participant : centralisateur.get().getComptesParticipants()) {
               totalSolde += participant.getSolde();
               participant.setSolde(0);
               creerOuMettreAJourCompte(CompteMapper.toEntity(participant));
           }
           centralisateur.get().setSolde(totalSolde);
           creerOuMettreAJourCompte(CompteMapper.toEntity(centralisateur.get()));
       }
    }

    /**
     * Calcule un montant d'ajustement basé sur la différence de solde entre le centralisateur et le participant.
     *
     * @param compteCentralisateur le compte centralisateur
     * @param participant le compte participant
     * @return le montant ajusté
     */
    private double calculMontantAjuste(final CompteEntity compteCentralisateur, final CompteEntity participant) {
        double soldeCentralisateur = compteCentralisateur.getSolde();
        double soldeParticipant = participant.getSolde();
        double differenceSolde = soldeCentralisateur - soldeParticipant;
        double montantAjuste = 0;
        if (differenceSolde > 0) {
            montantAjuste = differenceSolde * 0.1;
        } else if (differenceSolde < 0) {
            montantAjuste = differenceSolde * -0.1;
        }
        return montantAjuste;
    }

    /**
     * Équilibre le compte centralisateur en ajustant les soldes de chaque participant selon leur statut.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     */
    public void equilibrerCompte(Long idCentralisateur) {
        transactionService.executeTransaction(() -> {
            Optional<Compte> compteCentralisateur = jpaCompteRepository.getCompte(idCentralisateur);

            for (Compte participant : jpaCompteRepository.getComptesParticipants(idCentralisateur)) {
                if (participant.estDefict() && compteCentralisateur.isPresent()) {
                    double montant = participant.getSeuilMaximum() - participant.getSolde();
                    compteCentralisateur.get().debiter(montant);
                } else if (participant.estExcedentaire() && compteCentralisateur.isPresent()) {
                    double montant = participant.getSolde() - participant.getSeuilMaximum();
                    participant.debiter(montant);
                    compteCentralisateur.get().crediter(montant);
                    creerOuMettreAJourCompte(CompteMapper.toEntity(participant));
                }
            }
            compteCentralisateur.ifPresent(compte -> creerOuMettreAJourCompte(CompteMapper.toEntity(compte)));
        });
    }

    /**
     * Crée ou met à jour un compte en établissant les relations avec le centralisateur si nécessaire.
     *
     * @param compteEntity l'entité du compte à enregistrer
     * @return le compte enregistré
     */
    public Compte creerOuMettreAJourCompte(CompteEntity compteEntity) {
        if (compteEntity.getCompteCentralisateur() != null && compteEntity.getCompteCentralisateur().getId() != null) {
            Compte centralisateur = jpaCompteRepository.getCompte(compteEntity.getCompteCentralisateur().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Compte centralisateur introuvable."));
            compteEntity.setCompteCentralisateur(CompteMapper.toEntity(centralisateur));
        }
        return jpaCompteRepository.save(compteEntity);
    }
}
