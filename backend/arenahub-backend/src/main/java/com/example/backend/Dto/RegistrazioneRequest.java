package com.example.backend.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrazioneRequest {

    @NotBlank(message = "Dati non validi")
    private String nome;

    @NotBlank(message = "Dati non validi")
    private String cognome;

    @NotBlank(message = "Dati non validi")
    @Email(message = "Dati non validi")
    private String email;

    @NotBlank(message = "Dati non validi")
    @Size(min = 8, message = "Dati non validi")
    private String password;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}