package com.example.backend.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "partita")
public class Partita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "giocatore1", nullable = false)
    private Utente giocatore1;

    @ManyToOne
    @JoinColumn(name = "giocatore2", nullable = false)
    private Utente giocatore2;

    @Column(nullable = false)
    private Integer turno = 1;

    private LocalDateTime dataora;

    private Integer punteggio1;

    private Integer punteggio2;

    @Column(nullable = false, length = 50)
    private String stato = "da_giocare";

    @ManyToOne
    @JoinColumn(name = "vincitore_id")
    private Utente vincitore;

    public Partita() {}

    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Torneo getTorneo() { return torneo; }
    public void setTorneo(Torneo torneo) { this.torneo = torneo; }

    public Utente getGiocatore1() { return giocatore1; }
    public void setGiocatore1(Utente giocatore1) { this.giocatore1 = giocatore1; }

    public Utente getGiocatore2() { return giocatore2; }
    public void setGiocatore2(Utente giocatore2) { this.giocatore2 = giocatore2; }

    public Integer getTurno() { return turno; }
    public void setTurno(Integer turno) { this.turno = turno; }

    public LocalDateTime getDataora() { return dataora; }
    public void setDataora(LocalDateTime dataora) { this.dataora = dataora; }

    public Integer getPunteggio1() { return punteggio1; }
    public void setPunteggio1(Integer punteggio1) { this.punteggio1 = punteggio1; }

    public Integer getPunteggio2() { return punteggio2; }
    public void setPunteggio2(Integer punteggio2) { this.punteggio2 = punteggio2; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public Utente getVincitore() { return vincitore; }
    public void setVincitore(Utente vincitore) { this.vincitore = vincitore; }
}