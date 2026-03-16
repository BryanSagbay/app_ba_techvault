package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ComandoDAO;
import baustro.fin.ec.model.Comando;
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

public class ComandoPanel extends JPanel {

    private final ComandoDAO dao = new ComandoDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;
    private JTextArea previewArea;
    private List<Comando> allData = new ArrayList<>();
    public static final String[] CATEGORIAS_COMANDO = {"Red","Logs","Deploy","Base de Datos","Sistema","Docker","Git","Seguridad","Monitoreo","Otro"};

    public ComandoPanel() {
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
        JLabel title = new JLabel("Comandos");
        title.setFont(UIConstants.FONT_TITLE); title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico=IconManager.getIcon(IconManager.ICON_COMANDO,22);
        if(ico!=null&&ico.getIconWidth()>1){title.setIcon(ico);title.setIconTextGap(8);}
        titlePane.add(title);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        headerButtons.setOpaque(false);
        JButton btnNew    = StyledComponents.addButton("Agregar");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");
        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        headerButtons.add(btnNew); headerButtons.add(btnEdit); headerButtons.add(btnDelete);

        header.add(titlePane,     BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        //  STATS BAR
        JPanel statsBar=new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER));
        statsLabel=new JLabel(); statsLabel.setFont(UIConstants.FONT_SMALL); statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        //  TABLA
        String[] cols={"#","Titulo","Categoría","SO","Descripción"};
        tableModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); StyledComponents.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(280);
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())showPreview();});
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)editSelected();}});

        //  PREVIEW TERMINAL
        JPanel previewPanel=new JPanel(new BorderLayout());
        previewPanel.setBackground(UIConstants.BG_BASE);
        JPanel previewHeader=new JPanel(new BorderLayout(8,0));
        previewHeader.setBackground(UIConstants.BG_CARD);
        previewHeader.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        JLabel pLbl=new JLabel("Comando:"); pLbl.setFont(UIConstants.FONT_SMALL); pLbl.setForeground(UIConstants.TEXT_MUTED);
        JButton btnCopy=StyledComponents.copyButton("Copiar Comando");
        btnCopy.addActionListener(e->copyCommand());
        previewHeader.add(pLbl,BorderLayout.WEST); previewHeader.add(btnCopy,BorderLayout.EAST);
        previewArea=StyledComponents.monoTextArea(4,40);
        previewArea.setEditable(false); previewArea.setBackground(UIConstants.BG_SURFACE);
        previewArea.setForeground(new Color(80,220,120)); previewArea.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        previewPanel.add(previewHeader,BorderLayout.NORTH);
        previewPanel.add(StyledComponents.darkScrollPane(previewArea),BorderLayout.CENTER);

        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(300); split.setDividerSize(4); split.setBackground(UIConstants.BG_BASE);
        JPanel topArea=new JPanel(new BorderLayout());
        topArea.add(statsBar,BorderLayout.NORTH);
        topArea.add(StyledComponents.darkScrollPane(table),BorderLayout.CENTER);
        split.setTopComponent(topArea);
        split.setBottomComponent(previewPanel);

        //  BOTTOM: filtros
        hsf = new HeaderSearchFilter(
                "Buscar título, comando, descripción...",
                new HeaderSearchFilter.ComboConfig("SO",        UIConstants.SISTEMAS_OPERATIVOS_CMD, "Todos"),
                new HeaderSearchFilter.ComboConfig("Categoría", CATEGORIAS_COMANDO,      "Todas"),
                new HeaderSearchFilter.ComboConfig("Ordenar",
                        new String[]{"Título Z-A","Categoría A-Z","Categoría Z-A","SO"},
                        "Título A-Z")
        ).onChanged(this::applyFilters);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        bottom.add(hsf, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(split,  BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData(){
        try{allData=dao.findAll();applyFilters();}
        catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}
    }

    private void applyFilters(){
        String q    = hsf.getQuery().toLowerCase();
        String so   = hsf.getFilter(0);  // índice 0 → SO
        String cat  = hsf.getFilter(1);  // índice 1 → Categoría
        String sort = hsf.getFilter(2);  // índice 2 → Ordenar

        Stream<Comando> s = allData.stream();
        if (!q.isEmpty())   s = s.filter(c -> nv(c.getTitulo(),q) || nv(c.getComando(),q) || nv(c.getDescripcion(),q) || nv(c.getCategoria(),q));
        if (!so.isEmpty())  s = s.filter(c -> so.equals(c.getSistemaOperativo()));
        if (!cat.isEmpty()) s = s.filter(c -> cat.equals(c.getCategoria()));

        Comparator<Comando> cmp = switch (sort) {
            case "Título Z-A"    -> Comparator.comparing((Comando c) -> nvl(c.getTitulo())).reversed();
            case "Categoría A-Z" -> Comparator.comparing(c -> nvl(c.getCategoria()));
            case "Categoría Z-A" -> Comparator.comparing((Comando c) -> nvl(c.getCategoria())).reversed();
            case "SO"            -> Comparator.comparing(c -> nvl(c.getSistemaOperativo()));
            default              -> Comparator.comparing(c -> nvl(c.getTitulo())); // "Título A-Z"
        };

        List<Comando> res = s.sorted(cmp).toList();
        tableModel.setRowCount(0);
        int i=1; for(Comando c:res)
            tableModel.addRow(new Object[]{i++,c.getTitulo(),c.getCategoria(),c.getSistemaOperativo(),c.getDescripcion()});
        Map<String,Long> byCat=res.stream().collect(java.util.stream.Collectors.groupingBy(c->nvl(c.getCategoria()),java.util.stream.Collectors.counting()));
        String catStr=byCat.entrySet().stream().filter(e->!e.getKey().isEmpty()).map(e->e.getKey()+": "+e.getValue()).reduce((a,b)->a+" | "+b).orElse("");
        statsLabel.setText("  Total: "+res.size()+(catStr.isEmpty()?"":" — "+catStr));
    }

    private boolean nv(String f,String q){return f!=null&&f.toLowerCase().contains(q);}
    private String nvl(String s){return s==null?"":s;}

    private void showPreview(){
        Comando c=getSelected(false); if(c!=null) previewArea.setText(c.getComando());
    }
    private void copyCommand(){
        Comando c=getSelected(true); if(c==null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(c.getComando()),null);
        JOptionPane.showMessageDialog(this,"Comando copiado","Copiado",JOptionPane.INFORMATION_MESSAGE);
    }

    private Comando getSelected(boolean msg){
        int row=table.getSelectedRow();
        if(row<0){if(msg)JOptionPane.showMessageDialog(this,"Seleccione un comando.");return null;}
        String tit=(String)tableModel.getValueAt(row,1);
        return allData.stream().filter(c->tit.equals(c.getTitulo())).findFirst().orElse(null);
    }

    private void editSelected(){Comando c=getSelected(true);if(c!=null)openForm(c);}
    private void deleteSelected(){
        Comando c=getSelected(true); if(c==null) return;
        int ok=JOptionPane.showConfirmDialog(this,"Eliminar \""+c.getTitulo()+"\"?","Confirmar",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){try{dao.delete(c.getId());loadData();previewArea.setText("");}catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());}}
    }

    private void openForm(Comando existing){
        JDialog d=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),existing==null?"Agregar Comando":"Editar Comando",true);
        d.setSize(560,430); d.setLocationRelativeTo(this); d.getContentPane().setBackground(UIConstants.BG_CARD); d.setLayout(new BorderLayout());
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(UIConstants.BG_CARD); form.setBorder(BorderFactory.createEmptyBorder(20,24,10,24));
        GridBagConstraints gbc=new GridBagConstraints(); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(6,6,6,6);

        JTextField fTit = StyledComponents.styledTextField("Nombre descriptivo");
        // Categoría ahora es ComboBox con la lista fija — igual que el filtro
        JComboBox<String> fCat = StyledComponents.styledCombo(CATEGORIAS_COMANDO);
        JComboBox<String> fSO  = StyledComponents.styledCombo(UIConstants.SISTEMAS_OPERATIVOS_CMD);
        JTextArea fCmd  = StyledComponents.monoTextArea(5,40);
        fCmd.setBackground(UIConstants.BG_SURFACE); fCmd.setForeground(new Color(80,220,120));
        JTextArea fDesc = StyledComponents.styledTextArea(3,40);

        if(existing!=null){
            fTit.setText(existing.getTitulo());
            sc(fCat, existing.getCategoria());
            sc(fSO,  existing.getSistemaOperativo());
            fCmd.setText(existing.getComando());
            fDesc.setText(existing.getDescripcion());
        }

        int r=0;
        ar(form,gbc,r++,"Título *",    fTit, "Categoría", fCat);
        ar(form,gbc,r++,"Sistema Operativo", fSO, null, null);
        af(form,gbc,r++,"Comando *",   new JScrollPane(fCmd));
        af(form,gbc,r,  "Descripción / Uso", new JScrollPane(fDesc));

        JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,10)); bp.setBackground(UIConstants.BG_BASE);
        JButton bS=StyledComponents.successButton("Guardar"); JButton bC=StyledComponents.cancelButton("Cancelar");
        bC.addActionListener(e->d.dispose());
        bS.addActionListener(e->{
            if(fTit.getText().trim().isEmpty()||fCmd.getText().trim().isEmpty()){JOptionPane.showMessageDialog(d,"Título y comando obligatorios.");return;}
            Comando c=existing!=null?existing:new Comando();
            c.setTitulo(fTit.getText().trim());
            c.setCategoria((String)fCat.getSelectedItem());
            c.setSistemaOperativo((String)fSO.getSelectedItem());
            c.setComando(fCmd.getText().trim());
            c.setDescripcion(fDesc.getText().trim());
            try{if(existing==null)dao.insert(c);else dao.update(c);loadData();d.dispose();}
            catch(Exception ex){JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage());}
        });
        bp.add(bS); bp.add(bC);
        JScrollPane sp=new JScrollPane(form); sp.getViewport().setBackground(UIConstants.BG_CARD); sp.setBorder(null);
        d.add(sp,BorderLayout.CENTER); d.add(bp,BorderLayout.SOUTH); d.setVisible(true);
    }

    private void ar(JPanel p,GridBagConstraints g,int row,String l1,Component c1,String l2,Component c2){
        g.gridy=row*2;g.gridx=0;g.gridwidth=1;g.weightx=0;p.add(fl(l1),g);g.gridx=1;g.weightx=.5;p.add(c1,g);
        if(l2!=null){g.gridx=2;g.weightx=0;p.add(fl(l2),g);g.gridx=3;g.weightx=.5;p.add(c2,g);}}
    private void af(JPanel p,GridBagConstraints g,int row,String lbl,Component c){
        g.gridy=row*2;g.gridx=0;g.gridwidth=4;g.weightx=1;p.add(fl(lbl),g);g.gridy=row*2+1;p.add(c,g);g.gridwidth=1;}
    private JLabel fl(String t){JLabel l=new JLabel(t);l.setFont(UIConstants.FONT_SMALL);l.setForeground(UIConstants.TEXT_SECONDARY);return l;}
    private void sc(JComboBox<String> cb,String v){if(v==null)return;for(int i=0;i<cb.getItemCount();i++)if(v.equals(cb.getItemAt(i))){cb.setSelectedIndex(i);return;}}
}