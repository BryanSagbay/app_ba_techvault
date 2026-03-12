package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ContrasenaDAO;
import baustro.fin.ec.model.Contrasena;
import baustro.fin.ec.security.EncryptionUtil;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ContrasenaPanel extends JPanel {

    private final ContrasenaDAO dao = new ContrasenaDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private List<Contrasena> allData = new ArrayList<>();

    public ContrasenaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(10,0));
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10,20,10,16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Gestor de Contraseñas");
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico=IconManager.getIcon(IconManager.ICON_PASSWORD,22);
        if(ico!=null&&ico.getIconWidth()>1){title.setIcon(ico);title.setIconTextGap(8);}

        titlePane.add(title);

        // Categorías comunes para contraseñas
        String[] cats={"BD","Servidor","App","VPN","Email","API","Web","Sistema"};
        hsf = new HeaderSearchFilter(
                "Buscar titulo, usuario, categoría...",
                new HeaderSearchFilter.ComboConfig("Categoría", cats, "Todas"),
                new HeaderSearchFilter.ComboConfig("Ordenar", new String[]{"Titulo Z-A","Categoría","Reciente"}, "Titulo A-Z")
        ).onChanged(this::applyFilters);

        header.add(titlePane,BorderLayout.WEST);
        header.add(hsf,BorderLayout.EAST);

        JPanel statsBar=new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        statsBar.setBackground(new Color(20,26,38));
        statsBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER));
        statsLabel=new JLabel();statsLabel.setFont(UIConstants.FONT_SMALL);statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        String[] cols={"#","Titulo","Usuario","Categoría","URL"};
        tableModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel);StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)editSelected();}});

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        bottom.setBackground(UIConstants.BG_DARK);
        JButton btnNew=StyledComponents.addButton("Nueva");
        btnNew.addActionListener(e->openForm(null));
        JButton btnEdit     =StyledComponents.editButton("Editar");
        JButton btnCopyPass =StyledComponents.copyButton("Copiar Contrasena");
        JButton btnCopyUser =StyledComponents.copyButton("Copiar Usuario");
        JButton btnDelete   =StyledComponents.dangerButton("Eliminar");
        btnEdit.addActionListener(e->editSelected());
        btnCopyPass.addActionListener(e->copyField("pass"));
        btnCopyUser.addActionListener(e->copyField("user"));
        btnDelete.addActionListener(e->deleteSelected());
        bottom.add(btnNew);bottom.add(btnEdit);bottom.add(btnCopyPass);bottom.add(btnCopyUser);bottom.add(btnDelete);

        JPanel center=new JPanel(new BorderLayout());
        center.add(statsBar,BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table),BorderLayout.CENTER);
        add(header,BorderLayout.NORTH);add(center,BorderLayout.CENTER);add(bottom,BorderLayout.SOUTH);
    }

    private void loadData(){
        try{allData=dao.findAll();applyFilters();}
        catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
    }

    private void applyFilters(){
        String q  =hsf.getQuery().toLowerCase();
        String cat=hsf.getFilter(0);String sort=hsf.getFilter(1);
        Stream<Contrasena> s=allData.stream();
        if(!q.isEmpty())   s=s.filter(c->nv(c.getTitulo(),q)||nv(c.getUsuario(),q)||nv(c.getCategoria(),q));
        if(!cat.isEmpty()) s=s.filter(c->cat.equals(c.getCategoria()));
        Comparator<Contrasena> cmp=switch(sort){
            case "Categoría" -> Comparator.comparing(c->nvl(c.getCategoria()));
            case "Reciente"  -> Comparator.comparing((Contrasena c)->nvl(c.getTitulo())).reversed();
            default          -> Comparator.comparing(c->nvl(c.getTitulo()));
        };
        List<Contrasena> res=s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i=1;for(Contrasena c:res)
            tableModel.addRow(new Object[]{i++,c.getTitulo(),c.getUsuario(),c.getCategoria(),c.getUrl()});

        // Agrupar categorías para stats
        Map<String,Long> byCat=res.stream()
            .collect(java.util.stream.Collectors.groupingBy(c->nvl(c.getCategoria()),java.util.stream.Collectors.counting()));
        String catStr=byCat.entrySet().stream().filter(e->!e.getKey().isEmpty())
            .map(e->e.getKey()+": "+e.getValue()).reduce((a,b)->a+" | "+b).orElse("");
        statsLabel.setText("  Total: "+res.size()+(catStr.isEmpty()?"":" — "+catStr));
    }

    private boolean nv(String f,String q){return f!=null&&f.toLowerCase().contains(q);}
    private String nvl(String s){return s==null?"":s;}

    private Contrasena getSelected(){
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Seleccione una entrada.");return null;}
        String tit=(String)tableModel.getValueAt(row,1);
        return allData.stream().filter(c->tit.equals(c.getTitulo())).findFirst().orElse(null);
    }

    private void editSelected(){Contrasena c=getSelected();if(c!=null)openForm(c);}
    private void deleteSelected(){
        Contrasena c=getSelected();if(c==null)return;
        int ok=JOptionPane.showConfirmDialog(this,"Eliminar \""+c.getTitulo()+"\"?","Confirmar",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){try{dao.delete(c.getId());loadData();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}}
    }
    private void copyField(String f){
        Contrasena c=getSelected();if(c==null)return;
        try{
            String val=f.equals("pass")?EncryptionUtil.decrypt(c.getContrasenaCifrada()):c.getUsuario();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(val),null);
            JOptionPane.showMessageDialog(this,f.equals("pass")?"Contrasena copiada":"Usuario copiado: "+val,"Copiado",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Error descifrando.","Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void openForm(Contrasena existing){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),existing==null?"Nueva Contrasena":"Editar Contrasena",true);
        d.setSize(480,400);d.setLocationRelativeTo(this);d.getContentPane().setBackground(UIConstants.BG_PANEL);d.setLayout(new BorderLayout());
        JPanel form=new JPanel(new GridBagLayout());form.setBackground(UIConstants.BG_PANEL);form.setBorder(BorderFactory.createEmptyBorder(20,24,10,24));
        GridBagConstraints gbc=new GridBagConstraints();gbc.fill=GridBagConstraints.HORIZONTAL;gbc.insets=new Insets(6,6,6,6);gbc.weightx=1;
        JTextField fTit=StyledComponents.styledTextField("Nombre del servicio");
        JTextField fUser=StyledComponents.styledTextField("usuario@dominio");
        JPasswordField fPass=StyledComponents.styledPasswordField();
        JTextField fUrl=StyledComponents.styledTextField("https://... o IP");
        JTextField fCat=StyledComponents.styledTextField("BD, Servidor, App...");
        JTextArea fNotas=StyledComponents.styledTextArea(3,20);
        JCheckBox showPw=new JCheckBox("Mostrar contrasena");
        showPw.setForeground(UIConstants.TEXT_SECONDARY);showPw.setBackground(UIConstants.BG_PANEL);showPw.setFont(UIConstants.FONT_SMALL);
        showPw.addItemListener(e->fPass.setEchoChar(showPw.isSelected()?(char)0:'*'));
        if(existing!=null){fTit.setText(existing.getTitulo());fUser.setText(existing.getUsuario());
            try{fPass.setText(EncryptionUtil.decrypt(existing.getContrasenaCifrada()));}catch(Exception ex){fPass.setText("");}
            fUrl.setText(existing.getUrl());fCat.setText(existing.getCategoria());fNotas.setText(existing.getNotas());}
        int r=0;
        row2(form,gbc,r++,"Titulo *",fTit);row2(form,gbc,r++,"Usuario",fUser);
        row2(form,gbc,r++,"Contrasena *",fPass);
        gbc.gridy=r++*2;gbc.gridx=0;gbc.gridwidth=2;form.add(showPw,gbc);
        row2(form,gbc,r++,"URL / Sistema",fUrl);row2(form,gbc,r++,"Categoría",fCat);
        row2(form,gbc,r,"Notas",new JScrollPane(fNotas));
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,10));bp.setBackground(UIConstants.BG_DARK);
        JButton bS=StyledComponents.successButton("Guardar");JButton bC=StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e->d.dispose());
        bS.addActionListener(e->{
            String pw=new String(fPass.getPassword());
            if(fTit.getText().trim().isEmpty()||pw.isEmpty()){JOptionPane.showMessageDialog(d,"Titulo y contrasena obligatorios.");return;}
            try{
                Contrasena c=existing!=null?existing:new Contrasena();
                c.setTitulo(fTit.getText().trim());c.setUsuario(fUser.getText().trim());
                c.setContrasenaCifrada(EncryptionUtil.encrypt(pw));
                c.setUrl(fUrl.getText().trim());c.setCategoria(fCat.getText().trim());c.setNotas(fNotas.getText().trim());
                if(existing==null)dao.insert(c);else dao.update(c);loadData();d.dispose();
            }catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        bp.add(bS);bp.add(bC);
        JScrollPane sp=new JScrollPane(form);sp.getViewport().setBackground(UIConstants.BG_PANEL);sp.setBorder(null);
        d.add(sp,BorderLayout.CENTER);d.add(bp,BorderLayout.SOUTH);d.setVisible(true);
    }
    private void row2(JPanel p,GridBagConstraints g,int row,String lbl,Component c){
        g.gridy=row*2;g.gridx=0;g.gridwidth=2;g.weightx=1;
        JLabel l=new JLabel(lbl);l.setFont(UIConstants.FONT_SMALL);l.setForeground(UIConstants.TEXT_SECONDARY);p.add(l,g);
        g.gridy=row*2+1;p.add(c,g);}
}
