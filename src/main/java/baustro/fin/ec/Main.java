package baustro.fin.ec;

import baustro.fin.ec.service.ConfigService;
import baustro.fin.ec.ui.MainWindow;
import baustro.fin.ec.ui.UITheme;
import baustro.fin.ec.ui.dialogs.SetupDialog;
import baustro.fin.ec.util.DatabaseManager;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        // Silenciar logs innecesarios de SQLite/JDBC
        Logger.getLogger("").setLevel(Level.WARNING);
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

        // Aplicar FlatLaf Dark
        try {
            FlatDarkLaf.setup();
            // Propiedades extra de FlatLaf para mejor rendering
            UIManager.put("Component.arc", 6);
            UIManager.put("Button.arc", 6);
            UIManager.put("TextComponent.arc", 4);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("TitlePane.unifiedBackground", false);
            UIManager.put("TitlePane.background", new java.awt.Color(0x080C14));
            UIManager.put("TitlePane.foreground", new java.awt.Color(0xE2E8F0));
        } catch (Exception e) {
            System.err.println("FlatLaf no disponible: " + e.getMessage());
        }

        // Defaults de tema oscuro
        UITheme.applyGlobalDefaults();

        // EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Init DB
                DatabaseManager.getInstance().getConnection();

                // Setup inicial (primera vez)
                if (!ConfigService.getInstance().isSetupDone()) {
                    SetupDialog setup = new SetupDialog(null);
                    setup.setVisible(true);
                }

                // Ventana principal
                MainWindow win = new MainWindow();
                win.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar:\n" + e.getMessage(),
                        "Error crítico", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
