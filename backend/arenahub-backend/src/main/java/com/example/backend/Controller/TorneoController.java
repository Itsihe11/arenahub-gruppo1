package com.example.backend.Controller;

import java.time.LocalDate;
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

import com.example.backend.Model.Torneo;
import com.example.backend.Services.TorneoService;

@RestController
@RequestMapping("/api/tornei")
@CrossOrigin(origins = "http://localhost:4200")
public class TorneoController {

    @Autowired TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    public List<Map<String, Object>> elenco(@RequestParam(required = false) String stato) {
        return torneoService.elenco(stato);
    }

    @GetMapping("/{id}")
    public Map<String, Object> dettaglio(@PathVariable int id) {
        return torneoService.dettaglio(id);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> crea(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @RequestBody Map<String, Object> payload) {

        Torneo t = convertiPayloadInTorneo(payload);
        Map<String, Object> risultato = torneoService.crea(uid, t);
        return ResponseEntity.ok(risultato);
    }


    @PostMapping("/crea")
    public ResponseEntity<Map<String, Object>> creaSottopercorso(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @RequestBody Map<String, Object> payload) {
        return crea(uid, payload);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modifica(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @PathVariable int id,
            @RequestBody Map<String, Object> payload) {

        Torneo t = convertiPayloadInTorneo(payload);
        Map<String, Object> risultato = torneoService.modifica(uid, id, t);
        return ResponseEntity.ok(risultato);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(
            @RequestParam(required = false, defaultValue = "1") Integer uid,
            @PathVariable int id) {

        torneoService.elimina(uid, id);
        return ResponseEntity.noContent().build();
    }


    private Torneo convertiPayloadInTorneo(Map<String, Object> p) {
        Torneo t = new Torneo();

        if (p.get("nome") != null) t.setNome(p.get("nome").toString());
        if (p.get("descrizione") != null) t.setDescrizione(p.get("descrizione").toString());
        if (p.get("gioco") != null) t.setGioco(p.get("gioco").toString());

        
        String stato = (p.get("stato") != null && !p.get("stato").toString().isBlank())
                ? p.get("stato").toString()
                : "aperto";
        t.setStato(stato);

      
        Object max = p.get("postiMax") != null ? p.get("postiMax") : p.get("maxPartecipanti");
        if (max != null) {
            try {
                t.setMaxPartecipanti(Integer.parseInt(max.toString()));
            } catch (NumberFormatException ignored) {}
        }


        Object dt = p.get("dataTorneo") != null ? p.get("dataTorneo") : p.get("dataInizioTorneo");
        if (dt != null && !dt.toString().isBlank()) {
            t.setDataInizioTorneo(parseData(dt.toString()));
        } else {
            t.setDataInizioTorneo(LocalDate.now().plusDays(7));
        }

     
        Object dii = p.get("dataInizioIscrizione");
        if (dii != null && !dii.toString().isBlank()) {
            t.setDataInizioIscrizione(parseData(dii.toString()));
        } else {
            t.setDataInizioIscrizione(LocalDate.now());
        }

        
        Object dfi = p.get("dataFineIscrizione");
        if (dfi != null && !dfi.toString().isBlank()) {
            t.setDataFineIscrizione(parseData(dfi.toString()));
        } else {
            t.setDataFineIscrizione(t.getDataInizioTorneo());
        }

        return t;
    }

    private LocalDate parseData(String dataStr) {
        try {
            if (dataStr.contains("T")) {
                dataStr = dataStr.substring(0, dataStr.indexOf("T"));
            }
            return LocalDate.parse(dataStr);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
    
    
}