package baustro.fin.ec.ui.panels;

import baustro.fin.ec.model.Nota;
import baustro.fin.ec.repository.NotaRepository;
import baustro.fin.ec.ui.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class NotasPanel extends JPanel {

    private final NotaRepository repo = NotaRepository.getInstance();
    private final DefaultListModel<Nota> listModel = new DefaultListModel<>();
    private JList<Nota> listNotas;
    private JTextField txtBuscar, txtTitulo, txtCategoria, txtTags;
    private JTextArea taContenido;
    private Nota notaActual = null;

    public NotasPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PANEL);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(270);
        split.setDividerSize(3);
        split.setBackground(UITheme.BORDER);
        split.setBorder(null);
        split.setOneTouchExpandable(false);

        // ── LEFT PANEL ───────────────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(UITheme.BG_DEEPEST);

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(0, 6));
        searchBar.setBackground(UITheme.BG_DEEPEST);
        searchBar.setBorder(new EmptyBorder(10,10,8,10));
        txtBuscar = UITheme.textField("🔍  Buscar notas...", 18);
        JButton btnNueva = UITheme.primaryButton("➕ Nueva");
        btnNueva.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadData(); }
        });
        searchBar.add(txtBuscar, BorderLayout.CENTER);
        searchBar.add(btnNueva, BorderLayout.SOUTH);
        btnNueva.addActionListener(e -> nuevaNota());

        // List
        listNotas = new JList<>(listModel);
        listNotas.setBackground(UITheme.BG_DEEPEST);
        listNotas.setForeground(UITheme.TEXT_PRIMARY);
        listNotas.setSelectionBackground(UITheme.BG_ROW_SEL);
        listNotas.setSelectionForeground(UITheme.TEXT_PRIMARY);
        listNotas.setFont(UITheme.FONT_UI);
        listNotas.setFixedCellHeight(56);
        listNotas.setCellRenderer((list, value, idx, sel, focus) -> {
            JPanel cell = new JPanel(new BorderLayout(0,2));
            cell.setBackground(sel ? UITheme.BG_ROW_SEL : (idx%2==0?UITheme.BG_DEEPEST:UITheme.BG_DARK));
            cell.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UITheme.BORDER),
                new EmptyBorder(8,14,8,10)));
            if (sel) {
                // Left accent
                cell.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0,3,1,0,UITheme.ACCENT_BLUE),
                    new EmptyBorder(8,11,8,10)));
            }
            JLabel lblTitulo = new JLabel(value.getTitulo());
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitulo.setForeground(sel ? UITheme.TEXT_PRIMARY : UITheme.TEXT_PRIMARY);
            String meta = "";
            if (value.getCategoria()!=null&&!value.getCategoria().isBlank()) meta += value.getCategoria();
            if (value.getTags()!=null&&!value.getTags().isBlank()) meta += (meta.isEmpty()?"":" · ") + value.getTags();
            JLabel lblMeta = new JLabel(meta.isEmpty() ? "Sin categoría" : meta);
            lblMeta.setFont(UITheme.FONT_SMALL);
            lblMeta.setForeground(UITheme.TEXT_GHOST);
            cell.add(lblTitulo, BorderLayout.NORTH);
            cell.add(lblMeta, BorderLayout.SOUTH);
            return cell;
        });
        listNotas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarNota(listNotas.getSelectedValue());
        });

        JScrollPane listScroll = UITheme.scrollPane(listNotas);
        leftPanel.add(searchBar, BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        // ── RIGHT PANEL ──────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(UITheme.BG_PANEL);

        // Meta fields bar
        JPanel metaBar = new JPanel(new GridBagLayout());
        metaBar.setBackground(UITheme.BG_DARK);
        metaBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,UITheme.BORDER),
            new EmptyBorder(10,16,10,16)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4,4,4,4); g.anchor=GridBagConstraints.WEST;

        txtTitulo    = UITheme.textField("Título de la nota...", 0);
        txtTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtCategoria = UITheme.textField("Categoría", 12);
        txtTags      = UITheme.textField("Tags (separados por coma)", 20);

        g.gridx=0;g.gridy=0;g.gridwidth=3;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;
        metaBar.add(txtTitulo,g);
        g.gridy=1;g.gridwidth=1;g.weightx=0;
        JLabel lcat = new JLabel("Categoría:");lcat.setFont(UITheme.FONT_SMALL);lcat.setForeground(UITheme.TEXT_DIM);
        metaBar.add(lcat,g);
        g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.4;metaBar.add(txtCategoria,g);
        g.gridx=2;g.weightx=1;metaBar.add(txtTags,g);

        // Editor
        taContenido = UITheme.textArea(0,0);
        taContenido.setBackground(UITheme.BG_PANEL);
        taContenido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane editorScroll = UITheme.scrollPane(taContenido);

        // Bottom buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.setBackground(UITheme.BG_DEEPEST);
        btnBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
        JButton btnGuardar  = UITheme.primaryButton("💾  Guardar");
        JButton btnEliminar = UITheme.dangerButton("🗑  Eliminar");
        JButton btnLimpiar  = UITheme.secondaryButton("✖  Nueva");
        btnGuardar.addActionListener(e -> guardarNota());
        btnEliminar.addActionListener(e -> eliminarNota());
        btnLimpiar.addActionListener(e -> nuevaNota());
        btnBar.add(btnLimpiar); btnBar.add(btnEliminar); btnBar.add(btnGuardar);

        rightPanel.add(metaBar,      BorderLayout.NORTH);
        rightPanel.add(editorScroll, BorderLayout.CENTER);
        rightPanel.add(btnBar,       BorderLayout.SOUTH);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        add(split, BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            List<Nota> lista = repo.search(txtBuscar.getText());
            listModel.clear();
            lista.forEach(listModel::addElement);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void nuevaNota() {
        notaActual = null;
        txtTitulo.setText(""); txtCategoria.setText(""); txtTags.setText(""); taContenido.setText("");
        listNotas.clearSelection();
        txtTitulo.requestFocus();
    }

    private void seleccionarNota(Nota n) {
        if (n==null) return;
        notaActual = n;
        txtTitulo.setText(n.getTitulo()!=null?n.getTitulo():"");
        txtCategoria.setText(n.getCategoria()!=null?n.getCategoria():"");
        txtTags.setText(n.getTags()!=null?n.getTags():"");
        taContenido.setText(n.getContenido()!=null?n.getContenido():"");
        taContenido.setCaretPosition(0);
    }

    private void guardarNota() {
        if (txtTitulo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,"El título es obligatorio."); return;
        }
        if (notaActual==null) notaActual = new Nota();
        notaActual.setTitulo(txtTitulo.getText().trim());
        notaActual.setCategoria(txtCategoria.getText().trim());
        notaActual.setTags(txtTags.getText().trim());
        notaActual.setContenido(taContenido.getText());
        try {
            repo.save(notaActual);
            loadData();
            for (int i=0;i<listModel.size();i++) {
                if (listModel.get(i).getId()==notaActual.getId()) { listNotas.setSelectedIndex(i); break; }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void eliminarNota() {
        if (notaActual==null||notaActual.getId()==0) return;
        if (JOptionPane.showConfirmDialog(this,"¿Eliminar \""+notaActual.getTitulo()+"\"?",
                "Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            try { repo.delete(notaActual.getId()); notaActual=null; nuevaNota(); loadData(); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
