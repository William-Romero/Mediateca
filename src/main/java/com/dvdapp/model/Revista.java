package com.dvdapp.model;

public class Revista extends Material {
    private String editorial;
    private String periodicidad;
    private String fechaPublicacion;

    public Revista() {
        super();
    }

    public Revista(
            String codigo,
            String titulo,
            Integer unidadesDisponibles,
            String editorial,
            String periodicidad,
            String fechaPublicacion
    ) {
        super(codigo, titulo, unidadesDisponibles);
        this.editorial = editorial;
        this.periodicidad = periodicidad;
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(String periodicidad) {
        this.periodicidad = periodicidad;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    @Override
    public String mostrarDatos() {
        return "Codigo: " + getCodigo()
            + " | Titulo: " + getTitulo()
            + " | Unidades: " + getUnidadesDisponibles()
            + " | Editorial: " + editorial
            + " | Periodicidad: " + periodicidad
            + " | FechaPublicacion: " + fechaPublicacion;
    }
}
