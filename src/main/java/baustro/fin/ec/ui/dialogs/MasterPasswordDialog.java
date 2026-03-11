package baustro.fin.ec.ui.dialogs;

import baustro.fin.ec.service.ConfigService;
import baustro.fin.ec.util.CryptoUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Diálogo de autenticación para el módulo de contraseñas.
 * Solicita el master password y activa la sesión de cifrado.
 */
public class MasterPasswordDialog extends JDialog {

    private boolean authenticated = false;
    private int attempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    public MasterPasswordDialog(Frame owner) {
        super(owner, "Acceso al Módulo de Contraseñas", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUI() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(28, 36, 24, 36));

        // Icon + title
        JLabel icon  = new JLabel("🔐", SwingConstants.CENTER);
        icon.setFont(icon.getFont().deriveFont(36f));
        JLabel title = new JLabel("Módulo Protegido", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        JLabel sub   = new JLabel("Ingresa tu contraseña maestra para continuar.", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        sub.setFont(sub.getFont().deriveFont(12f));

        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        headerPanel.add(icon); headerPanel.add(title); headerPanel.add(sub);

        // Password field
        JPasswordField pfPass = new JPasswordField(22);
        pfPass.setPreferredSize(new Dimension(260, 36));
        pfPass.setFont(pfPass.getFont().deriveFont(14f));

        JLabel lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setForeground(new Color(200, 50, 50));
        lblError.setFont(lblError.getFont().deriveFont(11.5f));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel fieldRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldRow.add(new JLabel("Contraseña: "));
        fieldRow.add(pfPass);
        centerPanel.add(fieldRow);
        centerPanel.add(lblError);

        // Buttons
        JButton btnOk     = new JButton("Entrar");
        JButton btnCancel = new JButton("Cancelar");
        btnOk.setPreferredSize(new Dimension(110, 34));
        btnCancel.setPreferredSize(new Dimension(110, 34));

        Runnable doAuth = () -> {
            String input = new String(pfPass.getPassword());
            if (input.isBlank()) return;

            String storedHash = ConfigService.getInstance().get("master_password_hash");
            String salt       = ConfigService.getInstance().get("master_password_salt");

            if (storedHash == null || storedHash.isEmpty()) {
                // No se configuró aún
                JOptionPane.showMessageDialog(this,
                    "No tienes una contraseña maestra configurada.\nVe a Configuración para crearla.",
                    "Sin configuración", JOptionPane.WARNING_MESSAGE);
                dispose();
                return;
            }

            if (CryptoUtil.verifyMasterPassword(input, storedHash, salt)) {
                try {
                    CryptoUtil.loadSessionKey(input, salt);
                    authenticated = true;
                    dispose();
                } catch (Exception ex) {
                    lblError.setText("Error al inicializar sesión: " + ex.getMessage());
                }
            } else {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                if (remaining <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Demasiados intentos fallidos.", "Acceso bloqueado", JOptionPane.ERROR_MESSAGE);
                    dispose();
                } else {
                    lblError.setText("Contraseña incorrecta. Intentos restantes: " + remaining);
                    pfPass.setText("");
                    pfPass.requestFocus();
                }
            }
        };

        btnOk.addActionListener(e -> doAuth.run());
        btnCancel.addActionListener(e -> dispose());

        pfPass.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doAuth.run();
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.add(btnCancel); btnPanel.add(btnOk);

        panel.add(headerPanel,  BorderLayout.NORTH);
        panel.add(centerPanel,  BorderLayout.CENTER);
        panel.add(btnPanel,     BorderLayout.SOUTH);

        setContentPane(panel);
        getRootPane().setDefaultButton(btnOk);
    }

    public boolean isAuthenticated() { return authenticated; }
}
