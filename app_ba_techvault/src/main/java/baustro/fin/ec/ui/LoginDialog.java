package baustro.fin.ec.ui;

import baustro.fin.ec.security.EncryptionUtil;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Pantalla de login al iniciar la app.
 * - Primera vez: pide crear contraseña maestra
 * - Siguientes veces: pide la contraseña para entrar
 * El hash se guarda en el mismo directorio que la BD.
 */
public class LoginDialog extends JDialog {

    private boolean authenticated = false;
    private static final String HASH_FILE;

    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        String dir = os.contains("win")
                ? home + File.separator + "AppData" + File.separator + "Local" + File.separator + "TechOpsManager"
                : home + File.separator + ".techopsmanager";
        boolean mkdirs = new File(dir).mkdirs();
        HASH_FILE = dir + File.separator + ".auth";
    }

    public LoginDialog() {
        super((Frame) null, "TechOps Manager", true);
        setUndecorated(false);
        setResizable(false);
        setSize(420, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { System.exit(0); }
        });
        getContentPane().setBackground(UIConstants.BG_BASE);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIConstants.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(36, 44, 36, 44)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridwidth = 1;

        // Logo / icono
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon lockIco = IconManager.getIcon(IconManager.ICON_LOCK, 56);
        if (lockIco != null && lockIco.getIconWidth() > 1) {
            logo.setIcon(lockIco);
        } else {
            logo.setText("TECHOPS");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
            logo.setForeground(UIConstants.ACCENT_BLUE);
        }

        JLabel appTitle = new JLabel("TechOps Manager", SwingConstants.CENTER);
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(UIConstants.TEXT_PRIMARY);

        boolean firstTime = !new File(HASH_FILE).exists();
        JLabel subtitle = new JLabel(firstTime
                ? "Crea tu contraseña maestra"
                : "Ingresa tu contraseña para continuar",
                SwingConstants.CENTER);
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPasswordField pwField = StyledComponents.styledPasswordField();
        pwField.setPreferredSize(new Dimension(300, 38));

        JPasswordField pwConfirm = StyledComponents.styledPasswordField();
        pwConfirm.setPreferredSize(new Dimension(300, 38));

        JLabel lblPw = fieldLabel(firstTime ? "Nueva contraseña:" : "Contraseña:");
        JLabel lblConfirm = fieldLabel("Confirmar contraseña:");

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(UIConstants.FONT_SMALL);
        errorLabel.setForeground(UIConstants.ACCENT_RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnLogin = StyledComponents.primaryButton(
                firstTime ? "Crear contraseña y entrar" : "Entrar",
                UIConstants.ACCENT_BLUE);
        btnLogin.setPreferredSize(new Dimension(300, 40));

        JLabel hint = new JLabel("Esta contraseña protege el acceso a la app", SwingConstants.CENTER);
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_MUTED);

        Runnable doLogin = () -> {
            String pw = new String(pwField.getPassword());
            if (pw.isEmpty()) { errorLabel.setText("Ingresa tu contraseña."); return; }

            if (firstTime) {
                String confirm = new String(pwConfirm.getPassword());
                if (!pw.equals(confirm)) { errorLabel.setText("Las contraseñas no coinciden."); return; }
                if (pw.length() < 4) { errorLabel.setText("Mínimo 4 caracteres."); return; }
                try {
                    saveHash(pw);
                    EncryptionUtil.setMasterPassword(pw);
                    authenticated = true;
                    dispose();
                } catch (Exception ex) { errorLabel.setText("Error guardando: " + ex.getMessage()); }
            } else {
                try {
                    if (checkHash(pw)) {
                        EncryptionUtil.setMasterPassword(pw);
                        authenticated = true;
                        dispose();
                    } else {
                        errorLabel.setText("Contraseña incorrecta.");
                        pwField.setText("");
                        pwField.requestFocus();
                    }
                } catch (Exception ex) { errorLabel.setText("Error: " + ex.getMessage()); }
            }
        };

        btnLogin.addActionListener(e -> doLogin.run());
        pwField.addActionListener(e -> { if (firstTime) pwConfirm.requestFocus(); else doLogin.run(); });
        pwConfirm.addActionListener(e -> doLogin.run());

        int row = 0;
        gbc.gridy = row++; card.add(logo, gbc);
        gbc.gridy = row++; card.add(appTitle, gbc);
        gbc.gridy = row++; card.add(subtitle, gbc);
        gbc.gridy = row++; gbc.insets = new Insets(12, 0, 2, 0); card.add(lblPw, gbc);
        gbc.gridy = row++; gbc.insets = new Insets(0, 0, 8, 0); card.add(pwField, gbc);
        if (firstTime) {
            gbc.gridy = row++; gbc.insets = new Insets(4, 0, 2, 0); card.add(lblConfirm, gbc);
            gbc.gridy = row++; gbc.insets = new Insets(0, 0, 8, 0); card.add(pwConfirm, gbc);
        }
        gbc.gridy = row++; gbc.insets = new Insets(4, 0, 4, 0); card.add(errorLabel, gbc);
        gbc.gridy = row++; gbc.insets = new Insets(4, 0, 12, 0); card.add(btnLogin, gbc);
        gbc.gridy = row;   gbc.insets = new Insets(0, 0, 0, 0);  card.add(hint, gbc);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIConstants.BG_BASE);
        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);

        // Focus
        SwingUtilities.invokeLater(pwField::requestFocusInWindow);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_SMALL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    public boolean isAuthenticated() { return authenticated; }

    // Hash helpers
    private void saveHash(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        Files.writeString(Path.of(HASH_FILE), Base64.getEncoder().encodeToString(hash));
    }

    private boolean checkHash(String password) throws Exception {
        String stored = Files.readString(Path.of(HASH_FILE)).trim();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return stored.equals(Base64.getEncoder().encodeToString(hash));
    }

    // Reuse StyledComponents inline
    private static JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setBackground(UIConstants.BG_SURFACE);
        pf.setForeground(UIConstants.TEXT_PRIMARY);
        pf.setCaretColor(UIConstants.TEXT_PRIMARY);
        pf.setFont(UIConstants.FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    private static JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIConstants.FONT_BODY);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    // Helper for use in StyledComponents
    private static class StyledComponents {
        static JPasswordField styledPasswordField() { return LoginDialog.styledPasswordField(); }

        static JButton primaryButton() {
            return primaryButton(null, null);
        }

        static JButton primaryButton(String t, Color c) { return LoginDialog.styledButton(t, c); }
    }
}
