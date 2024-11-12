package com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteEntity;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteMapper;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.JpaCompteRepository;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.in.CashPoolingUseCase;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.port.out.TransactionService;

@Service
public class CashPoolingService implements CashPoolingUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CashPoolingService.class);

    private final JpaCompteRepository jpaCompteRepository;

    private final TransactionService transactionService;

    public CashPoolingService(JpaCompteRepository jpaCompteRepository, TransactionService transactionService) {
        this.jpaCompteRepository = jpaCompteRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public boolean transferer(Long idSource, Long idDestination, int montant) {
        logger.info("Début du transfert de {} euros de {} à {}", montant, idSource, idDestination);
        Optional<Compte> compteSourceOpt = jpaCompteRepository.getCompte(idSource);
        Optional<Compte> compteDestinationOpt = jpaCompteRepository.getCompte(idDestination);

        // Utiliser la méthode CAS pour effectuer le transfert de manière sécurisée
        if (compteSourceOpt.isPresent() && compteDestinationOpt.isPresent()) {
            Compte compteSource = compteSourceOpt.get();
            Compte compteDestination = compteDestinationOpt.get();
            if (compteSource.retirer(montant)) {
                compteDestination.deposer(montant);
                jpaCompteRepository.save(compteDestination);
                jpaCompteRepository.save(compteSource);
                logger.info("Fin du transfert de {} de {} à {}", montant, compteSource.getTitulaire(),compteDestination.getTitulaire());
                return true;
            }
        }
        return false; // Transfert échoué(fonds insuffisants ou compte introuvable)
    }

    @Transactional
    public Compte createCompte(CompteEntity compteEntity) {

        return jpaCompteRepository.save(CompteMapper.toDomain(compteEntity));
    }

    public Optional<Compte> obtenirCompte(Long id) {
        return jpaCompteRepository.getCompte(id);
    }

    private void appliquerSurChaqueCompteParticipant(Long idCentralisateur, BiConsumer<Compte, Compte> operation) {

        Optional<Compte> centralisateur = jpaCompteRepository.getCompte(idCentralisateur);

        if (centralisateur.isEmpty()) {
            throw new IllegalArgumentException("Compte centralisateur introuvable.");
        }

        for (Compte participant : jpaCompteRepository.getComptesParticipants(idCentralisateur)) {
            operation.accept(centralisateur.get(), participant);
            jpaCompteRepository.save(participant);
        }
        jpaCompteRepository.save(centralisateur.get());
    }

    public double calculerInteretTotal(Long idCentralisateur, Function<Compte, Double> calculInterert) {
        Optional<Compte> centralisateur = jpaCompteRepository.getCompte(idCentralisateur); // Applique la fonction de calcul
        return centralisateur.get().getComptesParticipants().stream().map(calculInterert).reduce(0.0, Double::sum); // Somme les intérêts
    }

    @Transactional
    public void consoliderSoldes(Long idCentralisateur){
        Optional<Compte> centralisateur = jpaCompteRepository.getCompte(idCentralisateur);
        if (!"CENTRALISATEUR".equals(centralisateur.get().getTypeCompte())){
            throw  new IllegalArgumentException("Le compte n'est pas un centralisateur.");
        }

        double totalSolde = centralisateur.get().getSolde();

        for (Compte participant: centralisateur.get().getComptesParticipants()){
            totalSolde += participant.getSolde();
            appliquerSurChaqueCompteParticipant(idCentralisateur, (c, p) -> {
                // Logique à appliquer sur chaque participant
                double montant = calculMontantAjuste(c, p);
                c.debiter(montant);
                p.crediter(montant);
            });
            participant.setSolde(0); // Transfert du solde vers le centralisateur
            jpaCompteRepository.save(participant);
        }

        centralisateur.get().setSolde(totalSolde);
        jpaCompteRepository.save(centralisateur.get());
    }

    private double calculMontantAjuste(final Compte compte, final Compte participant) {
        double soldeCentralisateur = compte.getSolde();
        double soldeParticipant = participant.getSolde();

        // Calcul de la différence de solde entre le centralisateur et le participant
        double differenceSolde = soldeCentralisateur - soldeParticipant;

        // On applique la logique d'ajustement ici, par exemple, une proportion de la différence de solde
        // Si le centralisateur a un solde excédentaire par rapport au participant, on ajuste en conséquence.
        // Par exemple, si la différence est positive, on pourrait débiter du centralisateur et créditer le participant.

        // Exemple simple: on répartit la différence proportionnellement (calcul arbitraire pour démonstration)
        double montantAjuste = 0;
        if (differenceSolde > 0) {
            // On débite le centralisateur et on crédite le participant proportionnellement à la différence
            montantAjuste = differenceSolde * 0.1;  // Exemple d'ajustement à 10% de la différence
        } else if (differenceSolde < 0) {
            // Si le participant a un solde excédentaire, on pourrait ajuster l'autre sens
            montantAjuste = differenceSolde * -0.1; // Exemple d'ajustement négatif à 10% de la différence
        }

        // On renvoie le montant ajusté à appliquer
        return montantAjuste;
    }

    public  void equilibrerCompte(Long idCentralisateur){
        transactionService.executeTransaction(()->{
            Optional<Compte> compteCentralisateur = jpaCompteRepository.getCompte(idCentralisateur);

            for (Compte participant: jpaCompteRepository.getComptesParticipants(idCentralisateur)){
                if (participant.estDefict()){

                    double montant = participant.getSeuilMaximum()- participant.getSolde();
                    compteCentralisateur.get().debiter(montant);
                } else if (participant.estExcedentaire()) {
                    double montant = participant.getSolde()- participant.getSeuilMaximum();

                    participant.debiter(montant);
                    compteCentralisateur.get().crediter(montant);
                    jpaCompteRepository.save(participant);

                }
            }
            jpaCompteRepository.save(compteCentralisateur.get());
        });
    }
}
