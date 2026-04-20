package com.dvdapp.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dvdapp.controller.MaterialController;
import com.dvdapp.model.CD;
import com.dvdapp.model.DVD;
import com.dvdapp.model.Libro;
import com.dvdapp.model.Material;
import com.dvdapp.model.Revista;

public class MainWindow extends JFrame {

    private final MaterialController materialController;
    private final DVDTablePanel listPanel;

    private final JComboBox<String> tipoMaterial;
    private final JTextField codigoField;
    private final JTextField tituloField;
    private final JTextField unidadesField;

    private final JTextField duracionField;
    private final JTextField generoField;
    private final JTextField directorField;

    private final JTextField autorField;
    private final JTextField numeroPaginasField;
    private final JTextField editorialLibroField;
    private final JTextField isbnField;
    private final JTextField anioPublicacionField;

    private final JTextField editorialRevistaField;
    private final JTextField periodicidadField;
    private final JTextField fechaPublicacionField;

    private final JTextField artistaField;
    private final JTextField generoCdField;
    private final JTextField duracionCdField;
    private final JTextField numeroCancionesField;

    private final JPanel camposDinamicosPanel;
    private final CardLayout camposDinamicosLayout;

    private final JButton guardarButton;
    private final JButton limpiarButton;
    private final JButton eliminarButton;

    public MainWindow(MaterialController controller) {
        this.materialController = Objects.requireNonNull(controller, "controller es obligatorio.");
        this.listPanel = new DVDTablePanel(true);

        tipoMaterial = new JComboBox<>(new String[]{"DVD", "Libro", "Revista", "CD"});
        codigoField = new JTextField(25);
        codigoField.setEditable(false);
        tituloField = new JTextField(25);
        unidadesField = new JTextField(25);

        duracionField = new JTextField(25);
        generoField = new JTextField(25);
        directorField = new JTextField(25);

        autorField = new JTextField(25);
        numeroPaginasField = new JTextField(25);
        editorialLibroField = new JTextField(25);
        isbnField = new JTextField(25);
        anioPublicacionField = new JTextField(25);

        editorialRevistaField = new JTextField(25);
        periodicidadField = new JTextField(25);
        fechaPublicacionField = new JTextField(25);

        artistaField = new JTextField(25);
        generoCdField = new JTextField(25);
        duracionCdField = new JTextField(25);
        numeroCancionesField = new JTextField(25);

        camposDinamicosLayout = new CardLayout();
        camposDinamicosPanel = new JPanel(camposDinamicosLayout);

        guardarButton = new JButton("Guardar");
        limpiarButton = new JButton("Limpiar");
        eliminarButton = new JButton("Eliminar");

        configureWindow();
        registerEvents();
        updateColumnsByType();
        limpiarFormularioCompleto();
        loadListData();
    }

    private void configureWindow() {
        setTitle("Mediateca - Gestión");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildFormPanel(), BorderLayout.WEST);
        add(listPanel, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        add(panel, gbc, row++, "Tipo:", tipoMaterial);
        add(panel, gbc, row++, "Código:", codigoField);
        add(panel, gbc, row++, "Título:", tituloField);
        add(panel, gbc, row++, "Unidades:", unidadesField);

        camposDinamicosPanel.add(buildDVD(), "DVD");
        camposDinamicosPanel.add(buildLibro(), "Libro");
        camposDinamicosPanel.add(buildRevista(), "Revista");
        camposDinamicosPanel.add(buildCD(), "CD");

        gbc.gridx=0; gbc.gridy=row++; gbc.gridwidth=2;
        panel.add(camposDinamicosPanel, gbc);

        JPanel botones = new JPanel();
        botones.add(guardarButton);
        botones.add(limpiarButton);
        botones.add(eliminarButton);

        gbc.gridy=row;
        panel.add(botones, gbc);

        return panel;
    }

    private JPanel buildDVD() {
        JPanel p = new JPanel(new GridLayout(3,2));
        p.add(new JLabel("Duración")); p.add(duracionField);
        p.add(new JLabel("Género")); p.add(generoField);
        p.add(new JLabel("Director")); p.add(directorField);
        return p;
    }

    private JPanel buildLibro() {
        JPanel p = new JPanel(new GridLayout(5,2));
        p.add(new JLabel("Autor")); p.add(autorField);
        p.add(new JLabel("Nro. Páginas")); p.add(numeroPaginasField);
        p.add(new JLabel("Editorial")); p.add(editorialLibroField);
        p.add(new JLabel("ISBN")); p.add(isbnField);
        p.add(new JLabel("Año publicación")); p.add(anioPublicacionField);
        return p;
    }

    private JPanel buildRevista() {
        JPanel p = new JPanel(new GridLayout(3,2));
        p.add(new JLabel("Editorial")); p.add(editorialRevistaField);
        p.add(new JLabel("Periodicidad")); p.add(periodicidadField);
        p.add(new JLabel("Fecha")); p.add(fechaPublicacionField);
        return p;
    }

    private JPanel buildCD() {
        JPanel p = new JPanel(new GridLayout(4,2));
        p.add(new JLabel("Artista")); p.add(artistaField);
        p.add(new JLabel("Género")); p.add(generoCdField);
        p.add(new JLabel("Duración")); p.add(duracionCdField);
        p.add(new JLabel("Canciones")); p.add(numeroCancionesField);
        return p;
    }

    private void add(JPanel p, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1;
        p.add(new JLabel(label), gbc);
        gbc.gridx=1;
        p.add(field, gbc);
    }

    private void registerEvents() {
        tipoMaterial.addActionListener(e -> {
            camposDinamicosLayout.show(camposDinamicosPanel, getTipo());
            updateColumnsByType();
            updateUnidadesFieldState();
            limpiarFormularioCompleto();
            loadListData();
        });
        guardarButton.addActionListener(e -> guardar());
        limpiarButton.addActionListener(e -> limpiarFormularioCompleto());
        eliminarButton.addActionListener(e -> eliminar());

        listPanel.setOnRowSelected(this::cargarParaEditar);
        listPanel.setOnSearch(this::buscar);
        listPanel.setOnClearSearch(this::loadListData);
        listPanel.setOnRefresh(this::loadListData);
    }

    private void guardar() {
        try {
            Material m = buildMaterial();

            if (!codigoField.getText().isBlank()) {
                m.setCodigo(codigoField.getText());
                materialController.actualizarMaterial(m);
                show("Actualizado");
            } else {
                Material saved = materialController.guardarMaterial(m);
                show("Creado: " + saved.getCodigo());
            }

            limpiarFormularioCompleto();
            loadListData();

        } catch (IllegalArgumentException | SQLException e) {
            error(e.getMessage());
        }
    }

    private void eliminar() {
        try {
            String codigo = codigoField.getText();

            if (codigo.isBlank()) {
                error("Selecciona un material primero");
                return;
            }

            materialController.eliminarMaterial(getTipo(), codigo);
            show("Eliminado");
            limpiarFormularioCompleto();
            loadListData();

        } catch (IllegalArgumentException | SQLException e) {
            error(e.getMessage());
        }
    }

    private void buscar(String q) {
        try {
            List<Material> list = materialController.buscarMateriales(getTipo(), q);
            listPanel.setRows(buildRows(list));
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private void loadListData() {
        try {
            updateColumnsByType();
            List<Material> list = materialController.listarMateriales(getTipo());
            listPanel.setRows(buildRows(list));
        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private void cargarParaEditar(String codigo) {
        try {
            Material m = materialController.buscarMaterialPorCodigo(getTipo(), codigo);
            if (m == null) {
                return;
            }

            codigoField.setText(m.getCodigo());
            tituloField.setText(m.getTitulo());
            unidadesField.setText(m.getUnidadesDisponibles() == null ? "" : String.valueOf(m.getUnidadesDisponibles()));
            loadSpecificFields(m);

        } catch (SQLException e) {
            error(e.getMessage());
        }
    }

    private Material buildMaterial() {
        String tipo = getTipo();
        String titulo = tituloField.getText();

        if (tipo.equals("DVD")) {
            DVD d = new DVD();
            d.setTitulo(titulo);
            d.setUnidadesDisponibles(null);
            d.setDuracion(parseIntegerRequired(duracionField.getText(), "Duracion"));
            d.setGenero(generoField.getText());
            d.setDirector(directorField.getText());
            return d;
        }

        if (tipo.equals("Libro")) {
            int unidades = parseIntegerRequired(unidadesField.getText(), "Unidades");
            Libro l = new Libro();
            l.setTitulo(titulo);
            l.setUnidadesDisponibles(unidades);
            l.setAutor(autorField.getText());
            l.setNumeroPaginas(parseIntegerRequired(numeroPaginasField.getText(), "Numero de paginas"));
            l.setEditorial(editorialLibroField.getText());
            l.setIsbn(isbnField.getText());
            l.setAnioPublicacion(parseIntegerRequired(anioPublicacionField.getText(), "Año de publicacion"));
            return l;
        }

        if (tipo.equals("Revista")) {
            int unidades = parseIntegerRequired(unidadesField.getText(), "Unidades");
            Revista r = new Revista();
            r.setTitulo(titulo);
            r.setUnidadesDisponibles(unidades);
            r.setEditorial(editorialRevistaField.getText());
            r.setPeriodicidad(periodicidadField.getText());
            r.setFechaPublicacion(fechaPublicacionField.getText());
            return r;
        }

        int unidades = parseIntegerRequired(unidadesField.getText(), "Unidades");
        CD c = new CD();
        c.setTitulo(titulo);
        c.setUnidadesDisponibles(unidades);
        c.setArtista(artistaField.getText());
        c.setGenero(generoCdField.getText());
        c.setDuracion(parseIntegerRequired(duracionCdField.getText(), "Duracion"));
        c.setNumeroCanciones(parseIntegerRequired(numeroCancionesField.getText(), "Numero de canciones"));
        return c;
    }

    private List<Object[]> buildRows(List<Material> list) {
        List<Object[]> rows = new ArrayList<>();
        String tipo = getTipo();

        for (Material material : list) {
            if ("DVD".equals(tipo) && material instanceof DVD dvd) {
                rows.add(new Object[]{
                    dvd.getCodigo(),
                    dvd.getTitulo(),
                    dvd.getDuracion(),
                    dvd.getGenero(),
                    dvd.getDirector()
                });
            } else if ("Libro".equals(tipo) && material instanceof Libro libro) {
                rows.add(new Object[]{
                    libro.getCodigo(),
                    libro.getTitulo(),
                    libro.getUnidadesDisponibles(),
                    libro.getAutor(),
                    libro.getNumeroPaginas(),
                    libro.getEditorial(),
                    libro.getIsbn(),
                    libro.getAnioPublicacion()
                });
            } else if ("Revista".equals(tipo) && material instanceof Revista revista) {
                rows.add(new Object[]{
                    revista.getCodigo(),
                    revista.getTitulo(),
                    revista.getUnidadesDisponibles(),
                    revista.getEditorial(),
                    revista.getPeriodicidad(),
                    revista.getFechaPublicacion()
                });
            } else if ("CD".equals(tipo) && material instanceof CD cd) {
                rows.add(new Object[]{
                    cd.getCodigo(),
                    cd.getTitulo(),
                    cd.getUnidadesDisponibles(),
                    cd.getArtista(),
                    cd.getGenero(),
                    cd.getDuracion(),
                    cd.getNumeroCanciones()
                });
            }
        }

        return rows;
    }

    private String getTipo() {
        Object selected = tipoMaterial.getSelectedItem();
        if (selected == null) {
            throw new IllegalStateException("No hay tipo de material seleccionado.");
        }
        return selected.toString();
    }

    private void limpiarFormularioCompleto() {
        codigoField.setText("");
        tituloField.setText("");
        unidadesField.setText("");
        duracionField.setText("");
        generoField.setText("");
        directorField.setText("");
        autorField.setText("");
        numeroPaginasField.setText("");
        editorialLibroField.setText("");
        isbnField.setText("");
        anioPublicacionField.setText("");
        editorialRevistaField.setText("");
        periodicidadField.setText("");
        fechaPublicacionField.setText("");
        artistaField.setText("");
        generoCdField.setText("");
        duracionCdField.setText("");
        numeroCancionesField.setText("");
        camposDinamicosLayout.show(camposDinamicosPanel, getTipo());
        updateUnidadesFieldState();
    }

    private void loadSpecificFields(Material material) {
        duracionField.setText("");
        generoField.setText("");
        directorField.setText("");
        autorField.setText("");
        numeroPaginasField.setText("");
        editorialLibroField.setText("");
        isbnField.setText("");
        anioPublicacionField.setText("");
        editorialRevistaField.setText("");
        periodicidadField.setText("");
        fechaPublicacionField.setText("");
        artistaField.setText("");
        generoCdField.setText("");
        duracionCdField.setText("");
        numeroCancionesField.setText("");

        switch (material) {
            case DVD dvd -> {
                duracionField.setText(String.valueOf(dvd.getDuracion()));
                generoField.setText(dvd.getGenero());
                directorField.setText(dvd.getDirector());
            }
            case Libro libro -> {
                autorField.setText(libro.getAutor());
                numeroPaginasField.setText(String.valueOf(libro.getNumeroPaginas()));
                editorialLibroField.setText(libro.getEditorial());
                isbnField.setText(libro.getIsbn());
                anioPublicacionField.setText(String.valueOf(libro.getAnioPublicacion()));
            }
            case Revista revista -> {
                editorialRevistaField.setText(revista.getEditorial());
                periodicidadField.setText(revista.getPeriodicidad());
                fechaPublicacionField.setText(revista.getFechaPublicacion());
            }
            case CD cd -> {
                artistaField.setText(cd.getArtista());
                generoCdField.setText(cd.getGenero());
                duracionCdField.setText(String.valueOf(cd.getDuracion()));
                numeroCancionesField.setText(String.valueOf(cd.getNumeroCanciones()));
            }
            default -> {
            }
        }
    }

    private void updateColumnsByType() {
        listPanel.setColumns(columnsForType(getTipo()));
    }

    private String[] columnsForType(String tipo) {
        return switch (tipo) {
            case "DVD" -> new String[]{"Codigo", "Titulo", "Duracion", "Genero", "Director"};
            case "Libro" -> new String[]{"Codigo", "Titulo", "Unidades", "Autor", "Paginas", "Editorial", "ISBN", "Año"};
            case "Revista" -> new String[]{"Codigo", "Titulo", "Unidades", "Editorial", "Periodicidad", "Fecha"};
            case "CD" -> new String[]{"Codigo", "Titulo", "Unidades", "Artista", "Genero", "Duracion", "Canciones"};
            default -> new String[]{"Codigo", "Titulo", "Unidades"};
        };
    }

    private void updateUnidadesFieldState() {
        boolean enabled = !"DVD".equals(getTipo());
        unidadesField.setEnabled(enabled);
        if (!enabled) {
            unidadesField.setText("");
        }
    }

    private int parseIntegerRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " es obligatorio.");
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " debe ser numerico.");
        }
    }

    private void show(String m) {
        JOptionPane.showMessageDialog(this, m);
    }

    private void error(String m) {
        JOptionPane.showMessageDialog(this, m);
    }
}
