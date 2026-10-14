package com.example.backend.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Model.Iscrizione;
import com.example.backend.Model.Torneo;
import com.example.backend.Model.Utente;
import com.example.backend.Services.IscrizioneService;

@RestController
@RequestMapping("/api/iscrizioni")
@CrossOrigin(origins = "http://localhost:4200")
public class IscrizioneController {

    @Autowired
    private IscrizioneService iscrizioneService;


    @PostMapping
    public ResponseEntity<?> iscriviDiretto(@RequestBody Map<String, Object> payload) {
        return iscrivi(payload);
    }

 
    @PostMapping("/iscrivi")
    public ResponseEntity<?> iscrivi(@RequestBody Map<String, Object> payload) {
        System.out.println("=== REQUEST /api/iscrizioni/iscrivi ===");
        System.out.println("Payload ricevuto: " + payload);

        Integer uid = estraiId(payload, "uid", "utenteId", "idUtente");
        Integer torneoId = estraiId(payload, "torneoId", "idTorneo");

        System.out.println("Estratto -> UID: " + uid + ", TorneoID: " + torneoId);

        if (uid == null || torneoId == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Parametri 'utenteId' e 'torneoId' obbligatori o non validi.");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            Iscrizione nuova = iscrizioneService.iscrivi(uid, torneoId);
            
            Map<String, Object> risposta = new HashMap<>();
            risposta.put("id", nuova.getId());
            risposta.put("torneoId", nuova.getTorneoId());
            risposta.put("utenteId", nuova.getUtenteId());
            risposta.put("stato", nuova.getStato());

            return ResponseEntity.ok(risposta);
        } catch (Exception e) {
            System.err.println(" Errore in IscrizioneService: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
   
    @PostMapping("/annulla")
    public ResponseEntity<?> annulla(@RequestBody Map<String, Object> payload) {
        Integer uid = estraiId(payload, "uid", "utenteId", "idUtente");
        Integer torneoId = estraiId(payload, "torneoId", "idTorneo");

        if (uid == null || torneoId == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Parametri  'utenteId' e 'torneoId' obbligatori.");
            return ResponseEntity.badRequest().body(err);
        }

        iscrizioneService.annulla(uid, torneoId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> disiscrivi(
            @RequestParam int torneoId,
            @RequestParam(name = "utenteId", required = false) Integer utenteId,
            @RequestParam(name = "uid", required = false) Integer uid) {

        int idUtente = (utenteId != null) ? utenteId : (uid != null ? uid : 0);
        iscrizioneService.disiscrivi(torneoId, idUtente);

        return ResponseEntity.noContent().build();
    }

  
    @GetMapping("/mie")
    public ResponseEntity<List<Torneo>> mieIscrizioni(@RequestParam int uid) {
        List<Torneo> lista = iscrizioneService.mieIscrizioni(uid);
        return ResponseEntity.ok(lista);
    }

    
    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<List<Utente>> iscritti(@RequestParam int uid, @PathVariable int torneoId) {
        List<Utente> lista = iscrizioneService.iscritti(uid, torneoId);
        return ResponseEntity.ok(lista);
    }


    private Integer estraiId(Map<String, Object> payload, String... chiavi) {
        for (String chiave : chiavi) {
            if (payload.containsKey(chiave) && payload.get(chiave) != null) {
                Object val = payload.get(chiave);
                if (val instanceof Number) {
                    return ((Number) val).intValue();
                }
                try {
                    return Integer.parseInt(val.toString());
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }
}