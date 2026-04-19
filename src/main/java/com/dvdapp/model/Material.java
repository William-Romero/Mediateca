package com.dvdapp.model;

public abstract class Material {
    private String codigo;
    private String titulo;
    private int unidadesDisponibles;

    protected Material() {
    }

    protected Material(String codigo, String titulo, int unidadesDisponibles) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.unidadesDisponibles = unidadesDisponibles;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getUnidadesDisponibles() {
        return unidadesDisponibles;
    }

    public void setUnidadesDisponibles(int unidadesDisponibles) {
        this.unidadesDisponibles = unidadesDisponibles;
    }

    public abstract String mostrarDatos();
}
