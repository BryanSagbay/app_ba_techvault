package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ServidorDAO;
import baustro.fin.ec.model.Servidor;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ServidorPanel extends JPanel {

    private final ServidorDAO dao = new ServidorDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private List<Servidor> allData = new ArrayList<>();

    public ServidorPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        loadData();
    }

    private void buildUI() {
        //  HEADER: título + botones
        JPanel header = new JPanel(new BorderLayout(10,0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10,20,10,16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Servidores");
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_SERVIDOR,22);
        if(ico!=null&&ico.getIconWidth()>1){title.setIcon(ico);title.setIconTextGap(8);}
        titlePane.add(title);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        headerButtons.setOpaque(false);
        JButton btnNew    = StyledComponents.addButton("Nuevo");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnCopyIP = StyledComponents.copyButton("Copiar IP");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnCopyIP.addActionListener(e -> copyIP());
        btnDelete.addActionListener(e -> deleteSelected());
        headerButtons.add(btnNew);
        headerButtons.add(btnEdit);
        headerButtons.add(btnCopyIP);
        headerButtons.add(btnDelete);

        header.add(titlePane,     BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        //  STATS BAR
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER));
        statsLabel = new JLabel();
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        //  TABLA
        String[] cols={"#","Nombre","IP / Host","Tipo","Ambiente","SO","Puerto","Estado"};
        tableModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); StyledComponents.styleTable(table);
        int[]w={40,160,130,100,90,80,60,90};
        for(int i=0;i<w.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(7).setCellRenderer(estadoRenderer());
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)editSelected();}});

        //  BOTTOM: filtros
        hsf = new HeaderSearchFilter(
                "Buscar nombre, IP, tipo...",
                new HeaderSearchFilter.ComboConfig("Tipo",     UIConstants.TIPOS_SERVIDOR,  "Todos"),
                new HeaderSearchFilter.ComboConfig("Ambiente", UIConstants.AMBIENTES,        "Todos"),
                new HeaderSearchFilter.ComboConfig("Estado",   UIConstants.ESTADOS_SERVIDOR, "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar",
                        new String[]{"Nombre Z-A","IP A-Z","IP Z-A","Ambiente","Tipo"},
                        "Nombre A-Z")
        ).onChanged(this::applyFilters);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        bottom.add(hsf, BorderLayout.CENTER);

        //  ENSAMBLE
        JPanel center=new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private DefaultTableCellRenderer estadoRenderer(){
        return new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                String s=val!=null?val.toString():"";
                setBackground(sel?UIConstants.ACCENT_BLUE:(row%2==0?UIConstants.BG_CARD:UIConstants.BG_CARD_HOVER));
                setForeground(UIConstants.getEstadoColor(s));
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                return this;
            }
        };
    }

    private void loadData(){
        try{allData=dao.findAll();applyFilters();}
        catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
    }

    private void applyFilters(){
        String q    = hsf.getQuery().toLowerCase();
        String tipo = hsf.getFilter(0);  // índice 0 → Tipo
        String amb  = hsf.getFilter(1);  // índice 1 → Ambiente
        String est  = hsf.getFilter(2);  // índice 2 → Estado
        String sort = hsf.getFilter(3);  // índice 3 → Ordenar

        Stream<Servidor> s = allData.stream();

        // Búsqueda por texto: nombre, IP, tipo, SO y descripción
        if (!q.isEmpty())    s = s.filter(sv -> nv(sv.getNombre(),q) || nv(sv.getIp(),q)
                || nv(sv.getTipo(),q)   || nv(sv.getSistemaOperativo(),q)
                || nv(sv.getDescripcion(),q));
        if (!tipo.isEmpty()) s = s.filter(sv -> tipo.equals(sv.getTipo()));
        if (!amb.isEmpty())  s = s.filter(sv -> amb.equals(sv.getAmbiente()));
        if (!est.isEmpty())  s = s.filter(sv -> est.equals(sv.getEstado()));

        Comparator<Servidor> cmp = switch (sort) {
            case "Nombre Z-A" -> Comparator.comparing((Servidor sv) -> nvl(sv.getNombre())).reversed();
            case "IP A-Z"     -> Comparator.comparing(sv -> nvl(sv.getIp()));
            case "IP Z-A"     -> Comparator.comparing((Servidor sv) -> nvl(sv.getIp())).reversed();
            case "Ambiente"   -> Comparator.comparing(sv -> nvl(sv.getAmbiente()));
            case "Tipo"       -> Comparator.comparing(sv -> nvl(sv.getTipo()));
            default           -> Comparator.comparing(sv -> nvl(sv.getNombre())); // "Nombre A-Z"
        };
        List<Servidor> res=s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i=1; for(Servidor sv:res)
            tableModel.addRow(new Object[]{i++,sv.getNombre(),sv.getIp(),sv.getTipo(),sv.getAmbiente(),sv.getSistemaOperativo(),sv.getPuerto(),sv.getEstado()});
        long activos=res.stream().filter(sv->"Activo".equals(sv.getEstado())).count();
        long inactivos=res.stream().filter(sv->"Inactivo".equals(sv.getEstado())).count();
        statsLabel.setText(String.format("  Total: %d   |   Activos: %d   |   Inactivos: %d",res.size(),activos,inactivos));
    }

    private boolean nv(String f,String q){return f!=null&&f.toLowerCase().contains(q);}
    private String nvl(String s){return s==null?"":s;}

    private Servidor getSelected(){
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Seleccione un servidor.");return null;}
        String nombre=(String)tableModel.getValueAt(row,1);
        return allData.stream().filter(s->nombre.equals(s.getNombre())).findFirst().orElse(null);
    }

    private void editSelected(){Servidor s=getSelected();if(s!=null)openForm(s);}
    private void copyIP(){
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Seleccione un servidor.");return;}
        String ip=(String)tableModel.getValueAt(row,2);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ip),null);
        JOptionPane.showMessageDialog(this,"IP copiada: "+ip,"Copiado",JOptionPane.INFORMATION_MESSAGE);
    }
    private void deleteSelected(){
        Servidor s=getSelected();if(s==null)return;
        int ok=JOptionPane.showConfirmDialog(this,"Eliminar servidor "+s.getNombre()+"?","Confirmar",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){try{dao.delete(s.getId());loadData();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}}
    }

    private void openForm(Servidor existing){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),existing==null?"Nuevo Servidor":"Editar Servidor",true);
        d.setSize(600,500);d.setLocationRelativeTo(this);d.getContentPane().setBackground(UIConstants.BG_CARD);d.setLayout(new BorderLayout());
        JPanel form=new JPanel(new GridBagLayout());form.setBackground(UIConstants.BG_CARD);form.setBorder(BorderFactory.createEmptyBorder(20,24,10,24));
        GridBagConstraints gbc=new GridBagConstraints();gbc.fill=GridBagConstraints.HORIZONTAL;gbc.insets=new Insets(5,5,5,5);
        JTextField fNom=StyledComponents.styledTextField("Nombre");JTextField fIp=StyledComponents.styledTextField("192.168.x.x");
        JComboBox<String> fTipo=StyledComponents.styledCombo(UIConstants.TIPOS_SERVIDOR);
        JComboBox<String> fAmb=StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JComboBox<String> fSO=StyledComponents.styledCombo(new String[]{"Linux","Windows","AIX","Solaris","Otro"});
        JTextField fPuerto=StyledComponents.styledTextField("22/443");JTextField fUser=StyledComponents.styledTextField("usuario");
        JComboBox<String> fEst=StyledComponents.styledCombo(UIConstants.ESTADOS_SERVIDOR);
        JTextArea fDesc=StyledComponents.styledTextArea(3,20);JTextArea fNotas=StyledComponents.styledTextArea(3,20);
        if(existing!=null){fNom.setText(existing.getNombre());fIp.setText(existing.getIp());
            sc(fTipo,existing.getTipo());sc(fAmb,existing.getAmbiente());sc(fSO,existing.getSistemaOperativo());
            fPuerto.setText(existing.getPuerto());fUser.setText(existing.getUsuarioAcceso());
            sc(fEst,existing.getEstado());fDesc.setText(existing.getDescripcion());fNotas.setText(existing.getNotas());}
        int r=0;
        ar(form,gbc,r++,"Nombre *",fNom,"IP / Host *",fIp);ar(form,gbc,r++,"Tipo",fTipo,"Ambiente",fAmb);
        ar(form,gbc,r++,"Sistema Operativo",fSO,"Puerto",fPuerto);ar(form,gbc,r++,"Usuario Acceso",fUser,"Estado",fEst);
        af(form,gbc,r++,"Descripción",new JScrollPane(fDesc));af(form,gbc,r,"Notas",new JScrollPane(fNotas));
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,10));bp.setBackground(UIConstants.BG_BASE);
        JButton bS=StyledComponents.successButton("Guardar");JButton bC=StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e->d.dispose());
        bS.addActionListener(e->{
            if(fNom.getText().trim().isEmpty()||fIp.getText().trim().isEmpty()){JOptionPane.showMessageDialog(d,"Nombre e IP obligatorios.");return;}
            Servidor s=existing!=null?existing:new Servidor();
            s.setNombre(fNom.getText().trim());s.setIp(fIp.getText().trim());s.setTipo((String)fTipo.getSelectedItem());
            s.setAmbiente((String)fAmb.getSelectedItem());s.setSistemaOperativo((String)fSO.getSelectedItem());
            s.setPuerto(fPuerto.getText().trim());s.setUsuarioAcceso(fUser.getText().trim());
            s.setEstado((String)fEst.getSelectedItem());s.setDescripcion(fDesc.getText().trim());s.setNotas(fNotas.getText().trim());
            try{if(existing==null)dao.insert(s);else dao.update(s);loadData();d.dispose();}
            catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        bp.add(bS);bp.add(bC);
        JScrollPane sp=new JScrollPane(form);sp.getViewport().setBackground(UIConstants.BG_CARD);sp.setBorder(null);
        d.add(sp,BorderLayout.CENTER);d.add(bp,BorderLayout.SOUTH);d.setVisible(true);
    }
    private void ar(JPanel p,GridBagConstraints g,int row,String l1,Component c1,String l2,Component c2){
        g.gridy=row*2;g.gridx=0;g.gridwidth=1;g.weightx=0;p.add(fl(l1),g);g.gridx=1;g.weightx=.5;p.add(c1,g);
        g.gridx=2;g.weightx=0;p.add(fl(l2),g);g.gridx=3;g.weightx=.5;p.add(c2,g);}
    private void af(JPanel p,GridBagConstraints g,int row,String lbl,Component c){
        g.gridy=row*2;g.gridx=0;g.gridwidth=4;g.weightx=1;p.add(fl(lbl),g);g.gridy=row*2+1;p.add(c,g);g.gridwidth=1;}
    private JLabel fl(String t){JLabel l=new JLabel(t);l.setFont(UIConstants.FONT_SMALL);l.setForeground(UIConstants.TEXT_SECONDARY);return l;}
    private void sc(JComboBox<String> cb,String v){if(v==null)return;for(int i=0;i<cb.getItemCount();i++)if(v.equals(cb.getItemAt(i))){cb.setSelectedIndex(i);return;}}
}