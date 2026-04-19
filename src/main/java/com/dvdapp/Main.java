package com.dvdapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.dvdapp.controller.MaterialController;
import com.dvdapp.dao.MySqlCDDao;
import com.dvdapp.dao.MySqlDVDDao;
import com.dvdapp.dao.MySqlLibroDao;
import com.dvdapp.dao.MySqlRevistaDao;
import com.dvdapp.view.MainWindow;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Files.createDirectories(Path.of("logs"));
                System.setProperty("log4j.configurationFile", "log4j.properties");
                UIManager.setLookAndFeel(new FlatLightLaf());
                MaterialController controller = new MaterialController(
                    new MySqlDVDDao(),
                    new MySqlLibroDao(),
                    new MySqlRevistaDao(),
                    new MySqlCDDao()
                );
                MainWindow mainWindow = new MainWindow(controller);
                mainWindow.setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        """
                        No se pudo iniciar la aplicacion porque fallo la conexion con la base de datos.
                        Verifique MySQL, credenciales y variables de entorno.

                        Detalle: %s
                        """.formatted(ex.getMessage()),
                    "Error de inicio",
                        JOptionPane.ERROR_MESSAGE
                );
                } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    null,
                    "No se pudo inicializar la carpeta de logs: " + ex.getMessage(),
                    "Error de inicio",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (UnsupportedLookAndFeelException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo aplicar el estilo visual de la aplicacion: " + ex.getMessage(),
                        "Error de inicio",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        null,
                    "Error inesperado al iniciar: " + ex.getMessage(),
                    "Error de inicio",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
