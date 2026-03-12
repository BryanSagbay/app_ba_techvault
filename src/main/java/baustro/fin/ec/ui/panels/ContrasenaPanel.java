package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ContrasenaDAO;
import baustro.fin.ec.model.Contrasena;
import baustro.fin.ec.security.EncryptionUtil;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ContrasenaPanel extends JPanel {

    private final ContrasenaDAO dao = new ContrasenaDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private boolean unlocked = false;

    public ContrasenaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        // ── Lock screen ─────────────────────────────────────────
        if (!unlocked) {
            showLockScreen();
            return;
        }
        buildMainUI();
    }

    private void showLockScreen() {
        removeAll();
        JPanel lock = new JPanel(new GridBagLayout());
        lock.setBackground(UIConstants.BG_DARK);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIConstants.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Icono de candado desde resources/icons/lock.png
        ImageIcon lockImg = IconManager.getIcon(IconManager.ICON_LOCK, 64);
        JLabel icon = new JLabel(lockImg != null && lockImg.getIconWidth() > 1 ? lockImg : null, SwingConstants.CENTER);
        icon.setText(lockImg == null || lockImg.getIconWidth() <= 1 ? "LOCK" : "");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        icon.setForeground(baustro.fin.ec.ui.UIConstants.ACCENT_BLUE);
        JLabel title = new JLabel("Gestor de Contraseñas", SwingConstants.CENTER);
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel sub = new JLabel("Ingrese la clave maestra para acceder", SwingConstants.CENTER);
        sub.setFont(UIConstants.FONT_BODY);
        sub.setForeground(UIConstants.TEXT_SECONDARY);

        JPasswordField pwField = StyledComponents.styledPasswordField();
        pwField.setPreferredSize(new Dimension(280, 36));

        JButton btnUnlock = StyledComponents.primaryButton("Desbloquear", UIConstants.ACCENT_BLUE);
        JLabel hint = StyledComponents.muted("La clave protege todas tus contraseñas con AES-256");

        Runnable unlock = () -> {
            char[] pw = pwField.getPassword();
            if (pw.length == 0) {
                JOptionPane.showMessageDialog(this, "Ingrese la clave maestra.");
                return;
            }
            EncryptionUtil.setMasterPassword(new String(pw));
            unlocked = true;
            buildMainUI();
        };

        btnUnlock.addActionListener(e -> unlock.run());
        pwField.addActionListener(e -> unlock.run());

        gbc.gridy = 0; card.add(icon, gbc);
        gbc.gridy = 1; card.add(title, gbc);
        gbc.gridy = 2; card.add(sub, gbc);
        gbc.gridy = 3; card.add(pwField, gbc);
        gbc.gridy = 4; card.add(btnUnlock, gbc);
        gbc.gridy = 5; card.add(hint, gbc);

        lock.add(card);
        add(lock, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private void buildMainUI() {
        removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Gestor de Contrasenas");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico2 = IconManager.getIcon(IconManager.ICON_PASSWORD, 24);
        if (_ico2 != null && _ico2.getIconWidth() > 1) title.setIcon(_ico2);
        title.setIconTextGap(10);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = StyledComponents.searchBar("Buscar título, usuario, categoría...");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doSearch(); }
        });

        JButton btnNew  = StyledComponents.addButton("Nueva Contrasena");
        JButton btnLock = StyledComponents.primaryButton("Bloquear", UIConstants.ACCENT_RED);
        btnNew.addActionListener(e -> openForm(null));
        btnLock.addActionListener(e -> { unlocked = false; EncryptionUtil.clearMasterPassword(); showLockScreen(); });
        actions.add(searchField); actions.add(btnNew); actions.add(btnLock);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Table - sin mostrar contraseñas
        String[] cols = {"#", "Título", "Usuario", "Categoría", "URL"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JScrollPane scroll = StyledComponents.darkScrollPane(table);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_DARK);
        JButton btnEdit      = StyledComponents.editButton("Editar");
        JButton btnDelete    = StyledComponents.dangerButton("Eliminar");
        JButton btnCopyPass  = StyledComponents.copyButton("Copiar Contrasena");
        JButton btnCopyUser  = StyledComponents.copyButton("Copiar Usuario");

        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnCopyPass.addActionListener(e -> copyField("password"));
        btnCopyUser.addActionListener(e -> copyField("user"));

        bottom.add(btnEdit); bottom.add(btnDelete);
        bottom.add(btnCopyPass); bottom.add(btnCopyUser);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadData();
        revalidate(); repaint();
    }

    private void loadData() {
        try {
            refreshTable(dao.findAll());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        try {
            refreshTable(q.isEmpty() ? dao.findAll() : dao.search(q));
        } catch (Exception ex) { /* ignore */ }
    }

    private void refreshTable(List<Contrasena> data) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Contrasena c : data) {
            tableModel.addRow(new Object[]{i++, c.getTitulo(), c.getUsuario(), c.getCategoria(), c.getUrl()});
        }
    }

    private Contrasena getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una entrada."); return null; }
        String titulo = (String) tableModel.getValueAt(row, 1);
        String usuario = (String) tableModel.getValueAt(row, 2);
        try {
            return dao.findAll().stream()
                    .filter(c -> titulo.equals(c.getTitulo()) && (usuario == null || usuario.equals(c.getUsuario())))
                    .findFirst().orElse(null);
        } catch (Exception ex) { return null; }
    }

    private void editSelected() {
        Contrasena c = getSelected();
        if (c != null) openForm(c);
    }

    private void deleteSelected() {
        Contrasena c = getSelected();
        if (c == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar " + c.getTitulo() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void copyField(String field) {
        Contrasena c = getSelected();
        if (c == null) return;
        try {
            String val;
            if ("password".equals(field)) {
                val = EncryptionUtil.decrypt(c.getContrasenaCifrada());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(val), null);
                JOptionPane.showMessageDialog(this, "Contrasena copiada", "Copiado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                val = c.getUsuario();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(val), null);
                JOptionPane.showMessageDialog(this, "Usuario copiado: " + val, "Copiado", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error descifrando: clave maestra incorrecta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openForm(Contrasena existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nueva Contraseña" : "Editar Contraseña", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 6, 6, 6); gbc.weightx = 1;

        JTextField fTitulo   = StyledComponents.styledTextField("Nombre del servicio / sistema");
        JTextField fUsuario  = StyledComponents.styledTextField("usuario@dominio");
        JPasswordField fPass = StyledComponents.styledPasswordField();
        JTextField fUrl      = StyledComponents.styledTextField("https:// o dirección");
        JTextField fCat      = StyledComponents.styledTextField("Ej: BD, Servidor, App, VPN");
        JTextArea  fNotas    = StyledComponents.styledTextArea(3, 20);

        // Show/hide password
        JCheckBox showPw = new JCheckBox("Mostrar contraseña");
        showPw.setForeground(UIConstants.TEXT_SECONDARY);
        showPw.setBackground(UIConstants.BG_PANEL);
        showPw.setFont(UIConstants.FONT_SMALL);
        showPw.addItemListener(e ->
                fPass.setEchoChar(showPw.isSelected() ? (char) 0 : '●'));

        if (existing != null) {
            fTitulo.setText(existing.getTitulo());
            fUsuario.setText(existing.getUsuario());
            try { fPass.setText(EncryptionUtil.decrypt(existing.getContrasenaCifrada())); }
            catch (Exception ex) { fPass.setText(""); }
            fUrl.setText(existing.getUrl());
            fCat.setText(existing.getCategoria());
            fNotas.setText(existing.getNotas());
        }

        int r = 0;
        addRow2(form, gbc, r++, "Título *", fTitulo);
        addRow2(form, gbc, r++, "Usuario", fUsuario);
        addRow2(form, gbc, r++, "Contraseña *", fPass);
        gbc.gridy = r++ * 2; gbc.gridx = 0; gbc.gridwidth = 2; form.add(showPw, gbc);
        addRow2(form, gbc, r++, "URL / Sistema", fUrl);
        addRow2(form, gbc, r++, "Categoría", fCat);
        addFull2(form, gbc, r, "Notas", new JScrollPane(fNotas));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_DARK);
        JButton btnSave = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String tit = fTitulo.getText().trim();
            String pw  = new String(fPass.getPassword());
            if (tit.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Título y contraseña son obligatorios.");
                return;
            }
            try {
                String enc = EncryptionUtil.encrypt(pw);
                Contrasena c = existing != null ? existing : new Contrasena();
                c.setTitulo(tit); c.setUsuario(fUsuario.getText().trim());
                c.setContrasenaCifrada(enc); c.setUrl(fUrl.getText().trim());
                c.setCategoria(fCat.getText().trim()); c.setNotas(fNotas.getText().trim());
                if (existing == null) dao.insert(c); else dao.update(c);
                loadData(); dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error guardando: " + ex.getMessage());
            }
        });
        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_PANEL); sp.setBorder(null);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow2(JPanel p, GridBagConstraints gbc, int row, String label, Component c) {
        gbc.gridy = row * 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        JLabel l = new JLabel(label); l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY);
        p.add(l, gbc); gbc.gridy = row * 2 + 1; p.add(c, gbc);
    }

    private void addFull2(JPanel p, GridBagConstraints gbc, int row, String label, Component c) {
        addRow2(p, gbc, row, label, c);
    }
}
