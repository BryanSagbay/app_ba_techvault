package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.TareaDAO;
import baustro.fin.ec.model.Tarea;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TareaPanel extends JPanel {

    private final TareaDAO dao = new TareaDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private List<Tarea> allData = new ArrayList<>();

    public TareaPanel() {
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
        JLabel title = new JLabel("Tareas & To-Do");
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico=IconManager.getIcon(IconManager.ICON_TAREA,22);
        if(ico!=null&&ico.getIconWidth()>1){title.setIcon(ico);title.setIconTextGap(8);}
        JButton btnNew=StyledComponents.addButton("Nueva");
        btnNew.addActionListener(e->openForm(null));
        titlePane.add(title); titlePane.add(btnNew);

        hsf = new HeaderSearchFilter(
                "Buscar tarea, categoria...",
                new HeaderSearchFilter.ComboConfig("Estado",    UIConstants.ESTADOS_TAREA, "Todos"),
                new HeaderSearchFilter.ComboConfig("Prioridad", UIConstants.PRIORIDADES,   "Todas"),
                new HeaderSearchFilter.ComboConfig("Vence",     new String[]{"Hoy","Esta semana","Vencidas"}, "Cualquier fecha"),
                new HeaderSearchFilter.ComboConfig("Ordenar",   new String[]{"Prioridad Alta","Fecha limite","Estado","Titulo"}, "Prioridad Alta")
        ).onChanged(this::applyFilters);

        header.add(titlePane,BorderLayout.WEST);
        header.add(hsf,BorderLayout.EAST);

        JPanel statsBar=new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        statsBar.setBackground(new Color(20,26,38));
        statsBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER));
        statsLabel=new JLabel();statsLabel.setFont(UIConstants.FONT_SMALL);statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        String[] cols={"#","Titulo","Categoria","Prioridad","Estado","Fecha Limite","Descripcion"};
        tableModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel);StyledComponents.styleTable(table);
        int[]w={40,220,100,80,90,100,240};
        for(int i=0;i<w.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(3).setCellRenderer(colorRenderer("prioridad"));
        table.getColumnModel().getColumn(4).setCellRenderer(colorRenderer("estado"));
        table.getColumnModel().getColumn(5).setCellRenderer(fechaRenderer());
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)editSelected();}});

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        bottom.setBackground(UIConstants.BG_DARK);
        JButton btnEdit    =StyledComponents.editButton("Editar");
        JButton btnDelete  =StyledComponents.dangerButton("Eliminar");
        JButton btnProgress=StyledComponents.primaryButton("En Progreso",UIConstants.ACCENT_ORANGE);
        JButton btnDone    =StyledComponents.successButton("Completada");
        btnEdit.addActionListener(e->editSelected());
        btnDelete.addActionListener(e->deleteSelected());
        btnProgress.addActionListener(e->changeStatus("En Progreso"));
        btnDone.addActionListener(e->changeStatus("Completada"));
        bottom.add(btnEdit);bottom.add(btnDelete);bottom.add(btnProgress);bottom.add(btnDone);

        JPanel center=new JPanel(new BorderLayout());
        center.add(statsBar,BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table),BorderLayout.CENTER);
        add(header,BorderLayout.NORTH);add(center,BorderLayout.CENTER);add(bottom,BorderLayout.SOUTH);
    }

    private DefaultTableCellRenderer colorRenderer(String type){
        return new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                String s=val!=null?val.toString():"";
                setBackground(sel?UIConstants.ACCENT_BLUE:(row%2==0?UIConstants.BG_PANEL:UIConstants.TABLE_ROW_ALT));
                setForeground(type.equals("prioridad")?UIConstants.getPrioridadColor(s):UIConstants.getEstadoColor(s));
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
                return this;
            }
        };
    }

    private DefaultTableCellRenderer fechaRenderer(){
        return new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object val,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,val,sel,focus,row,col);
                String fecha=val!=null?val.toString():"";
                setBackground(sel?UIConstants.ACCENT_BLUE:(row%2==0?UIConstants.BG_PANEL:UIConstants.TABLE_ROW_ALT));
                Color fc=UIConstants.TEXT_PRIMARY;
                if(!fecha.isEmpty()){
                    try{
                        LocalDate d=LocalDate.parse(fecha);
                        LocalDate hoy=LocalDate.now();
                        if(d.isBefore(hoy))       fc=UIConstants.ACCENT_RED;
                        else if(d.isEqual(hoy))   fc=UIConstants.ACCENT_ORANGE;
                        else if(d.isBefore(hoy.plusDays(3))) fc=new Color(250,200,50);
                    }catch(Exception ignored){}
                }
                setForeground(fc);
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
        String q    =hsf.getQuery().toLowerCase();
        String est  =hsf.getFilter(0);String prio=hsf.getFilter(1);
        String vence=hsf.getFilter(2);String sort=hsf.getFilter(3);
        LocalDate hoy=LocalDate.now();
        Stream<Tarea> s=allData.stream();
        if(!q.isEmpty())    s=s.filter(t->nv(t.getTitulo(),q)||nv(t.getDescripcion(),q)||nv(t.getCategoria(),q));
        if(!est.isEmpty())  s=s.filter(t->est.equals(t.getEstado()));
        if(!prio.isEmpty()) s=s.filter(t->prio.equals(t.getPrioridad()));
        if(!vence.isEmpty()){
            s=s.filter(t->{
                if(t.getFechaLimite()==null||t.getFechaLimite().isEmpty())return false;
                try{LocalDate fd=LocalDate.parse(t.getFechaLimite());
                    return switch(vence){
                        case "Hoy"         ->fd.isEqual(hoy);
                        case "Esta semana" ->!fd.isBefore(hoy)&&fd.isBefore(hoy.plusDays(7));
                        case "Vencidas"    ->fd.isBefore(hoy)&&!"Completada".equals(t.getEstado());
                        default            ->true;
                    };
                }catch(Exception e){return false;}
            });
        }
        Comparator<Tarea> cmp=switch(sort){
            case "Fecha limite" ->Comparator.comparing(t->nvl(t.getFechaLimite()));
            case "Estado"       ->Comparator.comparing(t->nvl(t.getEstado()));
            case "Titulo"       ->Comparator.comparing(t->nvl(t.getTitulo()));
            default             ->Comparator.comparingInt(t->prioOrd(t.getPrioridad()));
        };
        List<Tarea> res=s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i=1;for(Tarea t:res)
            tableModel.addRow(new Object[]{i++,t.getTitulo(),t.getCategoria(),t.getPrioridad(),t.getEstado(),t.getFechaLimite(),t.getDescripcion()});
        long pend=res.stream().filter(t->"Pendiente".equals(t.getEstado())).count();
        long enp=res.stream().filter(t->"En Progreso".equals(t.getEstado())).count();
        long comp=res.stream().filter(t->"Completada".equals(t.getEstado())).count();
        long venc;try{venc=res.stream().filter(t->t.getFechaLimite()!=null&&!t.getFechaLimite().isEmpty()&&LocalDate.parse(t.getFechaLimite()).isBefore(hoy)&&!"Completada".equals(t.getEstado())).count();}catch(Exception e){venc=0;}
        statsLabel.setText(String.format("  Total: %d   |   Pendientes: %d   |   En Progreso: %d   |   Completadas: %d   |   Vencidas: %d",res.size(),pend,enp,comp,venc));
    }

    private boolean nv(String f,String q){return f!=null&&f.toLowerCase().contains(q);}
    private String nvl(String s){return s==null?"":s;}
    private int prioOrd(String p){
        return switch (p != null ? p : "") {
            case "Alta"  -> 0;
            case "Media" -> 1;
            default      -> 2;
        };
    }
    private Tarea getSelected(){
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Seleccione una tarea.");return null;}
        String tit=(String)tableModel.getValueAt(row,1);
        return allData.stream().filter(t->tit.equals(t.getTitulo())).findFirst().orElse(null);
    }

    private void editSelected(){Tarea t=getSelected();if(t!=null)openForm(t);}
    private void changeStatus(String st){Tarea t=getSelected();if(t==null)return;t.setEstado(st);try{dao.update(t);loadData();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}}
    private void deleteSelected(){
        Tarea t=getSelected();if(t==null)return;
        int ok=JOptionPane.showConfirmDialog(this,"Eliminar \""+t.getTitulo()+"\"?","Confirmar",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){try{dao.delete(t.getId());loadData();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}}
    }

    private void openForm(Tarea existing){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),existing==null?"Nueva Tarea":"Editar Tarea",true);
        d.setSize(520,420);d.setLocationRelativeTo(this);d.getContentPane().setBackground(UIConstants.BG_PANEL);d.setLayout(new BorderLayout());
        JPanel form=new JPanel(new GridBagLayout());form.setBackground(UIConstants.BG_PANEL);form.setBorder(BorderFactory.createEmptyBorder(20,24,10,24));
        GridBagConstraints gbc=new GridBagConstraints();gbc.fill=GridBagConstraints.HORIZONTAL;gbc.insets=new Insets(6,6,6,6);
        JTextField fTit=StyledComponents.styledTextField("Descripcion de la tarea");
        JComboBox<String> fPri=StyledComponents.styledCombo(UIConstants.PRIORIDADES);
        JComboBox<String> fEst=StyledComponents.styledCombo(UIConstants.ESTADOS_TAREA);
        JTextField fFecha=StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField fCat=StyledComponents.styledTextField("Correctivo, Deploy, Reunion...");
        JTextArea fDesc=StyledComponents.styledTextArea(5,20);
        if(existing!=null){fTit.setText(existing.getTitulo());sc(fPri,existing.getPrioridad());sc(fEst,existing.getEstado());fFecha.setText(existing.getFechaLimite());fCat.setText(existing.getCategoria());fDesc.setText(existing.getDescripcion());}
        int r=0;
        ar(form,gbc,r++,"Titulo *",fTit,"Categoria",fCat);
        ar(form,gbc,r++,"Prioridad",fPri,"Estado",fEst);
        ar(form,gbc,r++,"Fecha Limite",fFecha,null,null);
        af(form,gbc,r,"Descripcion",new JScrollPane(fDesc));
        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,10));bp.setBackground(UIConstants.BG_DARK);
        JButton bS=StyledComponents.successButton("Guardar");JButton bC=StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e->d.dispose());
        bS.addActionListener(e->{
            if(fTit.getText().trim().isEmpty()){JOptionPane.showMessageDialog(d,"Titulo obligatorio.");return;}
            Tarea t=existing!=null?existing:new Tarea();
            t.setTitulo(fTit.getText().trim());t.setPrioridad((String)fPri.getSelectedItem());t.setEstado((String)fEst.getSelectedItem());
            t.setFechaLimite(fFecha.getText().trim());t.setCategoria(fCat.getText().trim());t.setDescripcion(fDesc.getText().trim());
            try{if(existing==null)dao.insert(t);else dao.update(t);loadData();d.dispose();}
            catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        bp.add(bS);bp.add(bC);
        JScrollPane sp=new JScrollPane(form);sp.getViewport().setBackground(UIConstants.BG_PANEL);sp.setBorder(null);
        d.add(sp,BorderLayout.CENTER);d.add(bp,BorderLayout.SOUTH);d.setVisible(true);
    }
    private void ar(JPanel p,GridBagConstraints g,int row,String l1,Component c1,String l2,Component c2){
        g.gridy=row*2;g.gridx=0;g.gridwidth=1;g.weightx=0;p.add(fl(l1),g);g.gridx=1;g.weightx=.5;p.add(c1,g);
        if(l2!=null){g.gridx=2;g.weightx=0;p.add(fl(l2),g);g.gridx=3;g.weightx=.5;p.add(c2,g);}}
    private void af(JPanel p,GridBagConstraints g,int row,String lbl,Component c){
        g.gridy=row*2;g.gridx=0;g.gridwidth=4;g.weightx=1;p.add(fl(lbl),g);g.gridy=row*2+1;p.add(c,g);g.gridwidth=1;}
    private JLabel fl(String t){JLabel l=new JLabel(t);l.setFont(UIConstants.FONT_SMALL);l.setForeground(UIConstants.TEXT_SECONDARY);return l;}
    private void sc(JComboBox<String> cb,String v){if(v==null)return;for(int i=0;i<cb.getItemCount();i++)if(v.equals(cb.getItemAt(i))){cb.setSelectedIndex(i);return;}}
}
