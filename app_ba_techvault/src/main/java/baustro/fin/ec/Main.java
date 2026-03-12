package baustro.fin.ec;

import baustro.fin.ec.ui.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Look & Feel - FlatLaf Dark (si disponible) como base
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // usar default
            }
        }

        // Ajustes globales de UI
        UIManager.put("OptionPane.background",          new java.awt.Color(36, 45, 61));
        UIManager.put("Panel.background",               new java.awt.Color(36, 45, 61));
        UIManager.put("OptionPane.messageForeground",   new java.awt.Color(241, 245, 249));
        UIManager.put("Button.arc",                     8);
        UIManager.put("Component.arc",                  6);
        UIManager.put("TextComponent.arc",              6);

        // Lanzar en EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}