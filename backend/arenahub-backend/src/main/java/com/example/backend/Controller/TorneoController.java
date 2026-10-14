package com.example.backend.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Model.Torneo;
import com.example.backend.Services.TorneoService;

@RestController
@RequestMapping("/api/tornei")
@CrossOrigin(origins = "http://localhost:4200")
public class TorneoController {
	
	
	@RestControllerAdvice
	public class GestoreErrori {

	    @ExceptionHandler(ResponseStatusException.class)
	    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
	        return ResponseEntity.status(e.getStatusCode()).body(Map.of("errore", e.getReason()));
	    }

	    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
	    public ResponseEntity<Map<String, String>> id(Exception e) {
	        return ResponseEntity.badRequest().body(Map.of("errore", "Id non valido"));
	    }

	    @ExceptionHandler({HttpMessageNotReadableException.class})
	    public ResponseEntity<Map<String, String>> body(Exception e) {
	        return ResponseEntity.badRequest().body(Map.of("errore", "Dati non validi"));
	    }
	}

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    public List<Map<String, Object>> elenco(
    		@RequestParam(required = false) String stato,
    		@RequestParam(required = false) String gioco,
    		@RequestParam(required = false) String data) {
        return torneoService.elenco(stato, gioco, data);
    }

    @GetMapping("/{id}")
    public Map<String, Object> dettaglio(@PathVariable int id) {
        return torneoService.dettaglio(id);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> crea(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> payload) {

    	Map<String, Object> r = torneoService.crea(uid(auth), payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }



    @PutMapping("/{id}")
    public Map<String, Object> modifica(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable int id,
            @RequestBody Map<String, Object> payload) {
        return torneoService.modifica(uid(auth), id, payload);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable int id) {
        torneoService.elimina(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    private Integer uid(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            return Integer.parseInt(auth.substring(7).trim());
        } catch (NumberFormatException e) {
            return null;
        }
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


        Object dt = p.get("dataTorneo") != null ? p.get("dataTorneo") : p.get("dataTorneo");
        if (dt != null && !dt.toString().isBlank()) {
            t.setDataTorneo(parseData(dt.toString()));
        } else {
            t.setDataTorneo(LocalDate.now().plusDays(7));
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
            t.setDataFineIscrizione(t.getDataTorneo());
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