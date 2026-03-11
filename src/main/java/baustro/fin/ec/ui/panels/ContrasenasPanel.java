package baustro.fin.ec.ui.panels;

import baustro.fin.ec.model.Contrasena;
import baustro.fin.ec.repository.ContrasenaRepository;
import baustro.fin.ec.service.ConfigService;
import baustro.fin.ec.ui.UITheme;
import baustro.fin.ec.util.CryptoUtil;

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

public class ContrasenasPanel extends JPanel {

    private final ContrasenaRepository repo = ContrasenaRepository.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtBuscar;
    private JComboBox<String> cmbCategoria;
    private JPanel lockScreen, contentPanel;

    public ContrasenasPanel() {
        setLayout(new CardLayout());
        setBackground(UITheme.BG_PANEL);
        buildLockScreen();
        buildContent();
        showLock();
    }

    // ── LOCK SCREEN ──────────────────────────────────────────────────
    private void buildLockScreen() {
        lockScreen = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.BG_DEEPEST,
                    getWidth(), getHeight(), new Color(0x0F1525));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        lockScreen.setOpaque(false);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.ACCENT_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(44, 60, 44, 60));
        card.setPreferredSize(new Dimension(420, 340));

        JLabel iconLbl = new JLabel("🔐", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        iconLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("Módulo Protegido", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("<html><center>Este módulo requiere tu contraseña<br>maestra para acceder.</center></html>", SwingConstants.CENTER);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(UITheme.TEXT_DIM);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnDesbloquear = UITheme.primaryButton("🔓  Desbloquear módulo");
        btnDesbloquear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDesbloquear.setAlignmentX(CENTER_ALIGNMENT);
        btnDesbloquear.setMaximumSize(new Dimension(260, 44));
        btnDesbloquear.addActionListener(e -> autenticar());

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(14));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(28));
        card.add(btnDesbloquear);

        lockScreen.add(card);
        add(lockScreen, "LOCK");
    }

    private void autenticar() {
        // Verificar si hay master password configurado
        String hash = ConfigService.getInstance().get("master_password_hash");
        if (hash == null || hash.isBlank()) {
            JOptionPane.showMessageDialog(this,
                "No tienes contraseña maestra configurada.\nVe a Configuración para crearla.",
                "Sin configuración", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Dialog de autenticación
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this) instanceof Frame f ? f : null,
            "Acceso Protegido", true);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UITheme.BG_CARD);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UITheme.BG_CARD);
        top.setBorder(new EmptyBorder(28, 36, 20, 36));

        JLabel licon = new JLabel("🔑", SwingConstants.CENTER);
        licon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        licon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel ltitle = new JLabel("Ingresa tu contraseña maestra", SwingConstants.CENTER);
        ltitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ltitle.setForeground(UITheme.TEXT_PRIMARY);
        ltitle.setAlignmentX(CENTER_ALIGNMENT);

        JPasswordField pfPass = UITheme.passwordField(22);
        pfPass.setMaximumSize(new Dimension(300, 38));
        pfPass.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.RED);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        top.add(licon);
        top.add(Box.createVerticalStrut(10));
        top.add(ltitle);
        top.add(Box.createVerticalStrut(18));
        top.add(pfPass);
        top.add(Box.createVerticalStrut(6));
        top.add(lblError);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btns.setBackground(UITheme.BG_DEEPEST);
        btns.setBorder(BorderFactory.createMatteBorder(1,0,0,0, UITheme.BORDER));
        JButton btnOk  = UITheme.primaryButton("Entrar");
        JButton btnCan = UITheme.secondaryButton("Cancelar");
        btns.add(btnCan); btns.add(btnOk);

        Runnable doLogin = () -> {
            String input = new String(pfPass.getPassword());
            String salt  = ConfigService.getInstance().get("master_password_salt");
            if (CryptoUtil.verifyMasterPassword(input, hash, salt)) {
                try {
                    CryptoUtil.loadSessionKey(input, salt);
                    dlg.dispose();
                    showContent();
                    loadData();
                } catch (Exception ex) { lblError.setText("Error: " + ex.getMessage()); }
            } else {
                lblError.setText("Contraseña incorrecta. Intenta de nuevo.");
                pfPass.setText("");
                pfPass.requestFocus();
            }
        };
        btnOk.addActionListener(e -> doLogin.run());
        btnCan.addActionListener(e -> dlg.dispose());
        pfPass.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) doLogin.run(); }
        });

        panel.add(top, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);
        dlg.setContentPane(panel);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(360, 260));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ── CONTENT ──────────────────────────────────────────────────────
    private void buildContent() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UITheme.BG_PANEL);

        JPanel toolbar = UITheme.toolbarPanel();
        txtBuscar    = UITheme.textField("🔍  Título, usuario, URL...", 26);
        cmbCategoria = UITheme.comboBox(new String[]{"TODAS","SSH","DB","APP","WEB","SISTEMA"});
        JButton btnBuscar   = UITheme.secondaryButton("Buscar");
        JButton btnNueva    = UITheme.primaryButton("➕  Nueva Credencial");
        JButton btnBloquear = UITheme.dangerButton("🔒  Bloquear");

        btnBuscar.addActionListener(e -> loadData());
        btnNueva.addActionListener(e -> abrirForm(null));
        btnBloquear.addActionListener(e -> { CryptoUtil.clearSession(); showLock(); });
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) loadData(); }
        });

        toolbar.add(txtBuscar);
        toolbar.add(UITheme.sectionLabel("Categoría:")); toolbar.add(cmbCategoria);
        toolbar.add(btnBuscar);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(btnNueva);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(btnBloquear);

        String[] cols = {"Título","Usuario","Contraseña","Categoría","URL / Notas"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        int[] w = {180,150,140,100,0};
        for (int i=0;i<w.length;i++) if(w[i]>0) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // Contraseña siempre ●●●●●
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,focus,row,col);
                lbl.setText("  ● ● ● ● ● ● ●");
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lbl.setForeground(sel ? UITheme.TEXT_DIM : UITheme.BORDER_INPUT);
                lbl.setBackground(sel ? UITheme.BG_ROW_SEL : (row%2==0?UITheme.BG_PANEL:UITheme.BG_ROW_ALT));
                lbl.setOpaque(true);
                return lbl;
            }
        });
        // Categoría badge
        table.getColumnModel().getColumn(3).setCellRenderer(UITheme.badgeRenderer(cat -> switch(cat){
            case "SSH"    -> new Color[]{UITheme.GREEN_BG,  UITheme.GREEN};
            case "DB"     -> new Color[]{new Color(0x1E1B4B), new Color(0xA5B4FC)};
            case "APP"    -> new Color[]{new Color(0x1C1917), new Color(0xFBBF24)};
            case "WEB"    -> new Color[]{UITheme.ACCENT_DIM, UITheme.ACCENT_LIGHT};
            case "SISTEMA"-> new Color[]{UITheme.ORANGE_BG, UITheme.ORANGE};
            default       -> new Color[]{UITheme.BG_CARD,   UITheme.TEXT_DIM};
        }));

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2) editarSeleccionada();
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
        JMenuItem miVer      = mi("👁   Ver contraseña");
        JMenuItem miCopiarPw = mi("📋  Copiar contraseña");
        JMenuItem miCopiarUsr= mi("👤  Copiar usuario");
        JMenuItem miEditar   = mi("✏   Editar");
        JMenuItem miEliminar = mi("🗑   Eliminar"); miEliminar.setForeground(UITheme.RED);
        miVer.addActionListener(e -> verContrasena());
        miCopiarPw.addActionListener(e -> copiarContrasena());
        miCopiarUsr.addActionListener(e -> copiarUsuario());
        miEditar.addActionListener(e -> editarSeleccionada());
        miEliminar.addActionListener(e -> eliminarSeleccionada());
        popup.add(miVer); popup.add(miCopiarPw); popup.add(miCopiarUsr);
        popup.addSeparator(); popup.add(miEditar); popup.add(miEliminar);
        table.setComponentPopupMenu(popup);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT,16,4));
        statusBar.setBackground(UITheme.BG_DEEPEST);
        statusBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
        JLabel hint = new JLabel("Clic derecho → Ver / Copiar contraseña  •  Las contraseñas están cifradas con AES-256-GCM");
        hint.setFont(UITheme.FONT_SMALL); hint.setForeground(UITheme.TEXT_GHOST);
        statusBar.add(hint);

        contentPanel.add(toolbar, BorderLayout.NORTH);
        contentPanel.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        contentPanel.add(statusBar, BorderLayout.SOUTH);
        add(contentPanel, "CONTENT");
    }

    private JMenuItem mi(String text) {
        JMenuItem m = new JMenuItem(text);
        m.setBackground(UITheme.BG_CARD); m.setForeground(UITheme.TEXT_SECOND); m.setFont(UITheme.FONT_UI); return m;
    }

    private void showLock()    { ((CardLayout)getLayout()).show(this,"LOCK"); }
    private void showContent() { ((CardLayout)getLayout()).show(this,"CONTENT"); }

    private void loadData() {
        try {
            List<Contrasena> lista = repo.search(txtBuscar.getText(), (String)cmbCategoria.getSelectedItem());
            tableModel.setRowCount(0);
            for (Contrasena c : lista) {
                tableModel.addRow(new Object[]{c.getTitulo(), c.getUsuario(), "••••••",
                    c.getCategoria(), c.getUrl()!=null?c.getUrl():c.getNotas()});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private Contrasena getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String titulo = (String) tableModel.getValueAt(row, 0);
        try { return repo.search(titulo,"TODAS").stream().filter(c->titulo.equals(c.getTitulo())).findFirst().orElse(null); }
        catch (Exception ex) { return null; }
    }

    private void verContrasena() {
        Contrasena c = getSelected();
        if (c==null) return;
        try {
            String plain = CryptoUtil.decrypt(c.getPasswordCifrada());
            JPanel p = new JPanel(new BorderLayout(0,10));
            p.setBackground(UITheme.BG_CARD);
            p.setBorder(new EmptyBorder(16,20,16,20));
            JLabel lbl = new JLabel("Contraseña para: " + c.getTitulo());
            lbl.setForeground(UITheme.TEXT_DIM); lbl.setFont(UITheme.FONT_SMALL);
            JTextField tf = new JTextField(plain);
            tf.setEditable(false);
            tf.setFont(new Font("Consolas", Font.BOLD, 16));
            tf.setBackground(UITheme.BG_DEEPEST);
            tf.setForeground(UITheme.GREEN);
            tf.setCaretColor(UITheme.GREEN);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_DIM),
                new EmptyBorder(8,12,8,12)));
            p.add(lbl, BorderLayout.NORTH);
            p.add(tf, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, p, "🔓 Contraseña", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Error al descifrar: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copiarContrasena() {
        Contrasena c = getSelected();
        if (c==null) return;
        try {
            String plain = CryptoUtil.decrypt(c.getPasswordCifrada());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(plain),null);
            JOptionPane.showMessageDialog(this,"Contraseña copiada al portapapeles.","✔ Copiado",JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void copiarUsuario() {
        Contrasena c = getSelected();
        if (c==null||c.getUsuario()==null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(c.getUsuario()),null);
        JOptionPane.showMessageDialog(this,"Usuario copiado: "+c.getUsuario(),"✔ Copiado",JOptionPane.INFORMATION_MESSAGE);
    }

    private void editarSeleccionada() {
        ContrasenaFormDialog dlg = new ContrasenaFormDialog(SwingUtilities.getWindowAncestor(this), getSelected());
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void eliminarSeleccionada() {
        int row = table.getSelectedRow();
        if (row<0) return;
        String titulo = (String) tableModel.getValueAt(row,0);
        if (JOptionPane.showConfirmDialog(this,"¿Eliminar \""+titulo+"\"?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            Contrasena c = getSelected();
            if (c!=null) try{repo.delete(c.getId());loadData();}catch(Exception ex){ex.printStackTrace();}
        }
    }

    private void abrirForm(Contrasena c) {
        ContrasenaFormDialog dlg = new ContrasenaFormDialog(SwingUtilities.getWindowAncestor(this), c);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    // ── FORMULARIO ───────────────────────────────────────────────────
    static class ContrasenaFormDialog extends JDialog {
        private boolean saved = false;
        private final Contrasena contrasena;

        ContrasenaFormDialog(Window owner, Contrasena c) {
            super(owner, c==null?"Nueva Credencial":"Editar Credencial", ModalityType.APPLICATION_MODAL);
            this.contrasena = c==null?new Contrasena():c;
            buildUI(); pack();
            setMinimumSize(new Dimension(500,400));
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
            JLabel lh = new JLabel(contrasena.getId()==0?"🔐  Nueva Credencial":"✏  Editar Credencial");
            lh.setFont(new Font("Segoe UI",Font.BOLD,15)); lh.setForeground(UITheme.TEXT_PRIMARY);
            header.add(lh);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(UITheme.BG_DARK);
            form.setBorder(new EmptyBorder(20,24,20,24));
            GridBagConstraints g = new GridBagConstraints();
            g.insets=new Insets(7,6,7,6); g.anchor=GridBagConstraints.WEST;

            JTextField txtTitulo  = UITheme.textField("Nombre de la credencial",28);
            JTextField txtUsuario = UITheme.textField("Usuario / login",28);
            JTextField txtUrl     = UITheme.textField("URL o IP",28);
            JTextArea taNotas     = UITheme.textArea(3,28);
            JComboBox<String> cmbCat = UITheme.comboBox(new String[]{"SSH","DB","APP","WEB","SISTEMA","OTRO"});

            JPasswordField pfPass = UITheme.passwordField(28);
            JCheckBox chkMostrar  = new JCheckBox("Mostrar");
            chkMostrar.setBackground(UITheme.BG_DARK); chkMostrar.setForeground(UITheme.TEXT_DIM);
            chkMostrar.addActionListener(e -> pfPass.setEchoChar(chkMostrar.isSelected()?(char)0:'●'));

            if (contrasena.getTitulo()  !=null) txtTitulo.setText(contrasena.getTitulo());
            if (contrasena.getUsuario() !=null) txtUsuario.setText(contrasena.getUsuario());
            if (contrasena.getUrl()     !=null) txtUrl.setText(contrasena.getUrl());
            if (contrasena.getNotas()   !=null) taNotas.setText(contrasena.getNotas());
            if (contrasena.getCategoria()!=null) cmbCat.setSelectedItem(contrasena.getCategoria());
            if (contrasena.getPasswordCifrada()!=null && !contrasena.getPasswordCifrada().isBlank()) {
                try { pfPass.setText(CryptoUtil.decrypt(contrasena.getPasswordCifrada())); } catch (Exception ex) {}
            }

            JPanel passRow = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));
            passRow.setBackground(UITheme.BG_DARK);
            passRow.add(pfPass); passRow.add(chkMostrar);

            int y=0;
            addRow(form,g,y++,"Título:",txtTitulo);
            addRow(form,g,y++,"Usuario:",txtUsuario);
            addRow(form,g,y++,"Contraseña:",passRow);
            addRow(form,g,y++,"Categoría:",cmbCat);
            addRow(form,g,y++,"URL / IP:",txtUrl);
            g.gridx=0;g.gridy=y;g.gridwidth=1;form.add(lbl("Notas:"),g);
            g.gridx=1;g.gridy=y;g.gridwidth=2;g.fill=GridBagConstraints.BOTH;g.weightx=1;g.weighty=1;
            JScrollPane sp=UITheme.scrollPane(taNotas);sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_INPUT));
            sp.setPreferredSize(new Dimension(0,70));form.add(sp,g);

            JButton btnGuardar  = UITheme.primaryButton("💾  Guardar");
            JButton btnCancelar = UITheme.secondaryButton("Cancelar");
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,14));
            btnPanel.setBackground(UITheme.BG_DEEPEST);
            btnPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0,UITheme.BORDER));
            btnPanel.add(btnCancelar); btnPanel.add(btnGuardar);

            btnGuardar.addActionListener(e->{
                if(txtTitulo.getText().isBlank()){JOptionPane.showMessageDialog(this,"El título es obligatorio.");return;}
                String pass=new String(pfPass.getPassword());
                if(pass.isBlank()){JOptionPane.showMessageDialog(this,"La contraseña no puede estar vacía.");return;}
                try{
                    contrasena.setTitulo(txtTitulo.getText().trim());
                    contrasena.setUsuario(txtUsuario.getText().trim());
                    contrasena.setPasswordCifrada(CryptoUtil.encrypt(pass));
                    contrasena.setCategoria((String)cmbCat.getSelectedItem());
                    contrasena.setUrl(txtUrl.getText().trim());
                    contrasena.setNotas(taNotas.getText().trim());
                    ContrasenaRepository.getInstance().save(contrasena);
                    saved=true;dispose();
                }catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
            });
            btnCancelar.addActionListener(e->dispose());

            root.add(header,BorderLayout.NORTH);
            root.add(UITheme.scrollPane(form),BorderLayout.CENTER);
            root.add(btnPanel,BorderLayout.SOUTH);
            setContentPane(root);
        }

        private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_SMALL);l.setForeground(UITheme.TEXT_DIM);return l;}
        private void addRow(JPanel p,GridBagConstraints g,int y,String label,JComponent comp){
            g.gridwidth=1;g.fill=GridBagConstraints.NONE;g.weightx=0;
            g.gridx=0;g.gridy=y;p.add(lbl(label),g);
            g.gridx=1;g.gridwidth=2;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;p.add(comp,g);
        }
        boolean isSaved(){return saved;}
    }
}
