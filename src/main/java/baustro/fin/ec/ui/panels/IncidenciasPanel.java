package baustro.fin.ec.ui.panels;

import baustro.fin.ec.model.Incidencia;
import baustro.fin.ec.repository.IncidenciaRepository;
import baustro.fin.ec.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

public class IncidenciasPanel extends JPanel {

    private final IncidenciaRepository repo = IncidenciaRepository.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtBuscar;
    private JComboBox<String> cmbEstado, cmbPrioridad;

    public IncidenciasPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);
        setBackground(UITheme.BG_PANEL);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Toolbar ──────────────────────────────────────────────────
        JPanel toolbar = UITheme.toolbarPanel();

        txtBuscar = UITheme.textField("🔍  Buscar número, título, servicio...", 26);
        cmbEstado    = UITheme.comboBox(new String[]{"TODOS", "ABIERTO", "EN_PROCESO", "CERRADO"});
        cmbPrioridad = UITheme.comboBox(new String[]{"TODAS", "CRITICA", "ALTA", "MEDIA", "BAJA"});
        JButton btnBuscar  = UITheme.secondaryButton("Buscar");
        JButton btnNueva   = UITheme.primaryButton("➕  Nueva Incidencia");
        JButton btnRefresh = UITheme.secondaryButton("↻");
        btnRefresh.setToolTipText("Limpiar filtros");

        btnBuscar.addActionListener(e -> loadData());
        btnNueva.addActionListener(e -> abrirForm(null));
        btnRefresh.addActionListener(e -> { txtBuscar.setText(""); loadData(); });
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) loadData(); }
        });

        toolbar.add(txtBuscar);
        toolbar.add(UITheme.sectionLabel("Estado:"));   toolbar.add(cmbEstado);
        toolbar.add(UITheme.sectionLabel("Prioridad:")); toolbar.add(cmbPrioridad);
        toolbar.add(btnBuscar);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(btnNueva);
        toolbar.add(btnRefresh);

        // ── Table ─────────────────────────────────────────────────────
        String[] cols = {"Número", "Título", "Servicio", "Prioridad", "Estado", "Fecha"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        // Column widths
        int[] w = {120, 0, 130, 100, 110, 110};
        for (int i = 0; i < w.length; i++) {
            if (w[i] > 0) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }

        // Custom renderers
        // Número — monospace azul
        table.getColumnModel().getColumn(0).setCellRenderer(
            UITheme.darkRenderer(SwingConstants.LEFT, UITheme.FONT_MONO_B, UITheme.ACCENT_LIGHT));
        // Prioridad — badge
        table.getColumnModel().getColumn(3).setCellRenderer(
            UITheme.badgeRenderer(UITheme::prioBadgeColors));
        // Estado — badge
        table.getColumnModel().getColumn(4).setCellRenderer(
            UITheme.badgeRenderer(UITheme::estadoBadgeColors));
        // Fecha — monospace dim
        table.getColumnModel().getColumn(5).setCellRenderer(
            UITheme.darkRenderer(SwingConstants.LEFT, UITheme.FONT_MONO, UITheme.TEXT_DIM));

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editarSeleccionada();
            }
        });

        // Right-click menu
        JPopupMenu popup = buildPopup();
        table.setComponentPopupMenu(popup);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { selectRowAt(e); }
            @Override public void mouseReleased(MouseEvent e) { selectRowAt(e); }
            private void selectRowAt(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0) table.setRowSelectionInterval(r, r);
            }
        });

        JScrollPane scroll = UITheme.scrollPane(table);

        // ── Status bar ────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        statusBar.setBackground(UITheme.BG_DEEPEST);
        statusBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0, UITheme.BORDER));
        JLabel hint = new JLabel("Doble clic para editar  •  Clic derecho para opciones");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_GHOST);
        statusBar.add(hint);

        add(toolbar,   BorderLayout.NORTH);
        add(scroll,    BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPopupMenu buildPopup() {
        JPopupMenu pm = new JPopupMenu();
        pm.setBackground(UITheme.BG_CARD);
        pm.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_INPUT));
        JMenuItem miEditar   = menuItem("✏   Editar");
        JMenuItem miCerrar   = menuItem("✔   Marcar cerrada");
        JMenuItem miEliminar = menuItem("🗑   Eliminar");
        miEliminar.setForeground(UITheme.RED);
        miEditar.addActionListener(e -> editarSeleccionada());
        miCerrar.addActionListener(e -> cerrarSeleccionada());
        miEliminar.addActionListener(e -> eliminarSeleccionada());
        pm.add(miEditar); pm.add(miCerrar); pm.addSeparator(); pm.add(miEliminar);
        return pm;
    }

    private JMenuItem menuItem(String text) {
        JMenuItem mi = new JMenuItem(text);
        mi.setBackground(UITheme.BG_CARD);
        mi.setForeground(UITheme.TEXT_SECOND);
        mi.setFont(UITheme.FONT_UI);
        return mi;
    }

    private void loadData() {
        try {
            List<Incidencia> lista = repo.search(
                txtBuscar.getText(),
                (String) cmbEstado.getSelectedItem(),
                (String) cmbPrioridad.getSelectedItem());
            tableModel.setRowCount(0);
            for (Incidencia i : lista) {
                tableModel.addRow(new Object[]{
                    i.getNumero(), i.getTitulo(), i.getServicio(),
                    i.getPrioridad(), i.getEstado(), i.getFechaInicio()
                });
            }
        } catch (Exception ex) { showError(ex); }
    }

    private void abrirForm(Incidencia inc) {
        IncidenciaFormDialog dlg = new IncidenciaFormDialog(
            SwingUtilities.getWindowAncestor(this), inc);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void editarSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String numero = (String) tableModel.getValueAt(row, 0);
        try {
            repo.search(numero, "TODOS", "TODAS").stream().findFirst()
                .ifPresent(this::abrirForm);
        } catch (Exception ex) { showError(ex); }
    }

    private void cerrarSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String numero = (String) tableModel.getValueAt(row, 0);
        try {
            repo.search(numero, "TODOS", "TODAS").stream().findFirst().ifPresent(inc -> {
                inc.setEstado("CERRADO");
                inc.setFechaCierre(LocalDate.now().toString());
                try { repo.save(inc); loadData(); } catch (Exception ex) { showError(ex); }
            });
        } catch (Exception ex) { showError(ex); }
    }

    private void eliminarSeleccionada() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String titulo = (String) tableModel.getValueAt(row, 1);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + titulo + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                String numero = (String) tableModel.getValueAt(row, 0);
                repo.search(numero, "TODOS", "TODAS").stream().findFirst()
                    .ifPresent(inc -> { try { repo.delete(inc.getId()); loadData(); } catch (Exception ex) { showError(ex); } });
            } catch (Exception ex) { showError(ex); }
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── FORMULARIO ───────────────────────────────────────────────────
    static class IncidenciaFormDialog extends JDialog {
        private boolean saved = false;
        private final Incidencia inc;

        IncidenciaFormDialog(Window owner, Incidencia i) {
            super(owner, i == null ? "Nueva Incidencia" : "Editar Incidencia",
                  ModalityType.APPLICATION_MODAL);
            this.inc = i == null ? new Incidencia() : i;
            buildUI(); pack();
            setMinimumSize(new Dimension(720, 640));
            setLocationRelativeTo(owner);
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout(0, 0));
            root.setBackground(UITheme.BG_DARK);

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(UITheme.BG_DEEPEST);
            header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0, UITheme.BORDER),
                new EmptyBorder(16, 24, 16, 24)));
            JLabel lblTitle = new JLabel(inc.getId() == 0 ? "🐛  Nueva Incidencia" : "✏  Editar Incidencia");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTitle.setForeground(UITheme.TEXT_PRIMARY);
            header.add(lblTitle);

            // Form
            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_DARK);
            form.setBorder(new EmptyBorder(20, 24, 20, 24));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;

            JTextField txtNumero   = UITheme.textField("Ej: INC-0042 / TKT-0123", 18);
            JTextField txtServicio = UITheme.textField("Servicio o sistema afectado", 18);
            JTextField txtTitulo   = UITheme.textField("Descripción corta del problema", 40);
            JTextField txtFechaIni = UITheme.textField("YYYY-MM-DD", 12);
            JTextField txtFechaFin = UITheme.textField("YYYY-MM-DD", 12);
            JTextField txtTags     = UITheme.textField("oracle, timeout, produccion...", 30);

            if (inc.getNumero()     != null) txtNumero.setText(inc.getNumero());
            if (inc.getServicio()   != null) txtServicio.setText(inc.getServicio());
            if (inc.getTitulo()     != null) txtTitulo.setText(inc.getTitulo());
            if (inc.getFechaInicio()!= null) txtFechaIni.setText(inc.getFechaInicio());
            else txtFechaIni.setText(LocalDate.now().toString());
            if (inc.getFechaCierre()!= null) txtFechaFin.setText(inc.getFechaCierre());
            if (inc.getTags()       != null) txtTags.setText(inc.getTags());

            JComboBox<String> cmbEstado    = UITheme.comboBox(new String[]{"ABIERTO","EN_PROCESO","CERRADO"});
            JComboBox<String> cmbPrioridad = UITheme.comboBox(new String[]{"BAJA","MEDIA","ALTA","CRITICA"});
            if (inc.getEstado()    != null) cmbEstado.setSelectedItem(inc.getEstado());
            if (inc.getPrioridad() != null) cmbPrioridad.setSelectedItem(inc.getPrioridad());

            JTextArea taDesc  = UITheme.textArea(5, 50);
            JTextArea taSoluc = UITheme.textArea(5, 50);
            if (inc.getDescripcion() != null) taDesc.setText(inc.getDescripcion());
            if (inc.getSolucion()    != null) taSoluc.setText(inc.getSolucion());

            // Row layout helper
            int y = 0;
            addRow(form, g, y++, "Número / Ticket:", txtNumero, "Servicio/Sistema:", txtServicio);
            g.gridx=0; g.gridy=y; g.gridwidth=1; form.add(lbl("Título:"), g);
            g.gridx=1; g.gridy=y++; g.gridwidth=3; g.fill=GridBagConstraints.HORIZONTAL; form.add(txtTitulo, g); g.fill=GridBagConstraints.NONE;
            addRow(form, g, y++, "Estado:", cmbEstado, "Prioridad:", cmbPrioridad);
            addRow(form, g, y++, "Fecha inicio:", txtFechaIni, "Fecha cierre:", txtFechaFin);
            g.gridx=0; g.gridy=y; g.gridwidth=1; form.add(lbl("Tags:"), g);
            g.gridx=1; g.gridy=y++; g.gridwidth=3; g.fill=GridBagConstraints.HORIZONTAL; form.add(txtTags, g); g.fill=GridBagConstraints.NONE;

            // Texto areas
            addTextAreaRow(form, g, y++, "📋  Descripción del problema:", taDesc);
            addTextAreaRow(form, g, y,   "✅  Solución aplicada:", taSoluc);

            // Buttons
            JButton btnGuardar  = UITheme.primaryButton("💾  Guardar");
            JButton btnCancelar = UITheme.secondaryButton("Cancelar");
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
            btnPanel.setBackground(UITheme.BG_DEEPEST);
            btnPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0, UITheme.BORDER));
            btnPanel.add(btnCancelar); btnPanel.add(btnGuardar);

            btnGuardar.addActionListener(e -> {
                if (txtNumero.getText().isBlank() || txtTitulo.getText().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Número y Título son obligatorios.");
                    return;
                }
                inc.setNumero(txtNumero.getText().trim());
                inc.setTitulo(txtTitulo.getText().trim());
                inc.setServicio(txtServicio.getText().trim());
                inc.setEstado((String) cmbEstado.getSelectedItem());
                inc.setPrioridad((String) cmbPrioridad.getSelectedItem());
                inc.setFechaInicio(txtFechaIni.getText().trim());
                inc.setFechaCierre(txtFechaFin.getText().trim());
                inc.setTags(txtTags.getText().trim());
                inc.setDescripcion(taDesc.getText().trim());
                inc.setSolucion(taSoluc.getText().trim());
                try { IncidenciaRepository.getInstance().save(inc); saved = true; dispose(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            });
            btnCancelar.addActionListener(e -> dispose());

            root.add(header,                 BorderLayout.NORTH);
            root.add(UITheme.scrollPane(form), BorderLayout.CENTER);
            root.add(btnPanel,               BorderLayout.SOUTH);
            setContentPane(root);
        }

        private JLabel lbl(String t) {
            JLabel l = new JLabel(t);
            l.setFont(UITheme.FONT_SMALL);
            l.setForeground(UITheme.TEXT_DIM);
            return l;
        }

        private void addRow(JPanel p, GridBagConstraints g, int y, String l1, JComponent c1, String l2, JComponent c2) {
            g.gridwidth=1; g.fill=GridBagConstraints.NONE;
            g.gridx=0; g.gridy=y; p.add(lbl(l1), g);
            g.gridx=1; g.fill=GridBagConstraints.HORIZONTAL; p.add(c1, g);
            g.gridx=2; g.fill=GridBagConstraints.NONE; p.add(lbl(l2), g);
            g.gridx=3; g.fill=GridBagConstraints.HORIZONTAL; p.add(c2, g);
        }

        private void addTextAreaRow(JPanel p, GridBagConstraints g, int y, String label, JTextArea ta) {
            g.gridx=0; g.gridy=y; g.gridwidth=4; g.fill=GridBagConstraints.HORIZONTAL;
            p.add(lbl(label), g);
            g.gridy=y+1; g.fill=GridBagConstraints.BOTH; g.weightx=1; g.weighty=0.5;
            JScrollPane sp = UITheme.scrollPane(ta);
            sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_INPUT));
            sp.setPreferredSize(new Dimension(0, 110));
            p.add(sp, g);
            g.weighty=0; g.weightx=0;
        }

        boolean isSaved() { return saved; }
    }
}
