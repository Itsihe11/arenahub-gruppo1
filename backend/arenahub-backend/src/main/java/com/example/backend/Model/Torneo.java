package com.example.backend.Model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "torneo")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private String gioco;

    private String descrizione;

    @Column(name = "data_inizio_iscrizioni")
    @JsonProperty("dataInizioIscrizione")
    private LocalDate dataInizioIscrizione;

    @Column(name = "data_fine_iscrizioni")
    @JsonProperty("dataFineIscrizione")
    private LocalDate dataFineIscrizione;

    @Column(name = "max_partecipanti")
    @JsonProperty("postiMax")
    private Integer maxPartecipanti;

    @Column(name = "data_inizio_torneo")
    @JsonProperty("dataTorneo")
    private LocalDate dataInizioTorneo;

    private String stato;


    @Transient
    @JsonProperty("iscritti")
    private Integer iscritti = 0;

    public Torneo() {
    }

    public Torneo(String nome, String gioco, String descrizione, LocalDate dataInizioIscrizione,
            LocalDate dataFineIscrizione, Integer maxPartecipanti, LocalDate dataInizioTorneo, String stato) {
        this.nome = nome;
        this.gioco = gioco;
        this.descrizione = descrizione;
        this.dataInizioIscrizione = dataInizioIscrizione;
        this.dataFineIscrizione = dataFineIscrizione;
        this.maxPartecipanti = maxPartecipanti;
        this.dataInizioTorneo = dataInizioTorneo;
        this.stato = stato;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getGioco() {
        return gioco;
    }

    public void setGioco(String gioco) {
        this.gioco = gioco;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public LocalDate getDataInizioIscrizione() {
        return dataInizioIscrizione;
    }

    public void setDataInizioIscrizione(LocalDate dataInizioIscrizione) {
        this.dataInizioIscrizione = dataInizioIscrizione;
    }

    public LocalDate getDataFineIscrizione() {
        return dataFineIscrizione;
    }

    public void setDataFineIscrizione(LocalDate dataFineIscrizione) {
        this.dataFineIscrizione = dataFineIscrizione;
    }

    public Integer getMaxPartecipanti() {
        return maxPartecipanti;
    }

    public void setMaxPartecipanti(Integer maxPartecipanti) {
        this.maxPartecipanti = maxPartecipanti;
    }

    public LocalDate getDataInizioTorneo() {
        return dataInizioTorneo;
    }

    public void setDataInizioTorneo(LocalDate dataInizioTorneo) {
        this.dataInizioTorneo = dataInizioTorneo;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public Integer getIscritti() {
        return iscritti;
    }

    public void setIscritti(Integer iscritti) {
        this.iscritti = iscritti;
    }
}