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
import com.concurent_MoneyTransfert.MoneyTransfert.domain.adaptater.persistence.CompteMapper;
import com.concurent_MoneyTransfert.MoneyTransfert.domain.model.service.CashPoolingService;

@RestController
@RequestMapping("/api/pooling")
public class CashPoolingController {

    private final CashPoolingService cashPoolingService;

    @Autowired
    public CashPoolingController(final CashPoolingService cashPoolingService) {
        this.cashPoolingService = cashPoolingService;
    }


    @PostMapping("/creer")
    public CompteEntity creerCompte(@RequestBody CompteEntity compteEntity){
        return CompteMapper.toEntity( cashPoolingService.createCompte(compteEntity));
    }

    @GetMapping("/{id}")
    public Optional<CompteEntity> obtenirCompte(@PathVariable Long id){

        return Optional.ofNullable(CompteMapper.toEntity(cashPoolingService.obtenirCompte(id).get()));
    }

    @PostMapping("/transferer")
    public String transferer(@RequestParam Long idSource, @RequestParam Long idDestination, @RequestParam int montant){

        boolean reussi = cashPoolingService.transferer(idSource, idDestination, montant);
        return reussi ? "Transfert réussi " : " Echec du transfert.";
    }

    @PostMapping("/consolider/{idCentralisateur}")
    public ResponseEntity<String> consoliderSoldes(@PathVariable Long idCentralisateur){
        try {
            cashPoolingService.consoliderSoldes(idCentralisateur);
            return  ResponseEntity.ok("Consolidation des soldes réussi");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
