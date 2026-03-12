package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.CorrectivoDAO;
import baustro.fin.ec.model.Correctivo;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

public class CorrectivoPanel extends JPanel {

    private final CorrectivoDAO dao = new CorrectivoDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Correctivo> currentData;
    private JTextField searchField;

    public CorrectivoPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BG_DARK);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Header bar ──────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Correctivos / Incidencias");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico0 = IconManager.getIcon(IconManager.ICON_CORRECTIVO, 24);
        if (_ico0 != null && _ico0.getIconWidth() > 1) title.setIcon(_ico0);
        title.setIconTextGap(10);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchField = StyledComponents.searchBar("Buscar por tarea, servicio, error...");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doSearch(); }
        });

        JButton btnNew = StyledComponents.addButton("Nuevo Correctivo");
        btnNew.addActionListener(e -> openForm(null));

        JButton btnRefresh = StyledComponents.primaryButton("Actualizar", UIConstants.BG_CARD);
        btnRefresh.addActionListener(e -> loadData());

        actions.add(searchField);
        actions.add(btnNew);
        actions.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // ── Table ───────────────────────────────────────────────
        String[] cols = {"#", "N° Tarea", "Título", "Servicio", "Ambiente", "Prioridad", "Estado", "Fecha Reporte"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);

        // Color en columna prioridad y estado
        table.getColumnModel().getColumn(5).setCellRenderer(colorRenderer("prioridad"));
        table.getColumnModel().getColumn(6).setCellRenderer(colorRenderer("estado"));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JScrollPane scroll = StyledComponents.darkScrollPane(table);

        // ── Bottom action bar ────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_DARK);

        JButton btnEdit   = StyledComponents.primaryButton("Editar",   UIConstants.ACCENT_BLUE);
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        JButton btnView   = StyledComponents.primaryButton("Ver Detalle", UIConstants.ACCENT_PURPLE);

        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnView.addActionListener(e -> viewSelected());

        bottom.add(btnView);
        bottom.add(btnEdit);
        bottom.add(btnDelete);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private DefaultTableCellRenderer colorRenderer(String type) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(sel ? UIConstants.ACCENT_BLUE : (row % 2 == 0 ? UIConstants.BG_PANEL : UIConstants.TABLE_ROW_ALT));
                String s = val != null ? val.toString() : "";
                Color c = type.equals("prioridad") ? UIConstants.getPrioridadColor(s) : UIConstants.getEstadoColor(s);
                setForeground(c);
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    private void loadData() {
        try {
            currentData = dao.findAll();
            refreshTable(currentData);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        try {
            List<Correctivo> result = q.isEmpty() ? dao.findAll() : dao.search(q);
            refreshTable(result);
        } catch (Exception ex) { /* ignore */ }
    }

    private void refreshTable(List<Correctivo> data) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Correctivo c : data) {
            tableModel.addRow(new Object[]{
                i++, c.getNumeroTarea(), c.getTitulo(),
                c.getServicio(), c.getAmbiente(),
                c.getPrioridad(), c.getEstado(), c.getFechaReporte()
            });
        }
    }

    private Correctivo getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un registro."); return null; }
        // Mapear visual index to data considering search
        String numTarea = (String) tableModel.getValueAt(row, 1);
        try {
            List<Correctivo> all = dao.findAll();
            return all.stream().filter(c -> numTarea.equals(c.getNumeroTarea())).findFirst().orElse(null);
        } catch (Exception ex) { return null; }
    }

    private void editSelected() {
        Correctivo c = getSelected();
        if (c != null) openForm(c);
    }

    private void deleteSelected() {
        Correctivo c = getSelected();
        if (c == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el correctivo [" + c.getNumeroTarea() + "] " + c.getTitulo() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void viewSelected() {
        Correctivo c = getSelected();
        if (c != null) showDetailDialog(c);
    }

    private void openForm(Correctivo existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nuevo Correctivo" : "Editar Correctivo", true);
        dialog.setSize(700, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel form = buildForm(existing);
        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_PANEL);
        sp.setBorder(null);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_DARK);
        JButton btnSave = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());

        // Extract fields from form
        JTextField fNumTarea   = (JTextField) form.getClientProperty("fNumTarea");
        JTextField fTitulo     = (JTextField) form.getClientProperty("fTitulo");
        JTextArea  fDesc       = (JTextArea)  form.getClientProperty("fDesc");
        JComboBox<String> fAmb = (JComboBox<String>) form.getClientProperty("fAmbiente");
        JTextField fServicio   = (JTextField) form.getClientProperty("fServicio");
        JTextArea  fError      = (JTextArea)  form.getClientProperty("fError");
        JTextArea  fSolucion   = (JTextArea)  form.getClientProperty("fSolucion");
        JComboBox<String> fEst = (JComboBox<String>) form.getClientProperty("fEstado");
        JComboBox<String> fPri = (JComboBox<String>) form.getClientProperty("fPrioridad");
        JTextField fFechaRep   = (JTextField) form.getClientProperty("fFechaRep");
        JTextField fFechaSol   = (JTextField) form.getClientProperty("fFechaSol");
        JTextField fRespons    = (JTextField) form.getClientProperty("fResponsable");
        JTextArea  fObs        = (JTextArea)  form.getClientProperty("fObservaciones");

        btnSave.addActionListener(e -> {
            String num = fNumTarea.getText().trim();
            String tit = fTitulo.getText().trim();
            if (num.isEmpty() || tit.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Número de tarea y título son obligatorios.");
                return;
            }
            Correctivo c = existing != null ? existing : new Correctivo();
            c.setNumeroTarea(num);
            c.setTitulo(tit);
            c.setDescripcion(fDesc.getText().trim());
            c.setAmbiente((String) fAmb.getSelectedItem());
            c.setServicio(fServicio.getText().trim());
            c.setErrorPresentado(fError.getText().trim());
            c.setSolucion(fSolucion.getText().trim());
            c.setEstado((String) fEst.getSelectedItem());
            c.setPrioridad((String) fPri.getSelectedItem());
            c.setFechaReporte(fFechaRep.getText().trim());
            c.setFechaSolucion(fFechaSol.getText().trim());
            c.setResponsable(fRespons.getText().trim());
            c.setObservaciones(fObs.getText().trim());
            try {
                if (existing == null) dao.insert(c); else dao.update(c);
                loadData();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error guardando: " + ex.getMessage());
            }
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel buildForm(Correctivo c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIConstants.BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField fNumTarea    = StyledComponents.styledTextField("Ej: INC-2024-001");
        JTextField fTitulo      = StyledComponents.styledTextField("Descripción breve del problema");
        JTextArea  fDesc        = StyledComponents.styledTextArea(3, 20);
        JComboBox<String> fAmb  = StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JTextField fServicio    = StyledComponents.styledTextField("Ej: Servicio de pagos");
        JTextArea  fError       = StyledComponents.styledTextArea(4, 20);
        JTextArea  fSolucion    = StyledComponents.styledTextArea(4, 20);
        JComboBox<String> fEst  = StyledComponents.styledCombo(UIConstants.ESTADOS_CORRECTIVO);
        JComboBox<String> fPri  = StyledComponents.styledCombo(UIConstants.PRIORIDADES);
        JTextField fFechaRep    = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField fFechaSol    = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField fRespons     = StyledComponents.styledTextField("Nombre del responsable");
        JTextArea  fObs         = StyledComponents.styledTextArea(3, 20);

        if (c != null) {
            fNumTarea.setText(c.getNumeroTarea());
            fTitulo.setText(c.getTitulo());
            fDesc.setText(c.getDescripcion());
            setCombo(fAmb, c.getAmbiente());
            fServicio.setText(c.getServicio());
            fError.setText(c.getErrorPresentado());
            fSolucion.setText(c.getSolucion());
            setCombo(fEst, c.getEstado());
            setCombo(fPri, c.getPrioridad());
            fFechaRep.setText(c.getFechaReporte());
            fFechaSol.setText(c.getFechaSolucion());
            fRespons.setText(c.getResponsable());
            fObs.setText(c.getObservaciones());
        } else {
            fFechaRep.setText(LocalDate.now().toString());
        }

        // Guardar references en clientProperty para recuperarlas luego
        p.putClientProperty("fNumTarea", fNumTarea);
        p.putClientProperty("fTitulo", fTitulo);
        p.putClientProperty("fDesc", fDesc);
        p.putClientProperty("fAmbiente", fAmb);
        p.putClientProperty("fServicio", fServicio);
        p.putClientProperty("fError", fError);
        p.putClientProperty("fSolucion", fSolucion);
        p.putClientProperty("fEstado", fEst);
        p.putClientProperty("fPrioridad", fPri);
        p.putClientProperty("fFechaRep", fFechaRep);
        p.putClientProperty("fFechaSol", fFechaSol);
        p.putClientProperty("fResponsable", fRespons);
        p.putClientProperty("fObservaciones", fObs);

        int row = 0;
        addFormRow(p, gbc, row++, "N° Tarea *", fNumTarea, "Título *", fTitulo);
        addFormRow(p, gbc, row++, "Ambiente", fAmb, "Servicio", fServicio);
        addFormRow(p, gbc, row++, "Estado", fEst, "Prioridad", fPri);
        addFormRow(p, gbc, row++, "Fecha Reporte", fFechaRep, "Fecha Solución", fFechaSol);
        addFormRow(p, gbc, row++, "Responsable", fRespons, null, null);

        addFormFull(p, gbc, row++, "Descripción del problema", new JScrollPane(fDesc));
        addFormFull(p, gbc, row++, "Error presentado", new JScrollPane(fError));
        addFormFull(p, gbc, row++, "Solución aplicada", new JScrollPane(fSolucion));
        addFormFull(p, gbc, row,   "Observaciones", new JScrollPane(fObs));

        return p;
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row,
                            String lbl1, Component c1, String lbl2, Component c2) {
        gbc.gridy = row * 2;
        gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        p.add(fieldLabel(lbl1), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        p.add(c1, gbc);
        if (lbl2 != null) {
            gbc.gridx = 2; gbc.weightx = 0;
            p.add(fieldLabel(lbl2), gbc);
            gbc.gridx = 3; gbc.weightx = 0.5;
            p.add(c2, gbc);
        }
    }

    private void addFormFull(JPanel p, GridBagConstraints gbc, int row, String label, Component c) {
        gbc.gridy = row * 2; gbc.gridx = 0; gbc.gridwidth = 4; gbc.weightx = 1;
        p.add(fieldLabel(label), gbc);
        gbc.gridy = row * 2 + 1;
        p.add(c, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_SMALL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private void setCombo(JComboBox<String> cb, String val) {
        if (val == null) return;
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (val.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
        }
    }

    private void showDetailDialog(Correctivo c) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalle: " + c.getNumeroTarea(), true);
        d.setSize(680, 580);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_PANEL);

        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(UIConstants.FONT_MONO_SM);
        ta.setBackground(UIConstants.BG_DARK);
        ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        ta.setText(buildDetailText(c));

        d.add(StyledComponents.darkScrollPane(ta));
        d.setVisible(true);
    }

    private String buildDetailText(Correctivo c) {
        String sep  = "======================================================\n";
        String sep2 = "------------------------------------------------------\n";
        return sep
            + "CORRECTIVO: " + c.getNumeroTarea() + "\n"
            + sep
            + "Titulo       : " + nvl(c.getTitulo()) + "\n"
            + "Servicio     : " + nvl(c.getServicio()) + "\n"
            + "Ambiente     : " + nvl(c.getAmbiente()) + "\n"
            + "Estado       : " + nvl(c.getEstado()) + "\n"
            + "Prioridad    : " + nvl(c.getPrioridad()) + "\n"
            + "Responsable  : " + nvl(c.getResponsable()) + "\n"
            + "Fecha Reporte: " + nvl(c.getFechaReporte()) + "\n"
            + "Fecha Solucion: " + nvl(c.getFechaSolucion()) + "\n\n"
            + "-- DESCRIPCION \n" + sep2
            + nvl(c.getDescripcion()) + "\n\n"
            + "-- ERROR PRESENTADO \n" + sep2
            + nvl(c.getErrorPresentado()) + "\n\n"
            + "-- SOLUCION APLICADA \n" + sep2
            + nvl(c.getSolucion()) + "\n\n"
            + "-- OBSERVACIONES \n" + sep2
            + nvl(c.getObservaciones()) + "\n"
            + sep;
    }

        private String nvl(String s) { return s == null ? "(sin información)" : s; }
}
