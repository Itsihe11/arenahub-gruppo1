package com.example.backend.Services;

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

import jakarta.transaction.Transactional;

@Service
public class TorneoService {
    private static final Set<String> STATI = Set.of("aperto", "chiuso");
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
                ? tornei.findAllByOrderByDataInizioTorneo()
                : tornei.findByStatoOrderByDataInizioTorneo(stato);
        return conIscritti(lista);
    }

    public Map<String, Object> dettaglio(int id) {
        return conIscritti(trova(id));
    }

    public Map<String, Object> crea(Integer uid, Torneo t) {
        utenti.richiediAdmin(uid);
        t.setStato("aperto");
        return conIscritti(tornei.save(t));
    }

    public Map<String, Object> modifica(Integer uid, int id, Torneo nuovo) {
        utenti.richiediAdmin(uid);
        Torneo t = trova(id);
        if (!STATI.contains(nuovo.getStato()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        if (t.getStato().equals("concluso"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Torneo concluso");
        if (nuovo.getMaxPartecipanti() < iscrizioni.countByTorneoId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Posti insufficienti");
        nuovo.setId(id);
        return conIscritti(tornei.save(nuovo));
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
        m.put("descrizione", t.getDescrizione());
        m.put("gioco", t.getGioco());
        m.put("stato", t.getStato());
        m.put("dataInizioIscrizione", t.getDataInizioIscrizione());
        m.put("dataFineIscrizione", t.getDataFineIscrizione());
        m.put("dataTorneo", t.getDataInizioTorneo());
        m.put("postiMax", t.getMaxPartecipanti());
        m.put("iscritti", iscritti);
        return m;
    }
}