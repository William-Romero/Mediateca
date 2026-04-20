package com.dvdapp.model;

public class CD extends Material {
    private String artista;
    private String genero;
    private Integer duracion;
    private int numeroCanciones;

    public CD() {
        super();
    }

    public CD(
            String codigo,
            String titulo,
            Integer unidadesDisponibles,
            String artista,
            String genero,
            Integer duracion,
            int numeroCanciones
    ) {
        super(codigo, titulo, unidadesDisponibles);
        this.artista = artista;
        this.genero = genero;
        this.duracion = duracion;
        this.numeroCanciones = numeroCanciones;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
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
            + " | Genero: " + genero
            + " | Duracion: " + duracion
            + " | NumeroCanciones: " + numeroCanciones;
    }
}
