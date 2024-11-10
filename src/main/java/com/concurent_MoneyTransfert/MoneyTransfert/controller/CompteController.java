package com.concurent_MoneyTransfert.MoneyTransfert.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.concurent_MoneyTransfert.MoneyTransfert.model.Compte;
import com.concurent_MoneyTransfert.MoneyTransfert.service.CompteService;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    private final CompteService compteService;

    @Autowired
    public CompteController(final CompteService compteService) {
        this.compteService = compteService;
    }

    @PostMapping("/creer")
    public Compte creerCompte(@RequestParam String titulaire, @RequestParam int soldeInitial){
        return  compteService.createCompte(titulaire, soldeInitial);
    }

    @GetMapping("/{id}")
    public Optional<Compte> obtenirCompte(@PathVariable Long id){

        return compteService.obtenirCompte(id);
    }

    @PostMapping("/transferer")
    public String transferer(@RequestParam Long idSource, @RequestParam Long idDestination, @RequestParam int montant){

        boolean reussi = compteService.transferer(idSource, idDestination, montant);
        return reussi ? "Transfert réussi " : " Echec du transfert.";
    }
}
