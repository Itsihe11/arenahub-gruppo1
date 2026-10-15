package com.example.backend.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PartitaRisultatoRequest {

    @NotNull(message = "Dati non validi")
    @Min(value = 0, message = "Dati non validi")
    private Integer punteggio1;

    @NotNull(message = "Dati non validi")
    @Min(value = 0, message = "Dati non validi")
    private Integer punteggio2;

    @Pattern(regexp = "^(da_giocare|completata)$", message = "Dati non validi")
    private String stato = "completata";

    public Integer getPunteggio1() { return punteggio1; }
    public void setPunteggio1(Integer punteggio1) { this.punteggio1 = punteggio1; }
    public Integer getPunteggio2() { return punteggio2; }
    public void setPunteggio2(Integer punteggio2) { this.punteggio2 = punteggio2; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
}