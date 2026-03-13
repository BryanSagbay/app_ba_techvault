package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.CasoDAO;
import baustro.fin.ec.model.Caso;
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

public class CasoPanel extends JPanel {

    private final CasoDAO dao = new CasoDAO();

    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;

    private List<Caso> allData = new ArrayList<>();

    public CasoPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);
        buildUI();
        loadData();
    }

    private void buildUI() {

        // HEADER
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);

        JLabel title = new JLabel("Casos");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        ImageIcon ico = IconManager.getIcon(IconManager.ICON_CORRECTIVO, 22);
        if (ico != null && ico.getIconWidth() > 1) {
            title.setIcon(ico);
            title.setIconTextGap(8);
        }

        titlePane.add(title);

        // BOTONES
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        headerButtons.setOpaque(false);

        JButton btnNew = StyledComponents.addButton("Nuevo");
        JButton btnEdit = StyledComponents.editButton("Editar");
        JButton btnView = StyledComponents.primaryButton("Ver Detalle", UIConstants.ACCENT_PURPLE);
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");

        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnView.addActionListener(e -> viewSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        headerButtons.add(btnNew);
        headerButtons.add(btnEdit);
        headerButtons.add(btnView);
        headerButtons.add(btnDelete);

        header.add(titlePane, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        // BUSCADOR
        hsf = new HeaderSearchFilter(
                "Buscar N tarea, servicio, error...",
                new HeaderSearchFilter.ComboConfig("Estado", UIConstants.ESTADOS_CORRECTIVO, "Todos"),
                new HeaderSearchFilter.ComboConfig("Prioridad", UIConstants.PRIORIDADES, "Todas"),
                new HeaderSearchFilter.ComboConfig("Ambiente", UIConstants.AMBIENTES, "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar", new String[]{
                       "Fecha antigua","Prioridad Alta","Estado","N Tarea","Servicio"},
                        "Fecha reciente")
        ).onChanged(this::applyFilters);


        // STATS BAR
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(new Color(20, 26, 38));
        statsBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0, UIConstants.BORDER));

        statsLabel = new JLabel("Cargando...");
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);

        statsBar.add(statsLabel);


        // TABLA
        String[] cols = {"#","N Tarea","Titulo","Servicio","Ambiente","Prioridad","Estado","Fecha"};

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        StyledComponents.styleTable(table);

        int[] widths = {40,110,250,130,90,80,90,100};

        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(0).setMaxWidth(40);

        table.getColumnModel().getColumn(5).setCellRenderer(colorRenderer("prioridad"));
        table.getColumnModel().getColumn(6).setCellRenderer(colorRenderer("estado"));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2) editSelected();
            }
        });


        // BOTTOM - BUSCADOR
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_DARK);
        bottom.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));

        bottom.add(hsf, BorderLayout.CENTER);


        // CENTER
        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }


    private DefaultTableCellRenderer colorRenderer(String type) {

        return new DefaultTableCellRenderer() {

            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {

                super.getTableCellRendererComponent(t,val,sel,focus,row,col);

                String s = val!=null?val.toString():"";

                setBackground(sel
                        ? UIConstants.ACCENT_BLUE
                        : (row%2==0 ? UIConstants.BG_PANEL : UIConstants.TABLE_ROW_ALT));

                setForeground(type.equals("prioridad")
                        ? UIConstants.getPrioridadColor(s)
                        : UIConstants.getEstadoColor(s));

                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));

                setBorder(BorderFactory.createEmptyBorder(0,8,0,8));

                return this;
            }
        };
    }


    private void loadData() {

        try {
            allData = dao.findAll();
            applyFilters();
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());
        }
    }


    private void applyFilters() {

        String q = hsf.getQuery().toLowerCase();

        String estado = hsf.getFilter(0);
        String prio = hsf.getFilter(1);
        String amb = hsf.getFilter(2);
        String sort = hsf.getFilter(3);

        Stream<Caso> s = allData.stream();

        if (!q.isEmpty()) s = s.filter(c -> matches(c, q));
        if (!estado.isEmpty()) s = s.filter(c -> estado.equals(c.getEstado()));
        if (!prio.isEmpty()) s = s.filter(c -> prio.equals(c.getPrioridad()));
        if (!amb.isEmpty()) s = s.filter(c -> amb.equals(c.getAmbiente()));

        Comparator<Caso> cmp = switch (sort) {

            case "Fecha antigua" ->
                    Comparator.comparing(c -> nvl(c.getFechaReporte()));

            case "Prioridad Alta" ->
                    Comparator.comparingInt(c -> prioOrd(c.getPrioridad()));

            case "Estado" ->
                    Comparator.comparing(c -> nvl(c.getEstado()));

            case "N Tarea" ->
                    Comparator.comparing(c -> nvl(c.getNumeroTarea()));

            case "Servicio" ->
                    Comparator.comparing(c -> nvl(c.getServicio()));

            default ->
                    Comparator.comparing((Caso c) -> nvl(c.getFechaReporte())).reversed();
        };

        List<Caso> result = s.sorted(cmp).toList();

        refreshTable(result);
        updateStats(result);
    }


    private boolean matches(Caso c, String q) {

        return nv(c.getNumeroTarea(),q)
                || nv(c.getTitulo(),q)
                || nv(c.getServicio(),q)
                || nv(c.getErrorPresentado(),q)
                || nv(c.getSolucion(),q)
                || nv(c.getAmbiente(),q);
    }

    private boolean nv(String f, String q) {
        return f!=null && f.toLowerCase().contains(q);
    }

    private String nvl(String s) {
        return s==null?"":s;
    }

    private int prioOrd(String p){
        return switch (p != null ? p : "") {
            case "Alta" -> 0;
            case "Media" -> 1;
            default -> 2;
        };
    }


    private void refreshTable(List<Caso> data) {

        tableModel.setRowCount(0);

        int i=1;

        for (Caso c : data)

            tableModel.addRow(new Object[]{
                    i++,
                    c.getNumeroTarea(),
                    c.getTitulo(),
                    c.getServicio(),
                    c.getAmbiente(),
                    c.getPrioridad(),
                    c.getEstado(),
                    c.getFechaReporte()
            });
    }


    private void updateStats(List<Caso> data) {

        long ab = data.stream().filter(c->"Abierto".equals(c.getEstado())).count();
        long ep = data.stream().filter(c->"En Progreso".equals(c.getEstado())).count();
        long res = data.stream().filter(c->"Resuelto".equals(c.getEstado())).count();
        long alt = data.stream().filter(c->"Alta".equals(c.getPrioridad())).count();

        statsLabel.setText(String.format(
                " Total: %d | Abiertos: %d | En Progreso: %d | Resueltos: %d | Prioridad Alta: %d",
                data.size(), ab, ep, res, alt));
    }


    private Caso getSelected() {

        int row = table.getSelectedRow();

        if (row<0) {

            JOptionPane.showMessageDialog(this,"Seleccione un registro.");
            return null;
        }

        String num = (String) tableModel.getValueAt(row,1);

        return allData.stream()
                .filter(c->num.equals(c.getNumeroTarea()))
                .findFirst()
                .orElse(null);
    }


    private void editSelected() {
        Caso c=getSelected();
        if(c!=null) openForm(c);
    }

    private void viewSelected() {
        Caso c=getSelected();
        if(c!=null) showDetail(c);
    }


    private void deleteSelected() {

        Caso c=getSelected();

        if(c==null) return;

        int ok=JOptionPane.showConfirmDialog(
                this,
                "Eliminar ["+c.getNumeroTarea()+"] "+c.getTitulo()+"?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if(ok==JOptionPane.YES_OPTION){

            try{
                dao.delete(c.getId());
                loadData();
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());
            }
        }
    }

    private void showDetail(Caso c) {
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Detalle: "+c.getNumeroTarea(),true);
        d.setSize(680,560); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_PANEL);
        JTextArea ta = StyledComponents.monoTextArea(10,60);
        ta.setEditable(false); ta.setBackground(UIConstants.BG_DARK);
        ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        String sep="======================================================\n";
        ta.setText(sep+"CORRECTIVO: "+c.getNumeroTarea()+"\n"+sep
            +"Titulo       : "+nvl(c.getTitulo())+"\n"
            +"Servicio     : "+nvl(c.getServicio())+"\n"
            +"Ambiente     : "+nvl(c.getAmbiente())+"\n"
            +"Estado       : "+nvl(c.getEstado())+"\n"
            +"Prioridad    : "+nvl(c.getPrioridad())+"\n"
            +"Responsable  : "+nvl(c.getResponsable())+"\n"
            +"Fecha Reporte: "+nvl(c.getFechaReporte())+"\n"
            +"Fecha Solución: "+nvl(c.getFechaSolucion())+"\n\n"
            +"-- ERROR PRESENTADO\n"+sep+nvl(c.getErrorPresentado())+"\n\n"
            +"-- SOLUCIÓN APLICADA\n"+sep+nvl(c.getSolucion())+"\n\n"
            +"-- OBSERVACIONES\n"+sep+nvl(c.getObservaciones())+"\n"+sep);
        d.add(StyledComponents.darkScrollPane(ta)); d.setVisible(true);
    }

    private void openForm(Caso existing) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),
                existing==null?"Nuevo Casos":"Editar Caso",true);
        dialog.setSize(720,660); dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(20,24,10,24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(5,5,5,5);

        JTextField fNum   = StyledComponents.styledTextField("Ej: INC-2024-001");
        JTextField fTit   = StyledComponents.styledTextField("Descripción breve");
        JTextArea  fDesc  = StyledComponents.styledTextArea(3,20);
        JComboBox<String> fAmb = StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JTextField fSvc   = StyledComponents.styledTextField("Ej: Servicio de pagos");
        JTextArea  fErr   = StyledComponents.styledTextArea(4,20);
        JTextArea  fSol   = StyledComponents.styledTextArea(4,20);
        JComboBox<String> fEst = StyledComponents.styledCombo(UIConstants.ESTADOS_CORRECTIVO);
        JComboBox<String> fPri = StyledComponents.styledCombo(UIConstants.PRIORIDADES);
        JTextField fFR    = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField fFS    = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField fResp  = StyledComponents.styledTextField("Responsable");
        JTextArea  fObs   = StyledComponents.styledTextArea(3,20);

        if (existing!=null) {
            fNum.setText(existing.getNumeroTarea()); fTit.setText(existing.getTitulo());
            fDesc.setText(existing.getDescripcion()); setCombo(fAmb,existing.getAmbiente());
            fSvc.setText(existing.getServicio()); fErr.setText(existing.getErrorPresentado());
            fSol.setText(existing.getSolucion()); setCombo(fEst,existing.getEstado());
            setCombo(fPri,existing.getPrioridad()); fFR.setText(existing.getFechaReporte());
            fFS.setText(existing.getFechaSolucion()); fResp.setText(existing.getResponsable());
            fObs.setText(existing.getObservaciones());
        } else { fFR.setText(LocalDate.now().toString()); }

        int r=0;
        addRow(form,gbc,r++,"N Tarea *",fNum,"Titulo *",fTit);
        addRow(form,gbc,r++,"Ambiente",fAmb,"Servicio",fSvc);
        addRow(form,gbc,r++,"Estado",fEst,"Prioridad",fPri);
        addRow(form,gbc,r++,"Fecha Reporte",fFR,"Fecha Solución",fFS);
        addRow(form,gbc,r++,"Responsable",fResp,null,null);
        addFull(form,gbc,r++,"Descripción",new JScrollPane(fDesc));
        addFull(form,gbc,r++,"Error presentado",new JScrollPane(fErr));
        addFull(form,gbc,r++,"Solución aplicada",new JScrollPane(fSol));
        addFull(form,gbc,r,"Observaciones",new JScrollPane(fObs));

        JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,10));
        btnPanel.setBackground(UIConstants.BG_DARK);
        JButton btnSave=StyledComponents.successButton("Guardar");
        JButton btnCancel=StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e->dialog.dispose());
        btnSave.addActionListener(e->{
            if(fNum.getText().trim().isEmpty()||fTit.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(dialog,"Numero y titulo son obligatorios."); return;}
            Caso c=existing!=null?existing:new Caso();
            c.setNumeroTarea(fNum.getText().trim()); c.setTitulo(fTit.getText().trim());
            c.setDescripcion(fDesc.getText().trim()); c.setAmbiente((String)fAmb.getSelectedItem());
            c.setServicio(fSvc.getText().trim()); c.setErrorPresentado(fErr.getText().trim());
            c.setSolucion(fSol.getText().trim()); c.setEstado((String)fEst.getSelectedItem());
            c.setPrioridad((String)fPri.getSelectedItem()); c.setFechaReporte(fFR.getText().trim());
            c.setFechaSolucion(fFS.getText().trim()); c.setResponsable(fResp.getText().trim());
            c.setObservaciones(fObs.getText().trim());
            try{if(existing==null)dao.insert(c);else dao.update(c);loadData();dialog.dispose();}
            catch(Exception ex){JOptionPane.showMessageDialog(dialog,"Error: "+ex.getMessage());}
        });
        btnPanel.add(btnSave); btnPanel.add(btnCancel);
        JScrollPane sp=new JScrollPane(form); sp.getViewport().setBackground(UIConstants.BG_PANEL); sp.setBorder(null);
        dialog.add(sp,BorderLayout.CENTER); dialog.add(btnPanel,BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow(JPanel p,GridBagConstraints g,int row,String l1,Component c1,String l2,Component c2){
        g.gridy=row*2;g.gridx=0;g.gridwidth=1;g.weightx=0;p.add(fl(l1),g);
        g.gridx=1;g.weightx=.5;p.add(c1,g);
        if(l2!=null){g.gridx=2;g.weightx=0;p.add(fl(l2),g);g.gridx=3;g.weightx=.5;p.add(c2,g);}
    }
    private void addFull(JPanel p,GridBagConstraints g,int row,String label,Component c){
        g.gridy=row*2;g.gridx=0;g.gridwidth=4;g.weightx=1;p.add(fl(label),g);
        g.gridy=row*2+1;p.add(c,g);g.gridwidth=1;
    }
    private JLabel fl(String t){JLabel l=new JLabel(t);l.setFont(UIConstants.FONT_SMALL);l.setForeground(UIConstants.TEXT_SECONDARY);return l;}
    private void setCombo(JComboBox<String> cb,String v){
        if(v==null)return;for(int i=0;i<cb.getItemCount();i++)if(v.equals(cb.getItemAt(i))){cb.setSelectedIndex(i);return;}
    }
}
