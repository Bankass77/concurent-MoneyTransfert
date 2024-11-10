package com.concurent_MoneyTransfert.MoneyTransfert.service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.concurent_MoneyTransfert.MoneyTransfert.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.repository.CompteRepository;

@Service
public class CompteService {

    private static final Logger logger = LoggerFactory.getLogger(CompteService.class);
    private final CompteRepository compteRepository;

    @Autowired
    public CompteService(final CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    @Transactional
    public boolean transferer(Long idSource, Long idDestination, int montant) {
        logger.info("Début du transfert de {} euros de {} à {}", montant, idSource, idDestination);
        Optional<Compte> compteSourceOpt = compteRepository.findById(idSource);
        Optional<Compte> compteDestinationOpt = compteRepository.findById(idDestination);

        // Utiliser la méthode CAS pour effectuer le transfert de manière sécurisée
        if (compteSourceOpt.isPresent() && compteDestinationOpt.isPresent()) {
            Compte compteSource = compteSourceOpt.get();
            Compte compteDestination = compteDestinationOpt.get();
            if (compteSource.retirer(montant)) {
                compteDestination.deposer(montant);
                compteRepository.save(compteDestination);
                compteRepository.save(compteSource);
                logger.info("Fin du transfert de {} de {} à {}", montant, compteSource.getTitulaire(),compteDestination.getTitulaire());
                return true;
            }
        }
        return false; // Transfert échoué(fonds insuffisants ou compte introuvable)
    }

    public Compte createCompte(String titulaire, int soldeInitial) {
        Compte compte = new Compte();
        compte.setTitulaire(titulaire);
        compte.setSolde(soldeInitial);
        compteRepository.save(compte);
        return compte;
    }

    public Optional<Compte> obtenirCompte(Long id) {
        return compteRepository.findById(id);
    }
}
