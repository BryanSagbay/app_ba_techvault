package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.TareaDAO;
import baustro.fin.ec.model.Tarea;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TareaPanel extends JPanel {

    private final TareaDAO dao = new TareaDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> filterEstado;

    public TareaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Tareas & To-Do");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico3 = IconManager.getIcon(IconManager.ICON_TAREA, 24);
        if (_ico3 != null && _ico3.getIconWidth() > 1) title.setIcon(_ico3);
        title.setIconTextGap(10);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        String[] filterItems = {"Todas", "Pendiente", "En Progreso", "Completada", "Cancelada"};
        filterEstado = StyledComponents.styledCombo(filterItems);
        filterEstado.setPreferredSize(new Dimension(140, 34));
        filterEstado.addActionListener(e -> loadData());

        JButton btnNew = StyledComponents.addButton("Nueva Tarea");
        btnNew.addActionListener(e -> openForm(null));

        actions.add(new JLabel("Filtrar: ") {{ setForeground(UIConstants.TEXT_SECONDARY); setFont(UIConstants.FONT_BODY); }});
        actions.add(filterEstado);
        actions.add(btnNew);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Table
        String[] cols = {"#", "Título", "Categoría", "Prioridad", "Estado", "Fecha Límite", "Descripción"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(240);

        // Color renderers
        table.getColumnModel().getColumn(3).setCellRenderer(colorCellRenderer("prioridad"));
        table.getColumnModel().getColumn(4).setCellRenderer(colorCellRenderer("estado"));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JScrollPane scroll = StyledComponents.darkScrollPane(table);

        // Bottom action bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_DARK);
        JButton btnEdit     = StyledComponents.editButton("Editar");
        JButton btnDelete   = StyledComponents.dangerButton("Eliminar");
        JButton btnComplete = StyledComponents.successButton("Completada");
        JButton btnProgress = StyledComponents.primaryButton("En Progreso", UIConstants.ACCENT_ORANGE);

        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnComplete.addActionListener(e -> changeStatus("Completada"));
        btnProgress.addActionListener(e -> changeStatus("En Progreso"));

        bottom.add(btnEdit); bottom.add(btnDelete);
        bottom.add(btnProgress); bottom.add(btnComplete);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private DefaultTableCellRenderer colorCellRenderer(String type) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val != null ? val.toString() : "";
                setBackground(sel ? UIConstants.ACCENT_BLUE : (row % 2 == 0 ? UIConstants.BG_PANEL : UIConstants.TABLE_ROW_ALT));
                setForeground(type.equals("prioridad") ? UIConstants.getPrioridadColor(s) : UIConstants.getEstadoColor(s));
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    private void loadData() {
        try {
            String filter = (String) filterEstado.getSelectedItem();
            List<Tarea> data = (filter == null || filter.equals("Todas"))
                    ? dao.findAll() : dao.findByEstado(filter);
            refreshTable(data);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void refreshTable(List<Tarea> data) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Tarea t : data) {
            tableModel.addRow(new Object[]{i++, t.getTitulo(), t.getCategoria(),
                    t.getPrioridad(), t.getEstado(), t.getFechaLimite(), t.getDescripcion()});
        }
    }

    private Tarea getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una tarea."); return null; }
        String titulo = (String) tableModel.getValueAt(row, 1);
        try {
            return dao.findAll().stream().filter(t -> titulo.equals(t.getTitulo())).findFirst().orElse(null);
        } catch (Exception ex) { return null; }
    }

    private void editSelected() {
        Tarea t = getSelected();
        if (t != null) openForm(t);
    }

    private void deleteSelected() {
        Tarea t = getSelected();
        if (t == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar tarea \"" + t.getTitulo() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(t.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void changeStatus(String newStatus) {
        Tarea t = getSelected();
        if (t == null) return;
        t.setEstado(newStatus);
        try { dao.update(t); loadData(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void openForm(Tarea existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nueva Tarea" : "Editar Tarea", true);
        dialog.setSize(520, 430);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField fTitulo    = StyledComponents.styledTextField("Descripción corta de la tarea");
        JComboBox<String> fPri = StyledComponents.styledCombo(UIConstants.PRIORIDADES);
        JComboBox<String> fEst = StyledComponents.styledCombo(UIConstants.ESTADOS_TAREA);
        JTextField fFecha     = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField fCat       = StyledComponents.styledTextField("Ej: Correctivo, Deploy, Reunión");
        JTextArea  fDesc      = StyledComponents.styledTextArea(5, 20);

        if (existing != null) {
            fTitulo.setText(existing.getTitulo());
            setCombo(fPri, existing.getPrioridad());
            setCombo(fEst, existing.getEstado());
            fFecha.setText(existing.getFechaLimite());
            fCat.setText(existing.getCategoria());
            fDesc.setText(existing.getDescripcion());
        }

        int r = 0;
        addRow(form, gbc, r++, "Título *", fTitulo, "Categoría", fCat);
        addRow(form, gbc, r++, "Prioridad", fPri, "Estado", fEst);
        addRow(form, gbc, r++, "Fecha Límite", fFecha, null, null);
        addFull(form, gbc, r,  "Descripción / Detalle", new JScrollPane(fDesc));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_DARK);
        JButton btnSave = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (fTitulo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El título es obligatorio.");
                return;
            }
            Tarea t = existing != null ? existing : new Tarea();
            t.setTitulo(fTitulo.getText().trim());
            t.setPrioridad((String) fPri.getSelectedItem());
            t.setEstado((String) fEst.getSelectedItem());
            t.setFechaLimite(fFecha.getText().trim());
            t.setCategoria(fCat.getText().trim());
            t.setDescripcion(fDesc.getText().trim());
            try {
                if (existing == null) dao.insert(t); else dao.update(t);
                loadData(); dialog.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage()); }
        });
        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_PANEL); sp.setBorder(null);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String l1, Component c1, String l2, Component c2) {
        gbc.gridy = row * 2; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        p.add(lbl(l1), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; p.add(c1, gbc);
        if (l2 != null) {
            gbc.gridx = 2; gbc.weightx = 0; p.add(lbl(l2), gbc);
            gbc.gridx = 3; gbc.weightx = 0.5; p.add(c2, gbc);
        }
    }

    private void addFull(JPanel p, GridBagConstraints gbc, int row, String label, Component c) {
        gbc.gridy = row * 2; gbc.gridx = 0; gbc.gridwidth = 4; gbc.weightx = 1;
        p.add(lbl(label), gbc); gbc.gridy = row * 2 + 1; p.add(c, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY); return l;
    }

    private void setCombo(JComboBox<String> cb, String val) {
        if (val == null) return;
        for (int i = 0; i < cb.getItemCount(); i++)
            if (val.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }
}
