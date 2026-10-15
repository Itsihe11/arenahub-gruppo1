package com.example.backend.Dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RisultatiRequest {

    @NotEmpty(message = "Dati non validi")
    @Valid
    private List<RisultatoItemDto> risultati;

    public List<RisultatoItemDto> getRisultati() { return risultati; }
    public void setRisultati(List<RisultatoItemDto> risultati) { this.risultati = risultati; }

    public static class RisultatoItemDto {
        @NotNull(message = "Dati non validi")
        private Integer idUtente;

        @NotNull(message = "Dati non validi")
        @Min(value = 1, message = "Dati non validi")
        private Integer posizione;

        public Integer getIdUtente() { return idUtente; }
        public void setIdUtente(Integer idUtente) { this.idUtente = idUtente; }
        public Integer getPosizione() { return posizione; }
        public void setPosizione(Integer posizione) { this.posizione = posizione; }
    }
}