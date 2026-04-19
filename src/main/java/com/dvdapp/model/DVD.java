package com.dvdapp.model;

public class DVD extends Material {
    private int duracion;
    private String genero;
    private String director;

    public DVD() {
        super();
    }

    public DVD(String codigo, String titulo, int unidadesDisponibles, int duracion, String genero, String director) {
        super(codigo, titulo, unidadesDisponibles);
        this.duracion = duracion;
        this.genero = genero;
        this.director = director;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    @Override
    public String mostrarDatos() {
        return "Codigo: " + getCodigo()
            + " | Titulo: " + getTitulo()
            + " | Unidades: " + getUnidadesDisponibles()
            + " | Duracion: " + duracion
            + " | Genero: " + genero
            + " | Director: " + director;
    }
}
