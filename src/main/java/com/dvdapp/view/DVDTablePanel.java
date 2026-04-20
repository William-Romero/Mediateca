package com.dvdapp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.dvdapp.model.DVD;

public class DVDTablePanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTable dvdTable;
    private final JTextField searchField;
    private final JButton searchButton;
    private final JButton clearSearchButton;
    private final JButton refreshButton;

    private Consumer<String> rowSelectedListener;
    private Consumer<String> searchListener;
    private Runnable clearSearchListener;
    private Runnable refreshListener;

    public DVDTablePanel(boolean searchable) {
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD5DCE8)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        refreshButton = new JButton("Refrescar");
        topPanel.add(refreshButton);

        if (searchable) {
            JLabel searchLabel = new JLabel("Buscar:");
            searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            topPanel.add(searchLabel);
            searchField = new JTextField(20);
            searchField.setPreferredSize(new Dimension(220, 32));
            searchButton = new JButton("Buscar");
            clearSearchButton = new JButton("Limpiar");
            topPanel.add(searchField);
            topPanel.add(searchButton);
            topPanel.add(clearSearchButton);
        } else {
            searchField = null;
            searchButton = null;
            clearSearchButton = null;
        }

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Codigo", "Titulo", "Duracion", "Genero", "Director"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dvdTable = new JTable(tableModel);
        dvdTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dvdTable.setRowHeight(28);
        dvdTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dvdTable.getTableHeader().setBackground(new Color(0xEAF0FA));
        dvdTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(dvdTable), BorderLayout.CENTER);

        registerInternalEvents(searchable);
    }

    public void setDvds(List<DVD> dvds) {
        tableModel.setRowCount(0);
        for (DVD dvd : dvds) {
            tableModel.addRow(new Object[]{
                    dvd.getCodigo(),
                    dvd.getTitulo(),
                    dvd.getDuracion(),
                    dvd.getGenero(),
                    dvd.getDirector()
            });
        }
        dvdTable.clearSelection();
    }

    public void setColumns(String[] columns) {
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
    }

    public void setRows(List<Object[]> rows) {
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        dvdTable.clearSelection();
    }

    public void setOnRowSelected(Consumer<String> listener) {
        this.rowSelectedListener = listener;
    }

    public void setOnSearch(Consumer<String> listener) {
        this.searchListener = listener;
    }

    public void setOnClearSearch(Runnable listener) {
        this.clearSearchListener = listener;
    }

    public void setOnRefresh(Runnable listener) {
        this.refreshListener = listener;
    }

    public String getSearchText() {
        return searchField == null ? "" : searchField.getText();
    }

    public void clearSearchField() {
        if (searchField != null) {
            searchField.setText("");
        }
    }

    private void registerInternalEvents(boolean searchable) {
        dvdTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            String code = getSelectedCode();
            if (code != null && rowSelectedListener != null) {
                rowSelectedListener.accept(code);
            }
        });

        refreshButton.addActionListener(event -> {
            if (refreshListener != null) {
                refreshListener.run();
            }
        });

        if (searchable) {
            searchButton.addActionListener(event -> {
                if (searchListener != null) {
                    searchListener.accept(getSearchText());
                }
            });

            clearSearchButton.addActionListener(event -> {
                clearSearchField();
                if (clearSearchListener != null) {
                    clearSearchListener.run();
                }
            });
        }
    }

    private String getSelectedCode() {
        int selectedRow = dvdTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        Object value = tableModel.getValueAt(selectedRow, 0);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }
}
