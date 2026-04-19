package com.dvdapp.model;

public class Libro extends Material {
    private String autor;
    private String isbn;

    public Libro() {
        super();
    }

    public Libro(String codigo, String titulo, int unidadesDisponibles, String autor, String isbn) {
        super(codigo, titulo, unidadesDisponibles);
        this.autor = autor;
        this.isbn = isbn;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String mostrarDatos() {
        return "Codigo: " + getCodigo()
            + " | Titulo: " + getTitulo()
            + " | Unidades: " + getUnidadesDisponibles()
            + " | Autor: " + autor
            + " | ISBN: " + isbn;
    }
}
