package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.EmergenteDAO;
import baustro.fin.ec.model.Emergente;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class EmergentePanel extends JPanel {

    private static final String[] TIPOS_EMERGENTE = {"Critico", "Urgente", "Informativo", "Preventivo"};

    private final EmergenteDAO dao = new EmergenteDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private List<Emergente> allData = new ArrayList<>();

    public EmergentePanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // HEADER con filtros
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Emergentes");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_AZURE, 22);
        if (ico != null && ico.getIconWidth() > 1) { title.setIcon(ico); title.setIconTextGap(8); }
        titlePane.add(title);

        hsf = new HeaderSearchFilter(
                "Buscar numero, subsistema, descripcion...",
                new HeaderSearchFilter.ComboConfig("Tipo",    TIPOS_EMERGENTE,                                         "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar", new String[]{ "Fecha antigua",
                                                               "N Emergente", "Tipo", "Subsistema"},                   "Fecha reciente")
        ).onChanged(this::applyFilters);

        // BOTONES — parte derecha del header
        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setOpaque(false);
        JButton btnNew    = StyledComponents.addButton("Nuevo Emergente");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        JButton btnView = StyledComponents.iconTextButton("Ver Detalle", IconManager.ICON_INFO, UIConstants.ACCENT_PURPLE);        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnView.addActionListener(e -> viewSelected());
        headerBtns.add(btnNew); headerBtns.add(btnEdit); headerBtns.add(btnView); headerBtns.add(btnDelete);

        header.add(titlePane,   BorderLayout.WEST);
        header.add(headerBtns,  BorderLayout.EAST);

        // STATS BAR
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        statsLabel = new JLabel("Cargando...");
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        // TABLA
        String[] cols = {"#", "N Emergente", "Fecha", "Tipo", "Subsistema", "Transacciones", "Descripcion"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(260);
        table.getColumnModel().getColumn(3).setCellRenderer(tipoRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) editSelected(); }
        });

        // CENTRO
        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        // BOTTOM — filtros
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bottom.add(hsf, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private DefaultTableCellRenderer tipoRenderer() {
        return new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val != null ? val.toString() : "";
                setBackground(sel ? UIConstants.ACCENT_BLUE : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER));
                setForeground(switch (s) {
                    case "Critico"     -> UIConstants.ACCENT_RED;
                    case "Urgente"     -> UIConstants.ACCENT_ORANGE;
                    case "Informativo" -> UIConstants.ACCENT_BLUE;
                    case "Preventivo"  -> UIConstants.ACCENT_GREEN;
                    default            -> UIConstants.TEXT_SECONDARY;
                });
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    private void loadData() {
        try { allData = dao.findAll(); applyFilters(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void applyFilters() {
        String q    = hsf.getQuery().toLowerCase();
        String tipo = hsf.getFilter(0);
        String sort = hsf.getFilter(1);

        Stream<Emergente> s = allData.stream();
        if (!q.isEmpty())    s = s.filter(e -> nv(e.getNumeroEmergente(), q) || nv(e.getSubsistema(), q)
                                             || nv(e.getDescripcion(), q) || nv(e.getTransacciones(), q));
        if (!tipo.isEmpty()) s = s.filter(e -> tipo.equals(e.getTipo()));

        Comparator<Emergente> cmp = switch (sort) {
            case "Fecha antigua"  -> Comparator.comparing(e -> nvl(e.getFecha()));
            case "N Emergente"    -> Comparator.comparing(e -> nvl(e.getNumeroEmergente()));
            case "Tipo"           -> Comparator.comparing(e -> nvl(e.getTipo()));
            case "Subsistema"     -> Comparator.comparing(e -> nvl(e.getSubsistema()));
            default               -> Comparator.comparing((Emergente e) -> nvl(e.getFecha())).reversed();
        };

        List<Emergente> res = s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i = 1;
        for (Emergente e : res)
            tableModel.addRow(new Object[]{i++, e.getNumeroEmergente(), e.getFecha(),
                    e.getTipo(), e.getSubsistema(), e.getTransacciones(), e.getDescripcion()});

        long crit = res.stream().filter(e -> "Critico".equals(e.getTipo())).count();
        long urg  = res.stream().filter(e -> "Urgente".equals(e.getTipo())).count();
        statsLabel.setText(String.format(
                "  Total: %d   |   Criticos: %d   |   Urgentes: %d   |   Otros: %d",
                res.size(), crit, urg, res.size() - crit - urg));
    }

    private boolean nv(String f, String q) { return f != null && f.toLowerCase().contains(q); }
    private String nvl(String s) { return s == null ? "" : s; }

    private Emergente getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un emergente."); return null; }
        String num = (String) tableModel.getValueAt(row, 1);
        return allData.stream().filter(e -> num.equals(e.getNumeroEmergente())).findFirst().orElse(null);
    }

    private void editSelected() { Emergente e = getSelected(); if (e != null) openForm(e); }

    private void deleteSelected() {
        Emergente e = getSelected(); if (e == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Eliminar emergente [" + e.getNumeroEmergente() + "]?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(e.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void viewSelected() {
        Emergente e = getSelected(); if (e == null) return;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Detalle: " + e.getNumeroEmergente(), true);
        d.setSize(620, 460); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        JTextArea ta = StyledComponents.monoTextArea(10, 60);
        ta.setEditable(false);
        ta.setBackground(UIConstants.BG_BASE);
        ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        String sep = "=".repeat(54) + "\n";
        ta.setText(sep
            + "EMERGENTE: " + nvl(e.getNumeroEmergente()) + "\n" + sep
            + "Fecha         : " + nvl(e.getFecha()) + "\n"
            + "Tipo          : " + nvl(e.getTipo()) + "\n"
            + "Subsistema    : " + nvl(e.getSubsistema()) + "\n"
            + "Transacciones : " + nvl(e.getTransacciones()) + "\n\n"
            + "-- DESCRIPCION\n" + sep
            + nvl(e.getDescripcion()) + "\n" + sep);
        d.add(StyledComponents.darkScrollPane(ta));
        d.setVisible(true);
    }

    private void openForm(Emergente existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nuevo Emergente" : "Editar Emergente", true);
        d.setSize(600, 460); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(22, 28, 12, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 6, 6, 6);

        JTextField fNum      = StyledComponents.styledTextField("Ej: EME-2024-001");
        JTextField fFecha    = StyledComponents.styledTextField("yyyy-mm-dd");
        JComboBox<String> fTipo = StyledComponents.styledCombo(TIPOS_EMERGENTE);
        JTextField fSubsis   = StyledComponents.styledTextField("Ej: CORE, ATM, WEB...");
        JTextField fTrxs     = StyledComponents.styledTextField("Ej: 0200, CB01, 0420...");
        JTextArea  fDesc     = StyledComponents.styledTextArea(6, 40);

        if (existing != null) {
            fNum.setText(existing.getNumeroEmergente());
            fFecha.setText(existing.getFecha());
            sc(fTipo, existing.getTipo());
            fSubsis.setText(existing.getSubsistema());
            fTrxs.setText(existing.getTransacciones());
            fDesc.setText(existing.getDescripcion());
        } else {
            fFecha.setText(LocalDate.now().toString());
        }

        int r = 0;
        ar(form, gbc, r++, "N Emergente *", fNum,    "Fecha",       fFecha);
        ar(form, gbc, r++, "Tipo",          fTipo,   "Subsistema",  fSubsis);
        af(form, gbc, r++, "Transacciones involucradas", fTrxs);
        af(form, gbc, r,   "Descripcion",   new JScrollPane(fDesc));

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bp.setBackground(UIConstants.BG_BASE);
        JButton bS = StyledComponents.successButton("Guardar");
        JButton bC = StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e -> d.dispose());
        bS.addActionListener(e -> {
            if (fNum.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "El numero de emergente es obligatorio."); return;
            }
            Emergente em = existing != null ? existing : new Emergente();
            em.setNumeroEmergente(fNum.getText().trim());
            em.setFecha(fFecha.getText().trim());
            em.setTipo((String) fTipo.getSelectedItem());
            em.setSubsistema(fSubsis.getText().trim());
            em.setTransacciones(fTrxs.getText().trim());
            em.setDescripcion(fDesc.getText().trim());
            try {
                if (existing == null) dao.insert(em); else dao.update(em);
                loadData(); d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage()); }
        });
        bp.add(bS); bp.add(bC);

        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_CARD); sp.setBorder(null);
        d.add(sp, BorderLayout.CENTER); d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void ar(JPanel p, GridBagConstraints g, int row, String l1, Component c1, String l2, Component c2) {
        g.gridy=row*2; g.gridx=0; g.gridwidth=1; g.weightx=0; p.add(fl(l1),g);
        g.gridx=1; g.weightx=.5; p.add(c1,g);
        g.gridx=2; g.weightx=0; p.add(fl(l2),g);
        g.gridx=3; g.weightx=.5; p.add(c2,g);
    }
    private void af(JPanel p, GridBagConstraints g, int row, String lbl, Component c) {
        g.gridy=row*2; g.gridx=0; g.gridwidth=4; g.weightx=1; p.add(fl(lbl),g);
        g.gridy=row*2+1; p.add(c,g); g.gridwidth=1;
    }
    private JLabel fl(String t) {
        JLabel l = new JLabel(t); l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY); return l;
    }
    private void sc(JComboBox<String> cb, String v) {
        if (v == null) return;
        for (int i = 0; i < cb.getItemCount(); i++) if (v.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }
}
