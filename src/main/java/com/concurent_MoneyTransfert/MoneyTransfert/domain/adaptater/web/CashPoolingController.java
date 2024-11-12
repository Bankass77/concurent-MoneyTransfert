package com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.web;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteEntity;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service.CashPoolingService;
/**
 * Contrôleur REST pour gérer les opérations de cash pooling et de transfert entre comptes.
 * Fournit des points d'entrée pour créer, obtenir et transférer des comptes, ainsi que pour consolider les soldes.
 */
@RestController
@RequestMapping("/api/pooling")
public class CashPoolingController {

    private final CashPoolingService cashPoolingService;

    /**
     * Constructeur pour injecter le service de cash pooling.
     *
     * @param cashPoolingService le service de cash pooling gérant la logique métier des opérations de compte
     */
    @Autowired
    public CashPoolingController(final CashPoolingService cashPoolingService) {
        this.cashPoolingService = cashPoolingService;
    }

    /**
     * Crée un nouveau compte en utilisant les informations fournies dans le corps de la requête.
     *
     * @param compteEntity les informations du compte à créer
     * @return le compte créé sous forme d'entité CompteEntity
     */
    @PostMapping("/creer")
    public Compte creerCompte(@RequestBody CompteEntity compteEntity) {
        return cashPoolingService.createCompte(compteEntity);
    }

    /**
     * Récupère un compte spécifique par son ID.
     *
     * @param id l'ID du compte à récupérer
     * @return un Optional contenant l'entité CompteEntity du compte si elle est trouvée, sinon un Optional vide
     */
    @GetMapping("/{id}")
    public Optional<Compte> obtenirCompte(@PathVariable Long id) {

        if (cashPoolingService.obtenirCompte(id).isPresent()){
            return Optional.of(cashPoolingService.obtenirCompte(id).get());
        }

        return Optional.empty();
    }

    /**
     * Effectue un transfert d'un montant donné entre deux comptes spécifiés.
     *
     * @param idSource l'ID du compte source
     * @param idDestination l'ID du compte de destination
     * @param montant le montant à transférer
     * @return un message indiquant si le transfert a été réussi ou échoué
     */
    @PostMapping("/transferer")
    public String transferer(@RequestParam Long idSource, @RequestParam Long idDestination, @RequestParam int montant) {
        boolean reussi = cashPoolingService.transferer(idSource, idDestination, montant);
        return reussi ? "Transfert réussi" : "Échec du transfert.";
    }

    /**
     * Consolide les soldes de tous les comptes participants en les transférant vers un compte centralisateur.
     *
     * @param idCentralisateur l'ID du compte centralisateur
     * @return une réponse HTTP avec un message de succès ou un message d'erreur en cas d'échec
     */
    @PostMapping("/consolider/{idCentralisateur}")
    public ResponseEntity<String> consoliderSoldes(@PathVariable Long idCentralisateur) {
        try {
            cashPoolingService.consoliderSoldes(idCentralisateur);
            return ResponseEntity.ok("Consolidation des soldes réussie");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
