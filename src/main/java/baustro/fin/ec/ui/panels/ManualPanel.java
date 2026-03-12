package baustro.fin.ec.ui.panels;

import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.HeaderSearchFilter;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class ManualPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private HeaderSearchFilter hsf;

    private final List<File> allFiles = new ArrayList<>();

    private static final String MANUAL_PATH =
            "D:/USERS/" + System.getProperty("user.name") + "/Documents/Manuales";

    public ManualPanel() {

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        buildUI();
        loadFiles();
    }

    private void buildUI() {

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10,20,10,20)));

        JLabel title = new JLabel("Manuales");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        ImageIcon ico = IconManager.getIcon(IconManager.ICON_MANUAL,22);
        if(ico!=null){
            title.setIcon(ico);
            title.setIconTextGap(8);
        }

        header.add(title,BorderLayout.WEST);


        // FILTROS ARRIBA
        hsf = new HeaderSearchFilter(
                "Buscar manual...",
                new HeaderSearchFilter.ComboConfig(
                        "Tipo",
                        new String[]{"PDF","DOCX","XLSX","PPTX","TXT"},
                        "Todos"
                ),
                new HeaderSearchFilter.ComboConfig(
                        "Ordenar",
                        new String[]{"Fecha","Tamaño"},
                        "Nombre A-Z"
                )
        ).onChanged(this::applyFilters);

        JPanel filters = new JPanel(new BorderLayout());
        filters.setBackground(UIConstants.BG_PANEL);
        filters.setBorder(BorderFactory.createEmptyBorder(6,20,6,20));
        filters.add(hsf,BorderLayout.CENTER);


        // TABLA
        String[] cols = {"Nombre","Tipo","Tamaño","Fecha"};

        model = new DefaultTableModel(cols,0){
            public boolean isCellEditable(int r,int c){
                return false;
            }
        };

        table = new JTable(model);
        StyledComponents.styleTable(table);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    openSelected();
                }
            }
        });

        JScrollPane scroll = StyledComponents.darkScrollPane(table);


        // BOTONES ABAJO
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        bottom.setBackground(UIConstants.BG_DARK);

        JButton btnOpen = StyledComponents.primaryButton("Abrir");
        JButton btnRefresh = StyledComponents.editButton("Actualizar");

        btnOpen.addActionListener(e -> openSelected());
        btnRefresh.addActionListener(e -> loadFiles());

        bottom.add(btnOpen);
        bottom.add(btnRefresh);


        // PANEL CENTRAL
        JPanel center = new JPanel(new BorderLayout());
        center.add(filters,BorderLayout.NORTH);
        center.add(scroll,BorderLayout.CENTER);

        add(header,BorderLayout.NORTH);
        add(center,BorderLayout.CENTER);
        add(bottom,BorderLayout.SOUTH);
    }

    private void loadFiles(){

        model.setRowCount(0);
        allFiles.clear();

        File dir = new File(MANUAL_PATH);

        System.out.println("Leyendo carpeta: " + dir.getAbsolutePath());

        if(!dir.exists()){
            JOptionPane.showMessageDialog(this,
                    "La carpeta de manuales no existe:\n" + dir.getAbsolutePath());
            return;
        }

        File[] files = dir.listFiles();

        if(files == null){
            System.out.println("No se pudieron leer archivos.");
            return;
        }

        System.out.println("Archivos encontrados: " + files.length);

        for(File f : files){

            if(f.isFile()){

                allFiles.add(f); // necesario para filtros

                String nombre = f.getName();
                String tipo = getExtension(nombre);
                long sizeKB = f.length()/1024;

                model.addRow(new Object[]{
                        nombre,
                        tipo,
                        sizeKB + " KB",
                        new java.util.Date(f.lastModified())
                });
            }
        }
    }

    private void applyFilters(){

        model.setRowCount(0);

        String q = hsf.getQuery().toLowerCase().trim();
        String tipo = hsf.getFilter(0);
        String sort = hsf.getFilter(1);

        Stream<File> stream = allFiles.stream();

        // BÚSQUEDA
        if(!q.isEmpty()){
            stream = stream.filter(f ->
                    f.getName().toLowerCase().contains(q)
            );
        }

        // FILTRO TIPO
        if(tipo != null && !tipo.equalsIgnoreCase("Todos") && !tipo.isBlank()){
            stream = stream.filter(f ->
                    getExtension(f.getName()).equalsIgnoreCase(tipo)
            );
        }

        // ORDENAMIENTO
        Comparator<File> cmp;

        if("Fecha".equals(sort)){
            cmp = Comparator.comparing(File::lastModified).reversed();
        }
        else if("Tamaño".equals(sort)){
            cmp = Comparator.comparing(File::length).reversed();
        }
        else{
            cmp = Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        }

        List<File> res = stream.sorted(cmp).toList();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for(File f : res){

            model.addRow(new Object[]{
                    f.getName(),
                    getExtension(f.getName()),
                    (f.length()/1024) + " KB",
                    df.format(new Date(f.lastModified()))
            });
        }
    }

    private void openSelected(){

        int row = table.getSelectedRow();

        if(row < 0){
            JOptionPane.showMessageDialog(this,"Seleccione un manual.");
            return;
        }

        String name = table.getValueAt(row,0).toString();

        File file = new File(MANUAL_PATH + "/" + name);

        try{

            Desktop.getDesktop().open(file);

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el archivo.");
        }
    }

    private String getExtension(String name){

        int i = name.lastIndexOf(".");

        if(i > 0)
            return name.substring(i+1).toUpperCase();

        return "";
    }
}