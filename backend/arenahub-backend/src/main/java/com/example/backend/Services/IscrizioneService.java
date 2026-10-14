package com.example.backend.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Model.Iscrizione;
import com.example.backend.Model.Torneo;
import com.example.backend.Model.Utente;
import com.example.backend.Repository.IscrizioneRepository;
import com.example.backend.Repository.TorneoRepository;

@Service
public class IscrizioneService {

    private final TorneoRepository tornei;
    private final IscrizioneRepository iscrizioni;
    private final UtenteService utenti;
    private final TorneoService torneoService;

    public IscrizioneService(TorneoRepository tornei, IscrizioneRepository iscrizioni,
                             UtenteService utenti, TorneoService torneoService) {
        this.tornei = tornei;
        this.iscrizioni = iscrizioni;
        this.utenti = utenti;
        this.torneoService = torneoService;
    }

    @Transactional
    public Map<String, Object> iscrivi(Integer uid, int torneoId) {
        Utente u = utenti.utenteCorrente(uid);                       
        Torneo t = torneoService.trova(torneoId);                    

        if (!"aperto".equalsIgnoreCase(t.getStato()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Iscrizioni chiuse");
        if (iscrizioni.countByTorneoId(torneoId) >= t.getMaxPartecipanti())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Posti esauriti");
        if (iscrizioni.existsByTorneoIdAndUtenteId(torneoId, u.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Già iscritto");

        Iscrizione i = new Iscrizione();
        i.setTorneo(t);
        i.setUtente(u);
        i.setData(LocalDate.now());
        i.setStato("confermata");
        i = iscrizioni.save(i);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("idTorneo", torneoId);
        r.put("idUtente", u.getId());
        r.put("dataIscrizione", i.getData());
        r.put("torneo", riepilogo(t));
        return r;
    }

    @Transactional
    public Map<String, Object> annulla(Integer uid, int torneoId) {
        Utente u = utenti.utenteCorrente(uid);                       
        Torneo t = torneoService.trova(torneoId);                    

        boolean giaIniziato = t.getDataTorneo() != null
                && t.getDataTorneo().isBefore(LocalDate.now());
        if (!"aperto".equalsIgnoreCase(t.getStato()) || giaIniziato)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Annullamento non consentito");

        if (iscrizioni.deleteByTorneoIdAndUtenteId(torneoId, u.getId()) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Iscrizione non trovata");

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("idTorneo", torneoId);
        r.put("idUtente", u.getId());
        r.put("torneo", riepilogo(t));                               
        return r;
    }

    public List<Map<String, Object>> mieIscrizioni(Integer uid) {
        Utente u = utenti.utenteCorrente(uid);                       
        List<Map<String, Object>> risultato = new ArrayList<>();
        for (Iscrizione i : iscrizioni.findByUtenteId(u.getId())) {
            Map<String, Object> m = new LinkedHashMap<>(torneoService.conIscritti(i.getTorneo()));
            m.put("dataIscrizione", i.getData());
            risultato.add(m);
        }
        return risultato;
    }

    public Map<String, Object> partecipanti(int torneoId) {
        Torneo t = torneoService.trova(torneoId);                   
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Iscrizione i : iscrizioni.findByTorneoIdOrdinate(torneoId)) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("idUtente", i.getUtente().getId());
            p.put("nome", i.getUtente().getNome());
            p.put("cognome", i.getUtente().getCognome());
            p.put("dataIscrizione", i.getData());
            lista.add(p);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("idTorneo", t.getId());
        r.put("nome", t.getNome());
        r.put("iscritti", lista.size());
        r.put("postiMax", t.getMaxPartecipanti());
        r.put("partecipanti", lista);
        return r;
    }

    private Map<String, Object> riepilogo(Torneo t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("nome", t.getNome());
        m.put("iscritti", iscrizioni.countByTorneoId(t.getId()));
        m.put("postiMax", t.getMaxPartecipanti());
        return m;
    }
}