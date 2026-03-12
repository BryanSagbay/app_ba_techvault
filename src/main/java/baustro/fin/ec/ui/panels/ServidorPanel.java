package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ServidorDAO;
import baustro.fin.ec.model.Servidor;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ServidorPanel extends JPanel {

    private final ServidorDAO dao = new ServidorDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public ServidorPanel() {
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

        JLabel title = new JLabel("Servidores & IPs");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico1 = IconManager.getIcon(IconManager.ICON_SERVIDOR, 24);
        if (_ico1 != null && _ico1.getIconWidth() > 1) title.setIcon(_ico1);
        title.setIconTextGap(10);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = StyledComponents.searchBar("Buscar nombre, IP, tipo, ambiente...");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doSearch(); }
        });

        JButton btnNew = StyledComponents.addButton("Nuevo Servidor");
        btnNew.addActionListener(e -> openForm(null));
        actions.add(searchField);
        actions.add(btnNew);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Table
        String[] cols = {"#", "Nombre", "IP / Host", "Tipo", "Ambiente", "SO", "Puerto", "Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        table.getColumnModel().getColumn(7).setCellRenderer(estadoRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JScrollPane scroll = StyledComponents.darkScrollPane(table);

        // Bottom bar
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(UIConstants.BG_DARK);
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        JButton btnCopyIP = StyledComponents.copyButton("Copiar IP");

        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnCopyIP.addActionListener(e -> copyIP());

        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnCopyIP);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private DefaultTableCellRenderer estadoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String s = val != null ? val.toString() : "";
                setBackground(sel ? UIConstants.ACCENT_BLUE : (row % 2 == 0 ? UIConstants.BG_PANEL : UIConstants.TABLE_ROW_ALT));
                setForeground(UIConstants.getEstadoColor(s));
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
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

    private void refreshTable(List<Servidor> data) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Servidor s : data) {
            tableModel.addRow(new Object[]{i++, s.getNombre(), s.getIp(), s.getTipo(),
                    s.getAmbiente(), s.getSistemaOperativo(), s.getPuerto(), s.getEstado()});
        }
    }

    private Servidor getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un servidor."); return null; }
        String nombre = (String) tableModel.getValueAt(row, 1);
        String ip     = (String) tableModel.getValueAt(row, 2);
        try {
            return dao.findAll().stream()
                    .filter(s -> nombre.equals(s.getNombre()) && ip.equals(s.getIp()))
                    .findFirst().orElse(null);
        } catch (Exception ex) { return null; }
    }

    private void editSelected() {
        Servidor s = getSelected();
        if (s != null) openForm(s);
    }

    private void deleteSelected() {
        Servidor s = getSelected();
        if (s == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar servidor " + s.getNombre() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(s.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void copyIP() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un servidor."); return; }
        String ip = (String) tableModel.getValueAt(row, 2);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ip), null);
        JOptionPane.showMessageDialog(this, "IP copiada: " + ip, "Copiado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openForm(Servidor existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nuevo Servidor" : "Editar Servidor", true);
        dialog.setSize(600, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField fNombre  = StyledComponents.styledTextField("Nombre del servidor");
        JTextField fIp      = StyledComponents.styledTextField("192.168.1.x o hostname");
        JComboBox<String> fTipo = StyledComponents.styledCombo(UIConstants.TIPOS_SERVIDOR);
        JComboBox<String> fAmb  = StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JComboBox<String> fSO   = StyledComponents.styledCombo(new String[]{"Linux", "Windows", "AIX", "Solaris", "Otro"});
        JTextField fPuerto  = StyledComponents.styledTextField("22 / 443 / 8080");
        JTextField fUsuario = StyledComponents.styledTextField("usuario de acceso");
        JComboBox<String> fEst  = StyledComponents.styledCombo(UIConstants.ESTADOS_SERVIDOR);
        JTextArea  fDesc    = StyledComponents.styledTextArea(3, 20);
        JTextArea  fNotas   = StyledComponents.styledTextArea(3, 20);

        if (existing != null) {
            fNombre.setText(existing.getNombre()); fIp.setText(existing.getIp());
            setCombo(fTipo, existing.getTipo()); setCombo(fAmb, existing.getAmbiente());
            setCombo(fSO, existing.getSistemaOperativo()); fPuerto.setText(existing.getPuerto());
            fUsuario.setText(existing.getUsuarioAcceso()); setCombo(fEst, existing.getEstado());
            fDesc.setText(existing.getDescripcion()); fNotas.setText(existing.getNotas());
        }

        int r = 0;
        addRow(form, gbc, r++, "Nombre *", fNombre, "IP / Host *", fIp);
        addRow(form, gbc, r++, "Tipo", fTipo, "Ambiente", fAmb);
        addRow(form, gbc, r++, "Sistema Operativo", fSO, "Puerto", fPuerto);
        addRow(form, gbc, r++, "Usuario Acceso", fUsuario, "Estado", fEst);
        addFull(form, gbc, r++, "Descripción", new JScrollPane(fDesc));
        addFull(form, gbc, r,   "Notas", new JScrollPane(fNotas));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_DARK);
        JButton btnSave = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (fNombre.getText().trim().isEmpty() || fIp.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nombre e IP son obligatorios.");
                return;
            }
            Servidor s = existing != null ? existing : new Servidor();
            s.setNombre(fNombre.getText().trim()); s.setIp(fIp.getText().trim());
            s.setTipo((String)fTipo.getSelectedItem()); s.setAmbiente((String)fAmb.getSelectedItem());
            s.setSistemaOperativo((String)fSO.getSelectedItem()); s.setPuerto(fPuerto.getText().trim());
            s.setUsuarioAcceso(fUsuario.getText().trim()); s.setEstado((String)fEst.getSelectedItem());
            s.setDescripcion(fDesc.getText().trim()); s.setNotas(fNotas.getText().trim());
            try {
                if (existing == null) dao.insert(s); else dao.update(s);
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
        gbc.gridx = 2; gbc.weightx = 0; p.add(lbl(l2), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; p.add(c2, gbc);
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
