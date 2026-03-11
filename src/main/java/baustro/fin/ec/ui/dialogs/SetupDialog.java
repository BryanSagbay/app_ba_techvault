package baustro.fin.ec.ui.dialogs;

import baustro.fin.ec.service.ConfigService;
import baustro.fin.ec.ui.UITheme;
import baustro.fin.ec.util.CryptoUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SetupDialog extends JDialog {

    private boolean completed = false;

    public SetupDialog(Frame owner) {
        super(owner, "Configuración Inicial — TechOps Manager", true);
        buildUI();
        pack();
        setMinimumSize(new Dimension(480, 420));
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DEEPEST);
        header.setBorder(new EmptyBorder(24, 32, 20, 32));
        JLabel icon  = new JLabel("⚙  TechOps Manager");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Configuración inicial del sistema");
        sub.setFont(UITheme.FONT_UI);
        sub.setForeground(UITheme.TEXT_DIM);
        JPanel ht = new JPanel(new GridLayout(2,1,0,4));
        ht.setBackground(UITheme.BG_DEEPEST);
        ht.add(icon); ht.add(sub);
        header.add(ht);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_DARK);
        form.setBorder(new EmptyBorder(24, 32, 16, 32));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7,6,7,6); g.anchor=GridBagConstraints.WEST;

        JPasswordField pfPass    = UITheme.passwordField(22);
        JPasswordField pfConfirm = UITheme.passwordField(22);

        JLabel lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.RED);

        g.gridx=0;g.gridy=0;g.gridwidth=1;
        JLabel l1=new JLabel("Contraseña maestra:");l1.setFont(UITheme.FONT_SMALL);l1.setForeground(UITheme.TEXT_DIM);
        form.add(l1,g);
        g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;form.add(pfPass,g);
        g.gridx=0;g.gridy=1;g.fill=GridBagConstraints.NONE;g.weightx=0;
        JLabel l2=new JLabel("Confirmar contraseña:");l2.setFont(UITheme.FONT_SMALL);l2.setForeground(UITheme.TEXT_DIM);
        form.add(l2,g);
        g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;form.add(pfConfirm,g);
        g.gridx=0;g.gridy=2;g.gridwidth=2;form.add(lblError,g);

        // Info box
        JPanel infoBox = new JPanel(new BorderLayout());
        infoBox.setBackground(new Color(0x1C1A08));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.YELLOW_BG),
            new EmptyBorder(10,14,10,14)));
        JLabel infoLbl = new JLabel(
            "<html><b style='color:#FCD34D'>⚠  Importante:</b>" +
            "<br><span style='color:#94A3B8'>Esta contraseña NO se puede recuperar. " +
            "Se usa solo para el módulo de contraseñas.</span></html>");
        infoLbl.setFont(UITheme.FONT_SMALL);
        infoBox.add(infoLbl);

        JPanel infoWrap = new JPanel(new BorderLayout());
        infoWrap.setBackground(UITheme.BG_DARK);
        infoWrap.setBorder(new EmptyBorder(0,32,16,32));
        infoWrap.add(infoBox);

        // Buttons
        JButton btnOk     = UITheme.primaryButton("Guardar y continuar");
        JButton btnSaltar = UITheme.secondaryButton("Configurar después");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,14));
        btnPanel.setBackground(UITheme.BG_DEEPEST);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
        btnPanel.add(btnSaltar); btnPanel.add(btnOk);

        btnOk.addActionListener(e -> {
            String pass    = new String(pfPass.getPassword());
            String confirm = new String(pfConfirm.getPassword());
            if (pass.length() < 6) { lblError.setText("Mínimo 6 caracteres."); return; }
            if (!pass.equals(confirm)) { lblError.setText("Las contraseñas no coinciden."); return; }
            try {
                String salt = CryptoUtil.generateSalt();
                String hash = CryptoUtil.hashMasterPassword(pass, salt);
                ConfigService.getInstance().set("master_password_hash", hash);
                ConfigService.getInstance().set("master_password_salt", salt);
                ConfigService.getInstance().set("setup_done", "true");
                completed = true; dispose();
            } catch (Exception ex) { lblError.setText("Error: " + ex.getMessage()); }
        });
        btnSaltar.addActionListener(e -> {
            ConfigService.getInstance().set("setup_done", "true"); dispose();
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG_DARK);
        center.add(form, BorderLayout.NORTH);
        center.add(infoWrap, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(root);
    }

    public boolean isCompleted() { return completed; }
}
