package baustro.fin.ec;

import baustro.fin.ec.ui.LoginDialog;
import baustro.fin.ec.ui.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        UIManager.put("OptionPane.background", new Color(36, 45, 61));
        UIManager.put("Panel.background", new Color(36, 45, 61));
        UIManager.put("OptionPane.messageForeground", new Color(241, 245, 249));
        UIManager.put("Button.arc",  8);
        UIManager.put("Component.arc", 6);
        UIManager.put("TextComponent.arc", 6);

        SwingUtilities.invokeLater(() -> {
            // 1. Mostrar login
            LoginDialog login = new LoginDialog();
            login.setVisible(true);

            // 2. Si no se autentico
            if (!login.isAuthenticated()) {
                System.exit(0);
                return;
            }

            // 3. Abrir ventana principal
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
