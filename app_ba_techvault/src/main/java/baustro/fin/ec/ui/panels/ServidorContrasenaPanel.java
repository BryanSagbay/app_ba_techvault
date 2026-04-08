package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ServidorDAO;
import baustro.fin.ec.model.Servidor;
import baustro.fin.ec.security.EncryptionUtil;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.util.stream.Stream;

/**
 * Panel Servidores con contraseña integrada.
 * Layout: lock → Main * Main: header (título + botones) | tabla | filtros (abajo)
 */
public class ServidorContrasenaPanel extends JPanel {

    private final ServidorDAO dao = new ServidorDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private HeaderSearchFilter hsf;
    private JLabel            statsLabel;
    private List<Servidor>    allData = new ArrayList<>();

    //
    public ServidorContrasenaPanel() {
        setLayout(new CardLayout());
        setBackground(UIConstants.BG_BASE);
        buildLockScreen();
        buildMainContent();
        showLock();
    }

    //  LOCK SCREEN

    private void buildLockScreen() {
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

        // Icono
        JLabel icoLbl = new JLabel();
        icoLbl.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon lockIco = IconManager.getIcon(IconManager.ICON_LOCK, 52);
        if (lockIco != null && lockIco.getIconWidth() > 1) icoLbl.setIcon(lockIco);
        else { icoLbl.setText("🔒"); icoLbl.setFont(UIConstants.FONT_TITLE); icoLbl.setForeground(UIConstants.ACCENT_BLUE); }

        JLabel title = new JLabel("Servidores", SwingConstants.CENTER);
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel sub = new JLabel("Ingresa tu contraseña maestra para acceder", SwingConstants.CENTER);
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
            if (pw.isEmpty()) { errLbl.setText("Ingresa tu contraseña."); return; }
            if (checkHash(pw)) {
                if (!EncryptionUtil.hasMasterPassword()) EncryptionUtil.setMasterPassword(pw);
                showMain();
                loadData();
            } else {
                errLbl.setText("Contraseña incorrecta.");
                pwField.setText(""); pwField.requestFocus();
            }
        };
        btnUnlock.addActionListener(e -> doUnlock.run());
        pwField.addActionListener(e -> doUnlock.run());

        int r = 0;
        g.gridy = r++; card.add(icoLbl, g);
        g.gridy = r++; card.add(title, g);
        g.gridy = r++; g.insets = new Insets(2, 0, 12, 0); card.add(sub, g);
        g.gridy = r++; g.insets = new Insets(4, 0, 4, 0);  card.add(pwField, g);
        g.gridy = r++; card.add(errLbl, g);
        g.gridy = r;   card.add(btnUnlock, g);

        lockScreen.add(card);
        add(lockScreen, "lock");

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

    //  MAIN CONTENT

    private void buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIConstants.BG_BASE);

        //  HEADER: título + botones
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        // Título + botón bloquear
        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Servidores");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_SERVIDOR, 22);
        if (ico != null && ico.getIconWidth() > 1) { title.setIcon(ico); title.setIconTextGap(8); }

        JButton btnLock = new JButton("Bloquear");
        btnLock.setBackground(UIConstants.BG_CARD); btnLock.setForeground(UIConstants.TEXT_MUTED);
        btnLock.setFont(UIConstants.FONT_SMALL); btnLock.setFocusPainted(false);
        btnLock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        btnLock.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLock.addActionListener(e -> showLock());

        titlePane.add(title);
        titlePane.add(btnLock);

        // Botones CRUD — lado derecho
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPane.setOpaque(false);

        JButton btnNew    = StyledComponents.addButton("Nuevo");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnCopyIP = StyledComponents.copyButton("Copiar IP");
        JButton btnCopyUs = StyledComponents.copyButton("Copiar Usuario");
        JButton btnCopyPw = StyledComponents.copyButton("Copiar Contraseña");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");

        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnCopyIP.addActionListener(e -> copyField("ip"));
        btnCopyUs.addActionListener(e -> copyField("user"));
        btnCopyPw.addActionListener(e -> copyField("pass"));
        btnDelete.addActionListener(e -> deleteSelected());

        btnPane.add(btnNew); btnPane.add(btnEdit);
        btnPane.add(btnCopyIP); btnPane.add(btnCopyUs); btnPane.add(btnCopyPw);
        btnPane.add(btnDelete);

        header.add(titlePane, BorderLayout.WEST);
        header.add(btnPane,   BorderLayout.EAST);

        //  STATS BAR
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        statsLabel = new JLabel("—");
        statsLabel.setFont(UIConstants.FONT_SMALL); statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        //  TABLA
        String[] cols = {"#", "Host", "IP", "Tipo", "Ambiente", "SO", "Usuario", "Contraseña", "Puerto", "Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.setRowHeight(34);

        int[] widths = {36, 150, 120, 100, 90, 80, 110, 100, 60, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(40);

        // Columna Contraseña — siempre muestra "••••••••"
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, "••••••••", sel, focus, row, col);
                setForeground(sel ? UIConstants.TEXT_BRIGHT : UIConstants.TEXT_MUTED);
                setBackground(sel ? UIConstants.ACCENT_BLUE
                        : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER));
                setFont(UIConstants.FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 8));
                return this;
            }
        });

        // Columna Estado — badge de color
        table.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String text = val != null ? val.toString() : "";
                Color accent = UIConstants.getEstadoColor(text);
                setBackground(sel ? UIConstants.ACCENT_BLUE
                        : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER));
                setForeground(accent);
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) editSelected(); }
        });

        //  CENTER
        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        //  FILTROS — parte inferior
        hsf = new HeaderSearchFilter(
                "Buscar servidor, IP, tipo...",
                new HeaderSearchFilter.ComboConfig("Tipo",    UIConstants.TIPOS_SERVIDOR,   "Todos"),
                new HeaderSearchFilter.ComboConfig("Estado",  UIConstants.ESTADOS_SERVIDOR, "Todos"),
                new HeaderSearchFilter.ComboConfig("Ambiente",UIConstants.AMBIENTES,        "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar",
                        new String[]{"IP", "Tipo", "Estado", "Ambiente"}, "Host A-Z")
        ).onChanged(this::applyFilters);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bottom.add(hsf, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        add(main, "main");
    }

    //  DATA

    private void loadData() {
        try { allData = dao.findAll(); applyFilters(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void applyFilters() {
        String q    = hsf.getQuery().toLowerCase();
        String tipo = hsf.getFilter(0);
        String est  = hsf.getFilter(1);
        String amb  = hsf.getFilter(2);
        String sort = hsf.getFilter(3);

        Stream<Servidor> s = allData.stream();
        if (!q.isEmpty())    s = s.filter(srv -> matches(srv, q));
        if (!tipo.isEmpty()) s = s.filter(srv -> tipo.equals(srv.getTipo()));
        if (!est.isEmpty())  s = s.filter(srv -> est.equals(srv.getEstado()));
        if (!amb.isEmpty())  s = s.filter(srv -> amb.equals(srv.getAmbiente()));

        Comparator<Servidor> cmp = switch (sort) {
            case "IP"       -> Comparator.comparing(srv -> nvl(srv.getIp()));
            case "Tipo"     -> Comparator.comparing(srv -> nvl(srv.getTipo()));
            case "Estado"   -> Comparator.comparing(srv -> nvl(srv.getEstado()));
            case "Ambiente" -> Comparator.comparing(srv -> nvl(srv.getAmbiente()));
            default         -> Comparator.comparing(srv -> nvl(srv.getHost()));
        };

        List<Servidor> res = s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i = 1;
        for (Servidor srv : res)
            tableModel.addRow(new Object[]{
                    i++,
                    srv.getHost(), srv.getIp(), srv.getTipo(), srv.getAmbiente(),
                    srv.getSistemaOperativo(), srv.getUsuarioAcceso(),
                    srv.getContrasenaEncriptada(),   // valor real (oculto por renderer)
                    srv.getPuerto(), srv.getEstado()
            });

        long act  = res.stream().filter(srv -> "Activo".equals(srv.getEstado())).count();
        long inac = res.stream().filter(srv -> "Inactivo".equals(srv.getEstado())).count();
        long mant = res.stream().filter(srv -> "Mantenimiento".equals(srv.getEstado())).count();
        statsLabel.setText(String.format(
                "  Total: %d  |  Activos: %d  |  Inactivos: %d  |  Mantenimiento: %d",
                res.size(), act, inac, mant));
    }

    private boolean matches(Servidor s, String q) {
        return nv(s.getHost(), q) || nv(s.getIp(), q) || nv(s.getTipo(), q)
                || nv(s.getAmbiente(), q) || nv(s.getDescripcion(), q)
                || nv(s.getUsuarioAcceso(), q);
    }

    //  ACCIONES

    private Servidor getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un servidor."); return null; }
        String nombre = (String) tableModel.getValueAt(row, 1);
        return allData.stream().filter(s -> nombre.equals(s.getHost())).findFirst().orElse(null);
    }

    private void editSelected() {
        Servidor s = getSelected(); if (s != null) openForm(s);
    }

    private void copyField(String field) {
        Servidor s = getSelected(); if (s == null) return;
        try {
            String val = switch (field) {
                case "ip"   -> nvl(s.getIp());
                case "user" -> nvl(s.getUsuarioAcceso());
                case "pass" -> {
                    if (!EncryptionUtil.hasMasterPassword())
                        throw new Exception("Clave maestra no disponible. Bloquea y vuelve a ingresar.");
                    String enc = s.getContrasenaEncriptada();
                    if (enc == null || enc.isBlank())
                        throw new Exception("Este servidor no tiene contraseña guardada.");
                    yield EncryptionUtil.decrypt(enc);
                }
                default -> "";
            };
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(val), null);
            String label = switch (field) { case "ip" -> "IP"; case "user" -> "Usuario"; default -> "Contraseña"; };
            JOptionPane.showMessageDialog(this,
                    field.equals("pass") ? "Contraseña copiada al portapapeles." : label + " copiado: " + val,
                    "Copiado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Servidor s = getSelected(); if (s == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar servidor \"" + s.getHost() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(s.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    //  FORMULARIO

    private void openForm(Servidor existing) {
        if (!EncryptionUtil.hasMasterPassword()) {
            JOptionPane.showMessageDialog(this,
                    "Clave maestra no disponible. Bloquea y vuelve a ingresar.",
                    "Error de seguridad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nuevo Servidor" : "Editar Servidor", true);
        d.setSize(620, 560); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField        fNom  = StyledComponents.styledTextField("Host descriptivo");
        JTextField        fIp   = StyledComponents.styledTextField("192.168.x.x");
        JComboBox<String> fTipo = StyledComponents.styledCombo(UIConstants.TIPOS_SERVIDOR);
        JComboBox<String> fAmb  = StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JComboBox<String> fSO   = StyledComponents.styledCombo(UIConstants.SISTEMAS_OPERATIVOS_CMD);
        JTextField        fUser = StyledComponents.styledTextField("usuario de acceso");
        JPasswordField    fPass = StyledComponents.styledPasswordField();
        JTextField        fPto  = StyledComponents.styledTextField("22 / 3389 / 443");
        JComboBox<String> fEst  = StyledComponents.styledCombo(UIConstants.ESTADOS_SERVIDOR);
        JTextField        fDesc = StyledComponents.styledTextField("Descripción breve");
        JTextArea         fNot  = StyledComponents.styledTextArea(3, 20);

        // Checkbox mostrar contraseña
        JCheckBox showPw = new JCheckBox("Mostrar contraseña");
        showPw.setForeground(UIConstants.TEXT_SECONDARY);
        showPw.setBackground(UIConstants.BG_CARD);
        showPw.setFont(UIConstants.FONT_SMALL);
        showPw.addItemListener(e -> fPass.setEchoChar(showPw.isSelected() ? (char) 0 : '•'));

        if (existing != null) {
            fNom.setText(existing.getHost()); fIp.setText(existing.getIp());
            setCombo(fTipo, existing.getTipo()); setCombo(fAmb, existing.getAmbiente());
            setCombo(fSO, existing.getSistemaOperativo()); fUser.setText(existing.getUsuarioAcceso());
            fPto.setText(existing.getPuerto()); setCombo(fEst, existing.getEstado());
            fDesc.setText(existing.getDescripcion()); fNot.setText(existing.getNotas());
            // Descifrar contraseña existente para edición
            try {
                String enc = existing.getContrasenaEncriptada();
                if (enc != null && !enc.isBlank())
                    fPass.setText(EncryptionUtil.decrypt(enc));
            } catch (Exception ignored) {}
        }

        int r = 0;
        addRow(form, gbc, r++, "Host *",fNom,  "IP *",fIp);
        addRow(form, gbc, r++, "Tipo",fTipo, "Ambiente",fAmb);
        addRow(form, gbc, r++, "Sistema Operativo", fSO, "Estado",fEst);
        addRow(form, gbc, r++, "Usuario Acceso",fUser, "Puerto",fPto);
        addRow(form, gbc, r++, "Contraseña",fPass, null,null);

        // Checkbox — fila completa debajo del campo contraseña
        gbc.gridy = r * 2; gbc.gridx = 0; gbc.gridwidth = 4; gbc.weightx = 1;
        form.add(showPw, gbc);
        r++;
        gbc.gridwidth = 1;

        addFull(form, gbc, r++, "Descripción", fDesc);
        addFull(form, gbc, r,   "Notas",       new JScrollPane(fNot));

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bp.setBackground(UIConstants.BG_BASE);
        JButton bS = StyledComponents.successButton("Guardar");
        JButton bC = StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e -> d.dispose());
        bS.addActionListener(e -> {
            if (fNom.getText().trim().isEmpty() || fIp.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "Host e IP son obligatorios."); return;
            }
            try {
                Servidor srv = existing != null ? existing : new Servidor();
                srv.setHost(fNom.getText().trim()); srv.setIp(fIp.getText().trim());
                srv.setTipo((String) fTipo.getSelectedItem());
                srv.setAmbiente((String) fAmb.getSelectedItem());
                srv.setSistemaOperativo((String) fSO.getSelectedItem());
                srv.setUsuarioAcceso(fUser.getText().trim());
                srv.setPuerto(fPto.getText().trim());
                srv.setEstado((String) fEst.getSelectedItem());
                srv.setDescripcion(fDesc.getText().trim());
                srv.setNotas(fNot.getText().trim());

                // Cifrar contraseña si se ingresó
                String rawPw = new String(fPass.getPassword());
                if (!rawPw.isBlank()) {
                    srv.setContrasenaEncriptada(EncryptionUtil.encrypt(rawPw));
                } else if (existing != null) {
                    srv.setContrasenaEncriptada(existing.getContrasenaEncriptada()); // mantener existente
                }

                if (existing == null) dao.insert(srv); else dao.update(srv);
                loadData(); d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bp.add(bS); bp.add(bC);

        JScrollPane sp = new JScrollPane(form); sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        d.add(sp, BorderLayout.CENTER); d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    //  FORM HELPERS

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        String l1, Component c1, String l2, Component c2) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 1; g.weightx = 0; p.add(lbl(l1), g);
        g.gridx = 1; g.weightx = .5; p.add(c1, g);
        if (l2 != null) {
            g.gridx = 2; g.weightx = 0; p.add(lbl(l2), g);
            g.gridx = 3; g.weightx = .5; p.add(c2, g);
        }
    }

    private void addFull(JPanel p, GridBagConstraints g, int row, String label, Component c) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 4; g.weightx = 1; p.add(lbl(label), g);
        g.gridy = row * 2 + 1; p.add(c, g); g.gridwidth = 1;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY); return l;
    }

    private void setCombo(JComboBox<String> cb, String v) {
        if (v == null) return;
        for (int i = 0; i < cb.getItemCount(); i++)
            if (v.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }

    private boolean nv(String f, String q) { return f != null && f.toLowerCase().contains(q); }
    private String  nvl(String s)           { return s == null ? "" : s; }
}