package com.example.backend.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class TorneoUpdateRequest {

    private String nome;
    private String gioco;
    private LocalDateTime dataTorneo;
    private LocalDate dataInizioIscrizione;
    private LocalDate dataFineIscrizione;

    @Min(value = 1, message = "Dati non validi")
    private Integer postiMax;

    @Pattern(regexp = "^(aperto|chiuso|concluso)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Dati non validi")
    private String stato;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getGioco() { return gioco; }
    public void setGioco(String gioco) { this.gioco = gioco; }
    public LocalDateTime getDataTorneo() { return dataTorneo; }
    public void setDataTorneo(LocalDateTime dataTorneo) { this.dataTorneo = dataTorneo; }
    public LocalDate getDataInizioIscrizione() { return dataInizioIscrizione; }
    public void setDataInizioIscrizione(LocalDate dataInizioIscrizione) { this.dataInizioIscrizione = dataInizioIscrizione; }
    public LocalDate getDataFineIscrizione() { return dataFineIscrizione; }
    public void setDataFineIscrizione(LocalDate dataFineIscrizione) { this.dataFineIscrizione = dataFineIscrizione; }
    public Integer getPostiMax() { return postiMax; }
    public void setPostiMax(Integer postiMax) { this.postiMax = postiMax; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
}