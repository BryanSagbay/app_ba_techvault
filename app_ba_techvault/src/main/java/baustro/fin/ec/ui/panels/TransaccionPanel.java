package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.TransaccionDAO;
import baustro.fin.ec.model.Transaccion;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class TransaccionPanel extends JPanel {

    private static final String[] TIPOS = {"Mantenimiento", "Consulta", "Mensaje"};

    private final TransaccionDAO dao = new TransaccionDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private List<Transaccion> allData = new ArrayList<>();

    public TransaccionPanel() {
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
        JLabel title = new JLabel("Transacciones");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_TRX, 22);
        if (ico != null && ico.getIconWidth() > 1) { title.setIcon(ico); title.setIconTextGap(8); }
        titlePane.add(title);

        hsf = new HeaderSearchFilter(
                "Buscar TRX, subsistema, descripción...",
                new HeaderSearchFilter.ComboConfig("Tipo",     TIPOS,                                                              "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar",  new String[]{"Subsistema", "Tipo", "Sub-transacción"},    "TRX A-Z")
        ).onChanged(this::applyFilters);

        header.add(titlePane, BorderLayout.WEST);
        header.add(hsf, BorderLayout.EAST);

        // STATS BAR
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        statsLabel = new JLabel("Cargando...");
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        // TABLA
        String[] cols = {"#", "TRX", "Subsistema", "Sub-transacción", "Tipo", "Descripción"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(340);
        table.getColumnModel().getColumn(4).setCellRenderer(tipoRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) editSelected(); }
        });

        // CENTRO
        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        // BOTTOM
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_BASE);
        JButton btnNew    = StyledComponents.addButton("Nueva Transaccion");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        bottom.add(btnNew); bottom.add(btnEdit); bottom.add(btnDelete);

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
                    case "Mantenimiento" -> UIConstants.ACCENT_ORANGE;
                    case "Consulta"      -> UIConstants.ACCENT_BLUE;
                    case "Mensaje"       -> UIConstants.ACCENT_GREEN;
                    default              -> UIConstants.TEXT_SECONDARY;
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

        Stream<Transaccion> s = allData.stream();
        if (!q.isEmpty())    s = s.filter(t -> nv(t.getTrx(), q) || nv(t.getSubsistema(), q)
                                             || nv(t.getSubtransaccion(), q) || nv(t.getDescripcion(), q));
        if (!tipo.isEmpty()) s = s.filter(t -> tipo.equals(t.getTipo()));

        Comparator<Transaccion> cmp = switch (sort) {
            case "Subsistema"      -> Comparator.comparing(t -> nvl(t.getSubsistema()));
            case "Tipo"            -> Comparator.comparing(t -> nvl(t.getTipo()));
            case "Sub-transacción"  -> Comparator.comparing(t -> nvl(t.getSubtransaccion()));
            default                -> Comparator.comparing(t -> nvl(t.getTrx()));
        };

        List<Transaccion> res = s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i = 1;
        for (Transaccion t : res)
            tableModel.addRow(new Object[]{i++, t.getTrx(), t.getSubsistema(),
                    t.getSubtransaccion(), t.getTipo(), t.getDescripcion()});

        // Stats por tipo
        long mant  = res.stream().filter(t -> "Mantenimiento".equals(t.getTipo())).count();
        long cons  = res.stream().filter(t -> "Consulta".equals(t.getTipo())).count();
        long msg   = res.stream().filter(t -> "Mensaje".equals(t.getTipo())).count();
        statsLabel.setText(String.format(
                "  Total: %d   |   Mantenimiento: %d   |   Consulta: %d   |   Mensaje: %d",
                res.size(), mant, cons, msg));
    }

    private boolean nv(String f, String q) { return f != null && f.toLowerCase().contains(q); }
    private String nvl(String s) { return s == null ? "" : s; }

    private Transaccion getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una transacción."); return null; }
        String trx = (String) tableModel.getValueAt(row, 1);
        // Usar el índice # para encontrar la correcta
        tableModel.getValueAt(row, 0);
        // Filtrar igual que applyFilters y tomar el elemento en esa position
        return allData.stream().filter(t -> trx.equals(t.getTrx())).findFirst().orElse(null);
    }

    private void editSelected()   { Transaccion t = getSelected(); if (t != null) openForm(t); }

    private void deleteSelected() {
        Transaccion t = getSelected(); if (t == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Eliminar transaccion [" + t.getTrx() + "]?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(t.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void openForm(Transaccion existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nueva Transaccion" : "Editar Transaccion", true);
        d.setSize(560, 400); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(22, 28, 12, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 6, 6, 6);

        JTextField fTrx     = StyledComponents.styledTextField("Ej: 0200, CB01...");
        JTextField fSubsis  = StyledComponents.styledTextField("Ej: CORE, ATM, WEB...");
        JTextField fSubtrx  = StyledComponents.styledTextField("Codigo o nombre de sub-transacción");
        JComboBox<String> fTipo = StyledComponents.styledCombo(TIPOS);
        JTextArea  fDesc    = StyledComponents.styledTextArea(5, 40);

        if (existing != null) {
            fTrx.setText(existing.getTrx());
            fSubsis.setText(existing.getSubsistema());
            fSubtrx.setText(existing.getSubtransaccion());
            sc(fTipo, existing.getTipo());
            fDesc.setText(existing.getDescripcion());
        }

        int r = 0;
        ar(form, gbc, r++, "TRX *",          fTrx,    "Subsistema",    fSubsis);
        ar(form, gbc, r++, "Sub-transacción",  fSubtrx, "Tipo",          fTipo);
        af(form, gbc, r, new JScrollPane(fDesc));

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bp.setBackground(UIConstants.BG_BASE);
        JButton bS = StyledComponents.successButton("Guardar");
        JButton bC = StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e -> d.dispose());
        bS.addActionListener(e -> {
            if (fTrx.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "El campo TRX es obligatorio."); return;
            }
            Transaccion t = existing != null ? existing : new Transaccion();
            t.setTrx(fTrx.getText().trim());
            t.setSubsistema(fSubsis.getText().trim());
            t.setSubtransaccion(fSubtrx.getText().trim());
            t.setTipo((String) fTipo.getSelectedItem());
            t.setDescripcion(fDesc.getText().trim());
            try {
                if (existing == null) dao.insert(t); else dao.update(t);
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
    private void af(JPanel p, GridBagConstraints g, int row, Component c) {
        g.gridy=row*2; g.gridx=0; g.gridwidth=4; g.weightx=1; p.add(fl("Descripción"),g);
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
