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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.IscrizioneService;
import com.example.backend.Services.UtenteService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class IscrizioneController {

    private final IscrizioneService service;
    private final UtenteService utenti;

    public IscrizioneController(IscrizioneService service, UtenteService utenti) {
        this.service = service;
        this.utenti = utenti;
    }

    @PostMapping("/tornei/{id}/iscrizione")
    public ResponseEntity<Map<String, Object>> iscrivi(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable int id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.iscrivi(utenti.idDaHeader(auth), id));
    }

    @DeleteMapping("/tornei/{id}/iscrizione")
    public Map<String, Object> annulla(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable int id) {
        return service.annulla(utenti.idDaHeader(auth), id);
    }

    @GetMapping("/utenti/me/iscrizioni")
    public List<Map<String, Object>> mie(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return service.mieIscrizioni(utenti.idDaHeader(auth));
    }

    @GetMapping("/tornei/{id}/partecipanti")
    public Map<String, Object> partecipanti(@PathVariable int id) {
        return service.partecipanti(id);
    }
}