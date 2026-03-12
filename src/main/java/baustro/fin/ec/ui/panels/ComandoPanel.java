package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ComandoDAO;
import baustro.fin.ec.model.Comando;
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

public class ComandoPanel extends JPanel {

    private final ComandoDAO dao = new ComandoDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JTextArea previewArea;

    public ComandoPanel() {
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

        JLabel title = new JLabel("Biblioteca de Comandos");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico5 = IconManager.getIcon(IconManager.ICON_COMANDO, 24);
        if (_ico5 != null && _ico5.getIconWidth() > 1) title.setIcon(_ico5);
        title.setIconTextGap(10);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = StyledComponents.searchBar("Buscar comando, categoría...");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doSearch(); }
        });
        JButton btnNew = StyledComponents.addButton("Agregar Comando");
        btnNew.addActionListener(e -> openForm(null));
        actions.add(searchField); actions.add(btnNew);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Split
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(320);
        split.setDividerSize(4);
        split.setBackground(UIConstants.BG_DARK);

        // Table
        String[] cols = {"#", "Título", "Categoría", "SO", "Descripción"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(280);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showPreview();
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JScrollPane scrollTop = StyledComponents.darkScrollPane(table);

        // Preview / command area
        JPanel previewPanel = new JPanel(new BorderLayout(0, 0));
        previewPanel.setBackground(UIConstants.BG_DARK);

        JPanel previewHeader = new JPanel(new BorderLayout(8, 0));
        previewHeader.setBackground(UIConstants.BG_CARD);
        previewHeader.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel previewLbl = StyledComponents.muted("Comando seleccionado:");
        JButton btnCopy = StyledComponents.copyButton("Copiar Comando");
        btnCopy.addActionListener(e -> copyCommand());
        previewHeader.add(previewLbl, BorderLayout.WEST);
        previewHeader.add(btnCopy, BorderLayout.EAST);

        previewArea = StyledComponents.monoTextArea(4, 40);
        previewArea.setEditable(false);
        previewArea.setBackground(new Color(15, 20, 30));
        previewArea.setForeground(new Color(80, 220, 120)); // green terminal style
        previewArea.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        previewPanel.add(previewHeader, BorderLayout.NORTH);
        previewPanel.add(StyledComponents.darkScrollPane(previewArea), BorderLayout.CENTER);

        split.setTopComponent(scrollTop);
        split.setBottomComponent(previewPanel);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_DARK);
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        bottom.add(btnEdit); bottom.add(btnDelete);

        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        try { refreshTable(dao.findAll()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        try { refreshTable(q.isEmpty() ? dao.findAll() : dao.search(q)); }
        catch (Exception ex) { /* ignore */ }
    }

    private void refreshTable(List<Comando> data) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Comando c : data)
            tableModel.addRow(new Object[]{i++, c.getTitulo(), c.getCategoria(), c.getSistemaOperativo(), c.getDescripcion()});
    }

    private void showPreview() {
        Comando c = getSelected(false);
        if (c != null) previewArea.setText(c.getComando());
    }

    private void copyCommand() {
        Comando c = getSelected(true);
        if (c == null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(c.getComando()), null);
        JOptionPane.showMessageDialog(this, "Comando copiado", "Copiado", JOptionPane.INFORMATION_MESSAGE);
    }

    private Comando getSelected(boolean showMsg) {
        int row = table.getSelectedRow();
        if (row < 0) { if (showMsg) JOptionPane.showMessageDialog(this, "Seleccione un comando."); return null; }
        String titulo = (String) tableModel.getValueAt(row, 1);
        try {
            return dao.findAll().stream().filter(c -> titulo.equals(c.getTitulo())).findFirst().orElse(null);
        } catch (Exception ex) { return null; }
    }

    private void editSelected() {
        Comando c = getSelected(true);
        if (c != null) openForm(c);
    }

    private void deleteSelected() {
        Comando c = getSelected(true);
        if (c == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar comando \"" + c.getTitulo() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); previewArea.setText(""); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void openForm(Comando existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Agregar Comando" : "Editar Comando", true);
        dialog.setSize(560, 440);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6,6,6,6);

        JTextField fTitulo  = StyledComponents.styledTextField("Nombre descriptivo del comando");
        JTextField fCat     = StyledComponents.styledTextField("Ej: Red, Logs, Deploy, DB, Sistema");
        JComboBox<String> fSO = StyledComponents.styledCombo(UIConstants.SISTEMAS_OPERATIVOS_CMD);
        JTextArea  fCmd     = StyledComponents.monoTextArea(5, 40);
        JTextArea  fDesc    = StyledComponents.styledTextArea(3, 40);

        fCmd.setBackground(new Color(15, 20, 30));
        fCmd.setForeground(new Color(80, 220, 120));

        if (existing != null) {
            fTitulo.setText(existing.getTitulo()); fCat.setText(existing.getCategoria());
            setCombo(fSO, existing.getSistemaOperativo()); fCmd.setText(existing.getComando());
            fDesc.setText(existing.getDescripcion());
        }

        int r = 0;
        addRow(form, gbc, r++, "Título *", fTitulo, "Categoría", fCat);
        addRow(form, gbc, r++, "Sistema Operativo", fSO, null, null);
        addFull(form, gbc, r++, "Comando *", new JScrollPane(fCmd));
        addFull(form, gbc, r,   "Descripción / Uso", new JScrollPane(fDesc));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_DARK);
        JButton btnSave = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (fTitulo.getText().trim().isEmpty() || fCmd.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Título y comando son obligatorios.");
                return;
            }
            Comando c = existing != null ? existing : new Comando();
            c.setTitulo(fTitulo.getText().trim()); c.setCategoria(fCat.getText().trim());
            c.setSistemaOperativo((String) fSO.getSelectedItem());
            c.setComando(fCmd.getText().trim()); c.setDescripcion(fDesc.getText().trim());
            try {
                if (existing == null) dao.insert(c); else dao.update(c);
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
