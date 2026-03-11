package baustro.fin.ec.ui.panels;

import baustro.fin.ec.model.Comando;
import baustro.fin.ec.repository.ComandoRepository;
import baustro.fin.ec.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ComandosPanel extends JPanel {

    private final ComandoRepository repo = ComandoRepository.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtBuscar;
    private JComboBox<String> cmbCategoria;
    private JTextArea taPreview;

    private static final String[] CATS = {
        "TODAS","LINUX","WINDOWS","SQL","ORACLE","GIT","DOCKER","NETWORKING","BASH","POWERSHELL","OTRO"
    };

    public ComandosPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);
        setBackground(UITheme.BG_PANEL);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Toolbar
        JPanel toolbar = UITheme.toolbarPanel();
        txtBuscar    = UITheme.textField("🔍  Buscar título, comando, tag...", 26);
        cmbCategoria = UITheme.comboBox(CATS);
        JButton btnBuscar = UITheme.secondaryButton("Buscar");
        JButton btnNuevo  = UITheme.primaryButton("➕  Nuevo Comando");
        btnBuscar.addActionListener(e -> loadData());
        btnNuevo.addActionListener(e -> abrirForm(null));
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) loadData(); }
        });
        toolbar.add(txtBuscar);
        toolbar.add(UITheme.sectionLabel("Categoría:")); toolbar.add(cmbCategoria);
        toolbar.add(btnBuscar);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(btnNuevo);

        // Table
        String[] cols = {"⭐","Título","Categoría","SO","Descripción","Comando"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        int[] w = {36,180,100,80,200,0};
        for (int i=0;i<w.length;i++) if(w[i]>0) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(36);

        // ⭐ col
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,focus,row,col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI Emoji",Font.PLAIN,13));
                lbl.setForeground("⭐".equals(v) ? UITheme.YELLOW : UITheme.BORDER);
                lbl.setBackground(sel?UITheme.BG_ROW_SEL:(row%2==0?UITheme.BG_PANEL:UITheme.BG_ROW_ALT));
                lbl.setOpaque(true);
                return lbl;
            }
        });
        // Comando — monospace verde
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,focus,row,col);
                lbl.setFont(UITheme.FONT_MONO);
                lbl.setForeground(sel ? UITheme.GREEN : new Color(0x4ADE80));
                lbl.setBackground(sel?UITheme.BG_ROW_SEL:(row%2==0?UITheme.BG_PANEL:UITheme.BG_ROW_ALT));
                lbl.setBorder(new EmptyBorder(0,12,0,8));
                lbl.setOpaque(true);
                return lbl;
            }
        });
        // Categoría badge
        table.getColumnModel().getColumn(2).setCellRenderer(UITheme.badgeRenderer(cat -> switch(cat){
            case "LINUX","BASH"   -> new Color[]{UITheme.GREEN_BG, UITheme.GREEN};
            case "WINDOWS","POWERSHELL" -> new Color[]{new Color(0x1E1B4B), new Color(0xA5B4FC)};
            case "SQL","ORACLE"   -> new Color[]{UITheme.ORANGE_BG, UITheme.ORANGE};
            case "GIT"            -> new Color[]{new Color(0x1C1917), new Color(0xFB923C)};
            case "DOCKER"         -> new Color[]{UITheme.ACCENT_DIM, UITheme.ACCENT_LIGHT};
            case "NETWORKING"     -> new Color[]{UITheme.YELLOW_BG, UITheme.YELLOW};
            default               -> new Color[]{UITheme.BG_CARD, UITheme.TEXT_DIM};
        }));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row>=0) {
                    Object cmd = tableModel.getValueAt(row,5);
                    taPreview.setText(cmd!=null?cmd.toString():"");
                    taPreview.setCaretPosition(0);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2) copiarComando();
            }
            @Override public void mousePressed(MouseEvent e)  { selectRowAt(e); }
            @Override public void mouseReleased(MouseEvent e) { selectRowAt(e); }
            private void selectRowAt(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                if (r>=0) table.setRowSelectionInterval(r,r);
            }
        });

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(UITheme.BG_CARD);
        JMenuItem miCopiar   = mi("📋  Copiar comando");
        JMenuItem miFav      = mi("⭐  Toggle Favorito");
        JMenuItem miEditar   = mi("✏   Editar");
        JMenuItem miEliminar = mi("🗑   Eliminar"); miEliminar.setForeground(UITheme.RED);
        miCopiar.addActionListener(e -> copiarComando());
        miFav.addActionListener(e -> toggleFavorito());
        miEditar.addActionListener(e -> editarSeleccionado());
        miEliminar.addActionListener(e -> eliminarSeleccionado());
        popup.add(miCopiar); popup.addSeparator();
        popup.add(miFav); popup.add(miEditar); popup.add(miEliminar);
        table.setComponentPopupMenu(popup);

        // Preview terminal panel
        taPreview = UITheme.terminalArea(4,50);
        taPreview.setEditable(false);
        taPreview.setText("# Selecciona un comando para ver el preview...\n# Doble clic para copiar al portapapeles");

        JPanel previewWrapper = new JPanel(new BorderLayout());
        previewWrapper.setBackground(UITheme.BG_DEEPEST);
        previewWrapper.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));

        JPanel previewHeader = new JPanel(new BorderLayout());
        previewHeader.setBackground(new Color(0x060A10));
        previewHeader.setBorder(new EmptyBorder(6,14,6,14));
        JLabel previewLabel = new JLabel("  ●  ●  ●     Terminal Preview");
        previewLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        previewLabel.setForeground(UITheme.TEXT_GHOST);
        JButton btnCopiarPreview = UITheme.secondaryButton("📋 Copiar");
        btnCopiarPreview.setFont(new Font("Segoe UI",Font.PLAIN,11));
        btnCopiarPreview.addActionListener(e -> copiarComando());
        previewHeader.add(previewLabel, BorderLayout.WEST);
        previewHeader.add(btnCopiarPreview, BorderLayout.EAST);

        previewWrapper.add(previewHeader, BorderLayout.NORTH);
        previewWrapper.add(UITheme.scrollPane(taPreview), BorderLayout.CENTER);
        previewWrapper.setPreferredSize(new Dimension(0, 120));

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT,16,4));
        statusBar.setBackground(UITheme.BG_DEEPEST);
        statusBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
        JLabel hint = new JLabel("Doble clic para copiar  •  Clic derecho para más opciones  •  ⭐ = Favoritos primero");
        hint.setFont(UITheme.FONT_SMALL); hint.setForeground(UITheme.TEXT_GHOST);
        statusBar.add(hint);

        add(toolbar, BorderLayout.NORTH);
        add(UITheme.scrollPane(table), BorderLayout.CENTER);
        add(previewWrapper, BorderLayout.SOUTH);
    }

    private JMenuItem mi(String text) {
        JMenuItem m = new JMenuItem(text);
        m.setBackground(UITheme.BG_CARD); m.setForeground(UITheme.TEXT_SECOND); m.setFont(UITheme.FONT_UI); return m;
    }

    private void loadData() {
        try {
            List<Comando> lista = repo.search(txtBuscar.getText(),(String)cmbCategoria.getSelectedItem());
            tableModel.setRowCount(0);
            for (Comando c : lista) {
                tableModel.addRow(new Object[]{c.isFavorito()?"⭐":"",c.getTitulo(),
                    c.getCategoria(),c.getSo(),c.getDescripcion(),c.getComando()});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private Comando getSelected() {
        int row = table.getSelectedRow();
        if (row<0) return null;
        String titulo = (String) tableModel.getValueAt(row,1);
        try { return repo.search(titulo,"TODAS").stream().filter(c->titulo.equals(c.getTitulo())).findFirst().orElse(null); }
        catch (Exception ex) { return null; }
    }

    private void copiarComando() {
        int row = table.getSelectedRow();
        if (row<0) return;
        String cmd = (String) tableModel.getValueAt(row,5);
        if (cmd!=null) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(cmd),null);
            JOptionPane.showMessageDialog(this,"Comando copiado al portapapeles.","✔ Copiado",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void toggleFavorito() {
        Comando c = getSelected();
        if (c==null) return;
        c.setFavorito(!c.isFavorito());
        try { repo.save(c); loadData(); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void editarSeleccionado() {
        ComandoFormDialog dlg = new ComandoFormDialog(SwingUtilities.getWindowAncestor(this), getSelected());
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void eliminarSeleccionado() {
        int row = table.getSelectedRow();
        if (row<0) return;
        String titulo = (String) tableModel.getValueAt(row,1);
        if (JOptionPane.showConfirmDialog(this,"¿Eliminar \""+titulo+"\"?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            Comando c = getSelected();
            if (c!=null) try{repo.delete(c.getId());loadData();}catch(Exception ex){ex.printStackTrace();}
        }
    }

    private void abrirForm(Comando c) {
        ComandoFormDialog dlg = new ComandoFormDialog(SwingUtilities.getWindowAncestor(this), c);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    // ── FORMULARIO ───────────────────────────────────────────────────
    static class ComandoFormDialog extends JDialog {
        private boolean saved = false;
        private final Comando cmd;

        ComandoFormDialog(Window owner, Comando c) {
            super(owner, c==null?"Nuevo Comando":"Editar Comando", ModalityType.APPLICATION_MODAL);
            this.cmd = c==null?new Comando():c;
            buildUI(); pack();
            setMinimumSize(new Dimension(600,480));
            setLocationRelativeTo(owner);
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(UITheme.BG_DARK);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(UITheme.BG_DEEPEST);
            header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UITheme.BORDER),
                new EmptyBorder(16,24,16,24)));
            JLabel lh = new JLabel(cmd.getId()==0?"💻  Nuevo Comando":"✏  Editar Comando");
            lh.setFont(new Font("Segoe UI",Font.BOLD,15)); lh.setForeground(UITheme.TEXT_PRIMARY);
            header.add(lh);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_DARK);
            form.setBorder(new EmptyBorder(20,24,20,24));
            GridBagConstraints g = new GridBagConstraints();
            g.insets=new Insets(6,6,6,6); g.anchor=GridBagConstraints.WEST;

            JTextField txtTitulo = UITheme.textField("Nombre descriptivo del comando",30);
            JTextField txtDesc   = UITheme.textField("Descripción corta de qué hace",30);
            JTextField txtTags   = UITheme.textField("Tags separados por coma",30);
            String[] cats = {"LINUX","WINDOWS","SQL","ORACLE","GIT","DOCKER","NETWORKING","BASH","POWERSHELL","OTRO"};
            JComboBox<String> cmbCat = UITheme.comboBox(cats);
            JComboBox<String> cmbSo  = UITheme.comboBox(new String[]{"Linux","Windows","Ambos"});
            JCheckBox chkFav = new JCheckBox("Marcar como favorito ⭐");
            chkFav.setBackground(UITheme.BG_DARK); chkFav.setForeground(UITheme.TEXT_SECOND);

            JTextArea taCmd = UITheme.terminalArea(7,50);

            if (cmd.getTitulo()    !=null) txtTitulo.setText(cmd.getTitulo());
            if (cmd.getDescripcion()!=null) txtDesc.setText(cmd.getDescripcion());
            if (cmd.getTags()      !=null) txtTags.setText(cmd.getTags());
            if (cmd.getCategoria() !=null) cmbCat.setSelectedItem(cmd.getCategoria());
            if (cmd.getSo()        !=null) cmbSo.setSelectedItem(cmd.getSo());
            chkFav.setSelected(cmd.isFavorito());
            if (cmd.getComando()   !=null) taCmd.setText(cmd.getComando());

            int y=0;
            addRow(form,g,y++,"Título:",txtTitulo);
            addRow(form,g,y++,"Descripción:",txtDesc);
            addRow(form,g,y++,"Categoría:",cmbCat);
            addRow(form,g,y++,"SO:",cmbSo);
            addRow(form,g,y++,"Tags:",txtTags);
            g.gridx=0;g.gridy=y++;g.gridwidth=2;form.add(chkFav,g);
            g.gridx=0;g.gridy=y++;g.gridwidth=2;g.fill=GridBagConstraints.HORIZONTAL;
            JLabel lblCmd=new JLabel("Comando:");lblCmd.setFont(UITheme.FONT_SMALL);lblCmd.setForeground(UITheme.TEXT_DIM);
            form.add(lblCmd,g);
            g.gridy=y;g.fill=GridBagConstraints.BOTH;g.weightx=1;g.weighty=1;
            JScrollPane sp=UITheme.scrollPane(taCmd);sp.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_DIM));
            sp.setPreferredSize(new Dimension(0,160));form.add(sp,g);

            JButton btnGuardar  = UITheme.primaryButton("💾  Guardar");
            JButton btnCancelar = UITheme.secondaryButton("Cancelar");
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,14));
            btnPanel.setBackground(UITheme.BG_DEEPEST);
            btnPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
            btnPanel.add(btnCancelar); btnPanel.add(btnGuardar);

            btnGuardar.addActionListener(e->{
                if(txtTitulo.getText().isBlank()||taCmd.getText().isBlank()){
                    JOptionPane.showMessageDialog(this,"Título y Comando son obligatorios.");return;}
                cmd.setTitulo(txtTitulo.getText().trim()); cmd.setDescripcion(txtDesc.getText().trim());
                cmd.setCategoria((String)cmbCat.getSelectedItem()); cmd.setSo((String)cmbSo.getSelectedItem());
                cmd.setTags(txtTags.getText().trim()); cmd.setFavorito(chkFav.isSelected());
                cmd.setComando(taCmd.getText().trim());
                try{
                    ComandoRepository.getInstance().save(cmd);saved=true;dispose();}
                catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
            });
            btnCancelar.addActionListener(e->dispose());

            root.add(header,BorderLayout.NORTH);
            root.add(UITheme.scrollPane(form),BorderLayout.CENTER);
            root.add(btnPanel,BorderLayout.SOUTH);
            setContentPane(root);
        }

        private void addRow(JPanel p,GridBagConstraints g,int y,String label,JComponent comp){
            g.gridwidth=1;g.fill=GridBagConstraints.NONE;g.weightx=0;
            g.gridx=0;g.gridy=y;
            JLabel l=new JLabel(label);l.setFont(UITheme.FONT_SMALL);l.setForeground(UITheme.TEXT_DIM);
            p.add(l,g);
            g.gridx=1;g.gridwidth=2;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(comp,g);
        }
        boolean isSaved(){return saved;}
    }
}
