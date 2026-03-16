package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ContrasenaDAO;
import baustro.fin.ec.model.Contrasena;
import baustro.fin.ec.security.EncryptionUtil;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContrasenaPanel extends JPanel {

    private final ContrasenaDAO dao = new ContrasenaDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private List<Contrasena> allData = new ArrayList<>();
    private static final String[] CATEGORIAS = {
            "BD", "Servidor", "App", "VPN", "Email", "API", "Web", "Sistema"
    };

    public ContrasenaPanel() {
        setLayout(new CardLayout());
        setBackground(UIConstants.BG_BASE);
        buildLockScreen();
        buildMainContent();
        // Mostrar lock screen al iniciar
        showLock();
    }

    //LOCK SCREEN
    private void buildLockScreen() {
        // Lock de módulo — pide clave al entrar
        JPanel lockScreen = new JPanel(new GridBagLayout());
        lockScreen.setBackground(UIConstants.BG_BASE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIConstants.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);

        //Icono
        JLabel icoLbl = new JLabel();
        icoLbl.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon lockIco = IconManager.getIcon(IconManager.ICON_LOCK, 52);
        if (lockIco != null && lockIco.getIconWidth() > 1) icoLbl.setIcon(lockIco);
        else { icoLbl.setText("[LOCK]"); icoLbl.setFont(UIConstants.FONT_TITLE); icoLbl.setForeground(UIConstants.ACCENT_BLUE); }

        JLabel title = new JLabel("Gestor de Contraseñas", SwingConstants.CENTER);
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel sub = new JLabel("Ingresa tu contrasena maestra para acceder", SwingConstants.CENTER);
        sub.setFont(UIConstants.FONT_BODY); sub.setForeground(UIConstants.TEXT_MUTED);

        JPasswordField pwField = new JPasswordField();
        pwField.setBackground(UIConstants.BG_SURFACE); pwField.setForeground(UIConstants.TEXT_PRIMARY);
        pwField.setCaretColor(UIConstants.TEXT_PRIMARY); pwField.setFont(UIConstants.FONT_BODY);
        pwField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        pwField.setPreferredSize(new Dimension(280, 38));

        JLabel errLbl = new JLabel(" ", SwingConstants.CENTER);
        errLbl.setFont(UIConstants.FONT_SMALL); errLbl.setForeground(UIConstants.ACCENT_RED);

        JButton btnUnlock = new JButton("Desbloquear");
        btnUnlock.setBackground(UIConstants.ACCENT_BLUE); btnUnlock.setForeground(Color.WHITE);
        btnUnlock.setFont(UIConstants.FONT_BODY); btnUnlock.setFocusPainted(false);
        btnUnlock.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        btnUnlock.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUnlock.setPreferredSize(new Dimension(280, 40));

        Runnable doUnlock = () -> {
            String pw = new String(pwField.getPassword());
            if (pw.isEmpty()) { errLbl.setText("Ingresa tu contrasena."); return; }
            // Verificar contra el hash guardado
            if (checkHash(pw)) {
                // Asegurar que EncryptionUtil tiene la clave
                if (!EncryptionUtil.hasMasterPassword()) {
                    EncryptionUtil.setMasterPassword(pw);
                }
                showMain();
                loadData();
            } else {
                errLbl.setText("Contrasena incorrecta.");
                pwField.setText("");
                pwField.requestFocus();
            }
        };

        btnUnlock.addActionListener(e -> doUnlock.run());
        pwField.addActionListener(e -> doUnlock.run());

        int r = 0;
        g.gridy = r++; card.add(icoLbl, g);
        g.gridy = r++; card.add(title, g);
        g.gridy = r++; g.insets = new Insets(2,0,12,0); card.add(sub, g);
        g.gridy = r++; g.insets = new Insets(4,0,4,0); card.add(pwField, g);
        g.gridy = r++; card.add(errLbl, g);
        g.gridy = r;   card.add(btnUnlock, g);

        lockScreen.add(card);
        add(lockScreen, "lock");

        // Focus cuando se muestre
        lockScreen.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                SwingUtilities.invokeLater(pwField::requestFocusInWindow);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
    }

    private boolean checkHash(String password) {
        try {
            String os   = System.getProperty("os.name", "").toLowerCase();
            String home = System.getProperty("user.home");
            String dir  = os.contains("win")
                    ? home + File.separator + "AppData" + File.separator + "Local" + File.separator + "TechOpsManager"
                    : home + File.separator + ".techopsmanager";
            String hashFile = dir + File.separator + ".auth";
            if (!new File(hashFile).exists()) {
                // Si no hay hash guardado, cualquier clave es valida (fallback)
                EncryptionUtil.setMasterPassword(password);
                return true;
            }
            String stored = Files.readString(Path.of(hashFile)).trim();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return stored.equals(Base64.getEncoder().encodeToString(hash));
        } catch (Exception e) { return false; }
    }

    private void showLock() { ((CardLayout) getLayout()).show(this, "lock"); }
    private void showMain() { ((CardLayout) getLayout()).show(this, "main"); }

    //MAIN CONTENT
    private void buildMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(UIConstants.BG_BASE);

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Gestor de Contraseñas");
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_UNLOCK, 22);
        if (ico != null && ico.getIconWidth() > 1) { title.setIcon(ico); title.setIconTextGap(8); }
        JButton btnLock   = new JButton("Bloquear");
        btnLock.setBackground(UIConstants.BG_CARD); btnLock.setForeground(UIConstants.TEXT_MUTED);
        btnLock.setFont(UIConstants.FONT_SMALL); btnLock.setFocusPainted(false);
        btnLock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        btnLock.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLock.addActionListener(e -> showLock());

        titlePane.add(title); titlePane.add(btnLock);

        hsf = new HeaderSearchFilter(
                "Buscar titulo, usuario, categoría...",
                new HeaderSearchFilter.ComboConfig("Categoría", CATEGORIAS, "Todas"),
                new HeaderSearchFilter.ComboConfig("Ordenar", new String[]{"Titulo Z-A","Categoría"}, "Titulo A-Z")
        ).onChanged(this::applyFilters);

        header.add(titlePane, BorderLayout.WEST);
        header.add(hsf, BorderLayout.EAST);

        // Stats bar
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        statsLabel = new JLabel();
        statsLabel.setFont(UIConstants.FONT_SMALL); statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        // Table
        String[] cols = {"#", "Titulo", "Usuario", "Categoría", "URL"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        // Categoría coloreada
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(sel ? UIConstants.ACCENT_BLUE : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER));
                setForeground(UIConstants.ACCENT_CYAN);
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) editSelected(); }
        });

        // Bottom bar
        JButton btnNew    = StyledComponents.addButton("Nueva");
        btnNew.addActionListener(e -> openForm(null));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_BASE);
        JButton btnEdit     = StyledComponents.editButton("Editar");
        JButton btnDelete   = StyledComponents.dangerButton("Eliminar");
        JButton btnCopyPass = StyledComponents.copyButton("Copiar Contrasena");
        JButton btnCopyUser = StyledComponents.copyButton("Copiar Usuario");
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnCopyPass.addActionListener(e -> copyField("pass"));
        btnCopyUser.addActionListener(e -> copyField("user"));
        bottom.add(btnNew); bottom.add(btnEdit); bottom.add(btnDelete); bottom.add(btnCopyPass); bottom.add(btnCopyUser);

        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        mainContent.add(header, BorderLayout.NORTH);
        mainContent.add(center, BorderLayout.CENTER);
        mainContent.add(bottom, BorderLayout.SOUTH);
        add(mainContent, "main");
    }

    private void loadData() {
        try { allData = dao.findAll(); applyFilters(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error cargando: " + ex.getMessage()); }
    }

    private void applyFilters() {
        String q    = hsf.getQuery().toLowerCase();
        String cat  = hsf.getFilter(0);
        String sort = hsf.getFilter(1);
        Stream<Contrasena> s = allData.stream();
        if (!q.isEmpty())   s = s.filter(c -> nv(c.getTitulo(), q) || nv(c.getUsuario(), q) || nv(c.getCategoria(), q));
        if (!cat.isEmpty()) s = s.filter(c -> cat.equals(c.getCategoria()));
        Comparator<Contrasena> cmp = switch (sort) {
            case "Categoría"  -> Comparator.comparing(c -> nvl(c.getCategoria()));
            case "Titulo Z-A" -> Comparator.comparing((Contrasena c) -> nvl(c.getTitulo())).reversed();
            default           -> Comparator.comparing(c -> nvl(c.getTitulo()));
        };
        List<Contrasena> res = s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i = 1;
        for (Contrasena c : res)
            tableModel.addRow(new Object[]{i++, c.getTitulo(), c.getUsuario(), c.getCategoria(), c.getUrl()});

        Map<String, Long> byCat = res.stream()
                .filter(c -> c.getCategoria() != null && !c.getCategoria().isEmpty())
                .collect(Collectors.groupingBy(Contrasena::getCategoria, Collectors.counting()));
        String catStr = byCat.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .reduce((a, b) -> a + " | " + b).orElse("");
        statsLabel.setText("  Total: " + res.size() + (catStr.isEmpty() ? "" : "   —   " + catStr));
    }

    private boolean nv(String f, String q) { return f != null && f.toLowerCase().contains(q); }
    private String nvl(String s) { return s == null ? "" : s; }

    private Contrasena getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una entrada."); return null; }
        String tit = (String) tableModel.getValueAt(row, 1);
        return allData.stream().filter(c -> tit.equals(c.getTitulo())).findFirst().orElse(null);
    }

    private void editSelected() { Contrasena c = getSelected(); if (c != null) openForm(c); }

    private void deleteSelected() {
        Contrasena c = getSelected(); if (c == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Eliminar \"" + c.getTitulo() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void copyField(String f) {
        Contrasena c = getSelected(); if (c == null) return;
        // Verificar que tenemos la clave antes de descifrar
        if (!EncryptionUtil.hasMasterPassword()) {
            JOptionPane.showMessageDialog(this, "Clave maestra no disponible. Bloquea y vuelve a ingresar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String val = f.equals("pass") ? EncryptionUtil.decrypt(c.getContrasenaCifrada()) : c.getUsuario();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(val), null);
            JOptionPane.showMessageDialog(this,
                    f.equals("pass") ? "Contrasena copiada al portapapeles." : "Usuario copiado: " + val,
                    "Copiado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al descifrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openForm(Contrasena existing) {
        // Verificar clave maestra ANTES de abrir el formulario
        if (!EncryptionUtil.hasMasterPassword()) {
            JOptionPane.showMessageDialog(this,
                    "No se puede guardar: la clave maestra no esta configurada.\nCierra y vuelve a abrir la sesión.",
                    "Error de seguridad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nueva Contrasena" : "Editar Contrasena", true);
        d.setSize(480, 430); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 6, 6, 6); gbc.weightx = 1;

        JTextField   fTit   = StyledComponents.styledTextField("Nombre del servicio o sistema");
        JTextField   fUser  = StyledComponents.styledTextField("usuario@dominio o nombre de usuario");
        JPasswordField fPass = StyledComponents.styledPasswordField();
        JTextField   fUrl   = StyledComponents.styledTextField("https://... o IP del servidor");
        JComboBox<String> fCat = new JComboBox<>(CATEGORIAS);
        fCat.setBackground(UIConstants.BG_SURFACE);
        fCat.setForeground(UIConstants.TEXT_PRIMARY);
        fCat.setFont(UIConstants.FONT_BODY);
        fCat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JTextArea    fNotas = StyledComponents.styledTextArea(3, 20);

        JCheckBox showPw = new JCheckBox("Mostrar contrasena");
        showPw.setForeground(UIConstants.TEXT_SECONDARY);
        showPw.setBackground(UIConstants.BG_CARD);
        showPw.setFont(UIConstants.FONT_SMALL);
        showPw.addItemListener(e -> fPass.setEchoChar(showPw.isSelected() ? (char) 0 : '*'));

        if (existing != null) {
            fTit.setText(existing.getTitulo());
            fUser.setText(existing.getUsuario());
            try { fPass.setText(EncryptionUtil.decrypt(existing.getContrasenaCifrada())); }
            catch (Exception ex) { fPass.setText(""); }
            fUrl.setText(existing.getUrl());
            fCat.setSelectedItem(existing.getCategoria());
            fNotas.setText(existing.getNotas());
        }

        int r = 0;
        row2(form, gbc, r++, "Titulo *", fTit);
        row2(form, gbc, r++, "Usuario", fUser);
        row2(form, gbc, r++, "Contrasena *", fPass);
        gbc.gridy = r++ * 2; gbc.gridx = 0; gbc.gridwidth = 2; form.add(showPw, gbc);
        row2(form, gbc, r++, "URL / Sistema", fUrl);
        row2(form, gbc, r++, "Categoría", fCat);
        row2(form, gbc, r, "Notas", new JScrollPane(fNotas));

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bp.setBackground(UIConstants.BG_BASE);
        JButton bS = StyledComponents.successButton("Guardar");
        JButton bC = StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e -> d.dispose());

        bS.addActionListener(e -> {
            String pw = new String(fPass.getPassword());
            if (fTit.getText().trim().isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Titulo y contrasena son obligatorios.");
                return;
            }
            if (!EncryptionUtil.hasMasterPassword()) {
                JOptionPane.showMessageDialog(d, "Error: clave maestra no disponible.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Contrasena c = existing != null ? existing : new Contrasena();
                c.setTitulo(fTit.getText().trim());
                c.setUsuario(fUser.getText().trim());
                c.setContrasenaCifrada(EncryptionUtil.encrypt(pw));
                c.setUrl(fUrl.getText().trim());
                c.setCategoria((String) fCat.getSelectedItem());
                c.setNotas(fNotas.getText().trim());
                if (existing == null) dao.insert(c); else dao.update(c);
                loadData();
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bp.add(bS); bp.add(bC);
        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_CARD); sp.setBorder(null);
        d.add(sp, BorderLayout.CENTER); d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void row2(JPanel p, GridBagConstraints g, int row, String lbl, Component c) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 2; g.weightx = 1;
        JLabel l = new JLabel(lbl);
        l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY);
        p.add(l, g);
        g.gridy = row * 2 + 1; p.add(c, g);
    }

}
