package baustro.fin.ec.ui.panels;

import baustro.fin.ec.model.Servidor;
import baustro.fin.ec.repository.ServidorRepository;
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

public class ServidoresPanel extends JPanel {

    private final ServidorRepository repo = ServidorRepository.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtBuscar;
    private JComboBox<String> cmbAmbiente;

    public ServidoresPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);
        setBackground(UITheme.BG_PANEL);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel toolbar = UITheme.toolbarPanel();
        txtBuscar   = UITheme.textField("🔍  IP, nombre, hostname, rol...", 26);
        cmbAmbiente = UITheme.comboBox(new String[]{"TODOS","PROD","HOM","QA","DEV"});
        JButton btnBuscar  = UITheme.secondaryButton("Buscar");
        JButton btnNuevo   = UITheme.primaryButton("➕  Nuevo Servidor");
        JButton btnRefresh = UITheme.secondaryButton("↻");

        btnBuscar.addActionListener(e -> loadData());
        btnNuevo.addActionListener(e -> abrirForm(null));
        btnRefresh.addActionListener(e -> { txtBuscar.setText(""); loadData(); });
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) loadData(); }
        });

        toolbar.add(txtBuscar);
        toolbar.add(UITheme.sectionLabel("Ambiente:")); toolbar.add(cmbAmbiente);
        toolbar.add(btnBuscar);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(btnNuevo); toolbar.add(btnRefresh);

        String[] cols = {"Nombre","IP","Hostname","SO","Rol","Ambiente","Puerto","Estado"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        int[] w = {150,130,150,110,90,90,70,80};
        for (int i=0; i<w.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // IP — monospace con click para copiar
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,focus,row,col);
                lbl.setFont(UITheme.FONT_MONO_B);
                lbl.setForeground(sel ? UITheme.TEXT_PRIMARY : UITheme.ACCENT_LIGHT);
                lbl.setBackground(sel ? UITheme.BG_ROW_SEL : (row%2==0?UITheme.BG_PANEL:UITheme.BG_ROW_ALT));
                lbl.setBorder(new EmptyBorder(0,12,0,8));
                lbl.setOpaque(true);
                lbl.setToolTipText("Clic derecho → Copiar IP");
                return lbl;
            }
        });
        table.getColumnModel().getColumn(5).setCellRenderer(UITheme.badgeRenderer(UITheme::ambienteBadgeColors));
        // Estado activo
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                boolean activo = Boolean.TRUE.equals(v);
                JLabel lbl = new JLabel(activo ? "● ACTIVO" : "○ INACTIVO", SwingConstants.LEFT);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(activo ? UITheme.GREEN : UITheme.TEXT_DIM);
                lbl.setBackground(sel ? UITheme.BG_ROW_SEL : (row%2==0?UITheme.BG_PANEL:UITheme.BG_ROW_ALT));
                lbl.setBorder(new EmptyBorder(0,12,0,8)); lbl.setOpaque(true);
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editarSeleccionado();
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
        JMenuItem miCopiarIp = mi("📋  Copiar IP");
        JMenuItem miEditar   = mi("✏   Editar");
        JMenuItem miEliminar = mi("🗑   Eliminar");
        miEliminar.setForeground(UITheme.RED);
        miCopiarIp.addActionListener(e -> copiarIp());
        miEditar.addActionListener(e -> editarSeleccionado());
        miEliminar.addActionListener(e -> eliminarSeleccionado());
        popup.add(miCopiarIp); popup.addSeparator(); popup.add(miEditar); popup.add(miEliminar);
        table.setComponentPopupMenu(popup);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        statusBar.setBackground(UITheme.BG_DEEPEST);
        statusBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
        JLabel hint = new JLabel("Doble clic para editar  •  Clic derecho → Copiar IP");
        hint.setFont(UITheme.FONT_SMALL); hint.setForeground(UITheme.TEXT_GHOST);
        statusBar.add(hint);

        add(toolbar, BorderLayout.NORTH);
        add(UITheme.scrollPane(table), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuItem mi(String text) {
        JMenuItem m = new JMenuItem(text);
        m.setBackground(UITheme.BG_CARD); m.setForeground(UITheme.TEXT_SECOND); m.setFont(UITheme.FONT_UI); return m;
    }

    private void loadData() {
        try {
            List<Servidor> lista = repo.search(txtBuscar.getText(), (String)cmbAmbiente.getSelectedItem());
            tableModel.setRowCount(0);
            for (Servidor s : lista) {
                tableModel.addRow(new Object[]{s.getNombre(), s.getIp(), s.getHostname(),
                    s.getSo(), s.getRol(), s.getAmbiente(), s.getPuertoSsh(), s.isActivo()});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void abrirForm(Servidor s) {
        ServidorFormDialog dlg = new ServidorFormDialog(SwingUtilities.getWindowAncestor(this), s);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void editarSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String ip = (String) tableModel.getValueAt(row, 1);
        try { repo.search(ip,"TODOS").stream().findFirst().ifPresent(this::abrirForm); }
        catch (Exception ex) { ex.printStackTrace(); }
    }

    private void eliminarSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String nombre = (String) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this,"¿Eliminar servidor \""+nombre+"\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            try {
                String ip = (String) tableModel.getValueAt(row, 1);
                repo.search(ip,"TODOS").stream().findFirst()
                    .ifPresent(s -> { try{repo.delete(s.getId());loadData();}catch(Exception ex){ex.printStackTrace();} });
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void copiarIp() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String ip = (String) tableModel.getValueAt(row, 1);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ip), null);
        JOptionPane.showMessageDialog(this, "IP copiada: " + ip, "✔ Copiado", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── FORMULARIO ───────────────────────────────────────────────────
    static class ServidorFormDialog extends JDialog {
        private boolean saved = false;
        private final Servidor s;

        ServidorFormDialog(Window owner, Servidor srv) {
            super(owner, srv==null?"Nuevo Servidor":"Editar Servidor", ModalityType.APPLICATION_MODAL);
            this.s = srv==null?new Servidor():srv;
            buildUI(); pack();
            setMinimumSize(new Dimension(600, 540));
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
            JLabel lh = new JLabel(s.getId()==0?"🖧  Nuevo Servidor":"✏  Editar Servidor");
            lh.setFont(new Font("Segoe UI",Font.BOLD,15)); lh.setForeground(UITheme.TEXT_PRIMARY);
            header.add(lh);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_DARK);
            form.setBorder(new EmptyBorder(20,24,20,24));
            GridBagConstraints g = new GridBagConstraints();
            g.insets=new Insets(6,6,6,6); g.anchor=GridBagConstraints.WEST;

            JTextField txtNombre  = UITheme.textField("Nombre del servidor",20);
            JTextField txtIp      = UITheme.textField("10.x.x.x",16);
            JTextField txtHost    = UITheme.textField("hostname.dominio",20);
            JTextField txtSo      = UITheme.textField("RHEL 8 / Ubuntu 22",16);
            JTextField txtPuerto  = UITheme.textField("22",6);
            JTextField txtUsuario = UITheme.textField("Usuario SSH",16);
            JComboBox<String> cmbRol = UITheme.comboBox(new String[]{"APP","DB","WEB","PROXY","BALANCER","MONITORING","OTRO"});
            JComboBox<String> cmbAmb = UITheme.comboBox(new String[]{"PROD","HOM","QA","DEV"});
            JCheckBox chkActivo = new JCheckBox("Servidor activo", s.isActivo());
            chkActivo.setBackground(UITheme.BG_DARK); chkActivo.setForeground(UITheme.TEXT_SECOND);
            JTextArea taDesc  = UITheme.textArea(3,30);
            JTextArea taNotas = UITheme.textArea(3,30);

            if (s.getNombre()  !=null) txtNombre.setText(s.getNombre());
            if (s.getIp()      !=null) txtIp.setText(s.getIp());
            if (s.getHostname()!=null) txtHost.setText(s.getHostname());
            if (s.getSo()      !=null) txtSo.setText(s.getSo());
            if (s.getPuertoSsh()!=null) txtPuerto.setText(s.getPuertoSsh());
            if (s.getUsuario() !=null) txtUsuario.setText(s.getUsuario());
            if (s.getRol()     !=null) cmbRol.setSelectedItem(s.getRol());
            if (s.getAmbiente()!=null) cmbAmb.setSelectedItem(s.getAmbiente());
            if (s.getDescripcion()!=null) taDesc.setText(s.getDescripcion());
            if (s.getNotas()   !=null) taNotas.setText(s.getNotas());

            int y=0;
            addRow(form,g,y++,"Nombre:",txtNombre,"IP:",txtIp);
            addRow(form,g,y++,"Hostname:",txtHost,"SO:",txtSo);
            addRow(form,g,y++,"Rol:",cmbRol,"Ambiente:",cmbAmb);
            addRow(form,g,y++,"Puerto SSH:",txtPuerto,"Usuario SSH:",txtUsuario);
            g.gridx=0;g.gridy=y++;g.gridwidth=4;form.add(chkActivo,g);
            addTA(form,g,y++,"Descripción:",taDesc);
            addTA(form,g,y,"Notas:",taNotas);

            JButton btnGuardar  = UITheme.primaryButton("💾  Guardar");
            JButton btnCancelar = UITheme.secondaryButton("Cancelar");
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,14));
            btnPanel.setBackground(UITheme.BG_DEEPEST);
            btnPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
            btnPanel.add(btnCancelar); btnPanel.add(btnGuardar);

            btnGuardar.addActionListener(e->{
                if(txtNombre.getText().isBlank()||txtIp.getText().isBlank()){
                    JOptionPane.showMessageDialog(this,"Nombre e IP son obligatorios.");return;}
                s.setNombre(txtNombre.getText().trim()); s.setIp(txtIp.getText().trim());
                s.setHostname(txtHost.getText().trim()); s.setSo(txtSo.getText().trim());
                s.setRol((String)cmbRol.getSelectedItem()); s.setAmbiente((String)cmbAmb.getSelectedItem());
                s.setPuertoSsh(txtPuerto.getText().trim()); s.setUsuario(txtUsuario.getText().trim());
                s.setActivo(chkActivo.isSelected()); s.setDescripcion(taDesc.getText().trim());
                s.setNotas(taNotas.getText().trim());
                try{ServidorRepository.getInstance().save(s);saved=true;dispose();}
                catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
            });
            btnCancelar.addActionListener(e->dispose());

            root.add(header,BorderLayout.NORTH);
            root.add(UITheme.scrollPane(form),BorderLayout.CENTER);
            root.add(btnPanel,BorderLayout.SOUTH);
            setContentPane(root);
        }

        private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_SMALL);l.setForeground(UITheme.TEXT_DIM);return l;}
        private void addRow(JPanel p,GridBagConstraints g,int y,String l1,JComponent c1,String l2,JComponent c2){
            g.gridwidth=1;g.fill=GridBagConstraints.NONE;
            g.gridx=0;g.gridy=y;p.add(lbl(l1),g);
            g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(c1,g);
            g.gridx=2;g.fill=GridBagConstraints.NONE;p.add(lbl(l2),g);
            g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;p.add(c2,g);
        }
        private void addTA(JPanel p,GridBagConstraints g,int y,String label,JTextArea ta){
            g.gridx=0;g.gridy=y;g.gridwidth=4;g.fill=GridBagConstraints.HORIZONTAL;p.add(lbl(label),g);
            g.gridy=y+1;g.fill=GridBagConstraints.BOTH;g.weightx=1;g.weighty=0.3;
            JScrollPane sp=UITheme.scrollPane(ta);sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_INPUT));
            sp.setPreferredSize(new Dimension(0,80));p.add(sp,g);g.weighty=0;g.weightx=0;
        }
        boolean isSaved(){return saved;}
    }
}
