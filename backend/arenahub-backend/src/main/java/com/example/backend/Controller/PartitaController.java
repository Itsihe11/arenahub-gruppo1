package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.PartitaServices;
import com.example.backend.Services.UtenteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/partite")
@CrossOrigin(origins = "http://localhost:4200")
public class PartitaController {

    private final PartitaServices partitaService;
    private final UtenteService utenti;

    public PartitaController(PartitaServices partitaService, UtenteService utenti) {
        this.partitaService = partitaService;
        this.utenti = utenti;
    }

    @GetMapping("/torneo/{torneoId}")
    public List<Map<String, Object>> getPartiteTorneo(@PathVariable Integer torneoId) {
        return partitaService.getPartiteByTorneo(torneoId);
    }

    @PostMapping("/genera/{torneoId}")
    public ResponseEntity<List<Map<String, Object>>> generaTabellone(@Valid
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer torneoId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(partitaService.generaTabellone(utenti.idDaHeader(auth), torneoId));
    }

    @PutMapping("/{id}/risultato")
    public Map<String, Object> salvaRisultato(@Valid
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        return partitaService.salvaRisultato(utenti.idDaHeader(auth), id, payload);
    }

    @DeleteMapping("/reset/{torneoId}")
    public List<Map<String, Object>> resetTabellone(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer torneoId) {
        return partitaService.resetTabellone(utenti.idDaHeader(auth), torneoId);
    }
}