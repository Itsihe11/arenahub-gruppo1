package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.PartitaServices;

@RestController
@RequestMapping("/api/partite")
@CrossOrigin(origins = "http://localhost:4200")
public class PartitaController {

    @Autowired PartitaServices partitaService;

    public PartitaController(PartitaServices partitaService) {
        this.partitaService = partitaService;
    }


    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<Map<String, Object>>> getPartiteTorneo(@PathVariable Integer torneoId) {
        return ResponseEntity.ok(partitaService.getPartiteByTorneo(torneoId));
    }


    @PostMapping("/genera/{torneoId}")
    public ResponseEntity<List<Map<String, Object>>> generaTabellone(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @PathVariable Integer torneoId) {
        return ResponseEntity.ok(partitaService.generaTabellone(uid, torneoId));
    }


    @PutMapping("/{id}/risultato")
    public ResponseEntity<Map<String, Object>> salvaRisultato(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {

        Integer p1 = payload.get("punteggio1") != null ? Integer.parseInt(payload.get("punteggio1").toString()) : 0;
        Integer p2 = payload.get("punteggio2") != null ? Integer.parseInt(payload.get("punteggio2").toString()) : 0;
        String stato = payload.get("stato") != null ? payload.get("stato").toString() : "completata";

        return ResponseEntity.ok(partitaService.salvaRisultato(uid, id, p1, p2, stato));
    }
    
    @DeleteMapping("/reset/{torneoId}")
    public ResponseEntity<List<Map<String, Object>>> resetTabellone(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @PathVariable Integer torneoId) {
        return ResponseEntity.ok(partitaService.resetTabellone(uid, torneoId));
    }
}