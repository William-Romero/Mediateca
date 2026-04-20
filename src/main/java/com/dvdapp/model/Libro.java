package com.dvdapp.model;

public class Libro extends Material {
    private String autor;
    private Integer numeroPaginas;
    private String editorial;
    private String isbn;
    private Integer anioPublicacion;

    public Libro() {
        super();
    }

    public Libro(
            String codigo,
            String titulo,
            Integer unidadesDisponibles,
            String autor,
            Integer numeroPaginas,
            String editorial,
            String isbn,
            Integer anioPublicacion
    ) {
        super(codigo, titulo, unidadesDisponibles);
        this.autor = autor;
        this.numeroPaginas = numeroPaginas;
        this.editorial = editorial;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    @Override
    public String mostrarDatos() {
        return "Codigo: " + getCodigo()
            + " | Titulo: " + getTitulo()
            + " | Unidades: " + getUnidadesDisponibles()
            + " | Autor: " + autor
            + " | Paginas: " + numeroPaginas
            + " | Editorial: " + editorial
            + " | ISBN: " + isbn
            + " | AnioPublicacion: " + anioPublicacion;
    }
}
