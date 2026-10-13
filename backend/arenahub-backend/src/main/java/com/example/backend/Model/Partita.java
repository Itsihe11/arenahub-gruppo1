package com.example.backend.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "partita")
public class Partita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giocatore1", nullable = false)
    private Utente giocatore1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giocatore2", nullable = false)
    private Utente giocatore2;

    @Column(nullable = false)
    private Integer turno;

    private LocalDateTime dataora;

    private Integer punteggio1; 

    private Integer punteggio2; 

    @Column(length = 50)
    private String stato;

    public Partita() {
    }

    public Partita(Torneo torneo, Utente giocatore1, Utente giocatore2, Integer turno, LocalDateTime dataora, Integer punteggio1, Integer punteggio2, String stato) {
        this.torneo = torneo;
        this.giocatore1 = giocatore1;
        this.giocatore2 = giocatore2;
        this.turno = turno;
        this.dataora = dataora;
        this.punteggio1 = punteggio1;
        this.punteggio2 = punteggio2;
        this.stato = stato;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public Utente getGiocatore1() {
        return giocatore1;
    }

    public void setGiocatore1(Utente giocatore1) {
        this.giocatore1 = giocatore1;
    }

    public Utente getGiocatore2() {
        return giocatore2;
    }

    public void setGiocatore2(Utente giocatore2) {
        this.giocatore2 = giocatore2;
    }

    public Integer getTurno() {
        return turno;
    }

    public void setTurno(Integer turno) {
        this.turno = turno;
    }

    public LocalDateTime getDataora() {
        return dataora;
    }

    public void setDataora(LocalDateTime dataora) {
        this.dataora = dataora;
    }

    public Integer getPunteggio1() {
        return punteggio1;
    }

    public void setPunteggio1(Integer punteggio1) {
        this.punteggio1 = punteggio1;
    }

    public Integer getPunteggio2() {
        return punteggio2;
    }

    public void setPunteggio2(Integer punteggio2) {
        this.punteggio2 = punteggio2;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}