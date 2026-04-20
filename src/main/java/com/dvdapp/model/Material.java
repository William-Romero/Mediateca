package com.dvdapp.model;

public abstract class Material {
    private String codigo;
    private String titulo;
    private Integer unidadesDisponibles;

    protected Material() {
    }

    protected Material(String codigo, String titulo, Integer unidadesDisponibles) {
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

    public Integer getUnidadesDisponibles() {
        return unidadesDisponibles;
    }

    public void setUnidadesDisponibles(Integer unidadesDisponibles) {
        this.unidadesDisponibles = unidadesDisponibles;
    }

    public abstract String mostrarDatos();
}
