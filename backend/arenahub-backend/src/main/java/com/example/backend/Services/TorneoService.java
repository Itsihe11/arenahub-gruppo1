package com.example.backend.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Model.Torneo;
import com.example.backend.Repository.IscrizioneRepository;
import com.example.backend.Repository.TorneoRepository;
import java.time.format.DateTimeParseException;

import jakarta.transaction.Transactional;

@Service
public class TorneoService {
    private static final Set<String> STATI = Set.of("aperto", "chiuso", "concluso");
    private final TorneoRepository tornei;
    private final IscrizioneRepository iscrizioni;
    private final UtenteService utenti;

    public TorneoService(TorneoRepository tornei, IscrizioneRepository iscrizioni, UtenteService utenti) {
        this.tornei = tornei;
        this.iscrizioni = iscrizioni;
        this.utenti = utenti;
    }
    
    @Transactional
    public void elimina(Integer uid, int id) {
        utenti.richiediAdmin(uid);
        Torneo t = trova(id);

        iscrizioni.deleteByTorneoId(id);

   
        tornei.delete(t);
    }

    public List<Map<String, Object>> elenco(String stato) {
        if (stato != null && !STATI.contains(stato))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stato non valido");
        List<Torneo> lista = (stato == null)
                ? tornei.findAllByOrderByDataTorneo()
                : tornei.findByStatoOrderByDataTorneo(stato);
        return conIscritti(lista);
    }

    public Map<String, Object> dettaglio(int id) {
        return conIscritti(trova(id));
    }

    public Map<String, Object> crea(Integer uid, Map<String, Object> p) {
        utenti.richiediAdmin(uid);
        for (String campo : List.of("nome", "gioco", "descrizione", "dataTorneo",
                                    "dataInizioIscrizione", "dataFineIscrizione", "postiMax")) {
            if (p.get(campo) == null || p.get(campo).toString().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        }
        Torneo t = new Torneo();
        applica(t, p);                       // converte e valida i valori presenti
        if (t.getMaxPartecipanti() <= 0 || t.getDataTorneo().isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        t.setStato("aperto");
        return conIscritti(tornei.save(t));
    }

    public Map<String, Object> modifica(Integer uid, int id, Map<String, Object> p) {
        utenti.richiediAdmin(uid);
        Torneo t = trova(id);                // parte dal torneo esistente
        if (t.getStato().equals("concluso"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Torneo concluso");
        applica(t, p);                       // sovrascrive SOLO i campi presenti nel body
        if (t.getMaxPartecipanti() <= 0 || !STATI.contains(t.getStato()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        if (t.getMaxPartecipanti() < iscrizioni.countByTorneoId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Posti insufficienti");
        return conIscritti(tornei.save(t));
    }
    
    private void applica(Torneo t, Map<String, Object> p) {
        try {
            if (p.get("nome") != null) t.setNome(p.get("nome").toString());
            if (p.get("descrizione") != null) t.setDescrizione(p.get("descrizione").toString());
            if (p.get("gioco") != null) t.setGioco(p.get("gioco").toString());
            if (p.get("stato") != null) t.setStato(p.get("stato").toString());
            if (p.get("postiMax") != null) t.setMaxPartecipanti(Integer.parseInt(p.get("postiMax").toString()));
            if (p.get("dataTorneo") != null) t.setDataTorneo(LocalDate.parse(solaData(p.get("dataTorneo"))));
            if (p.get("dataInizioIscrizione") != null) t.setDataInizioIscrizione(LocalDate.parse(solaData(p.get("dataInizioIscrizione"))));
            if (p.get("dataFineIscrizione") != null) t.setDataFineIscrizione(LocalDate.parse(solaData(p.get("dataFineIscrizione"))));
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        }
    }
    
    private String solaData(Object o) {
        String s = o.toString();
        return s.contains("T") ? s.substring(0, s.indexOf('T')) : s;
    }

    public Torneo trova(int id) {
        return tornei.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneo non trovato"));
    }

    public List<Map<String, Object>> conIscritti(List<Torneo> lista) {
  
        List<Object[]> conteggi = iscrizioni.countIscrittiPerTorneo();
        Map<Integer, Long> mappaConteggi = new HashMap<>();
        for (Object[] riga : conteggi) {
            Integer tId = (Integer) riga[0];
            Long count = (Long) riga[1];
            if (tId != null) {
                mappaConteggi.put(tId, count);
            }
        }

        List<Map<String, Object>> risultato = new ArrayList<>();
        for (Torneo t : lista) {
            long count = mappaConteggi.getOrDefault(t.getId(), 0L);
            risultato.add(creaMappa(t, (int) count));
        }
        return risultato;
    }

    public Map<String, Object> conIscritti(Torneo t) {
        return conIscritti(List.of(t)).get(0);
    }

    private Map<String, Object> creaMappa(Torneo t, int iscritti) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("nome", t.getNome());
        m.put("gioco", t.getGioco());
        m.put("descrizione", t.getDescrizione());
        m.put("stato", t.getStato());
        m.put("dataTorneo", t.getDataTorneo());
        m.put("dataInizioIscrizione", t.getDataInizioIscrizione());
        m.put("dataFineIscrizione", t.getDataFineIscrizione());
        m.put("postiMax", t.getMaxPartecipanti());
        m.put("iscritti", iscritti);
        return m;
    }
}