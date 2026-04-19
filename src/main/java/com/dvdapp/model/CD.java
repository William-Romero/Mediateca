package com.dvdapp.model;

public class CD extends Material {
    private String artista;
    private int numeroCanciones;

    public CD() {
        super();
    }

    public CD(String codigo, String titulo, int unidadesDisponibles, String artista, int numeroCanciones) {
        super(codigo, titulo, unidadesDisponibles);
        this.artista = artista;
        this.numeroCanciones = numeroCanciones;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public int getNumeroCanciones() {
        return numeroCanciones;
    }

    public void setNumeroCanciones(int numeroCanciones) {
        this.numeroCanciones = numeroCanciones;
    }

    @Override
    public String mostrarDatos() {
        return "Codigo: " + getCodigo()
            + " | Titulo: " + getTitulo()
            + " | Unidades: " + getUnidadesDisponibles()
            + " | Artista: " + artista
            + " | NumeroCanciones: " + numeroCanciones;
    }
}
