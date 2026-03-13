package baustro.fin.ec.ui.panels;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.service.ApiService;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final List<Runnable> refreshTasks = new ArrayList<>();
    private JLabel lblLastUpdate;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_DARK);

        buildUI();
        startAutoRefresh();
    }

    private void buildUI() {

        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    //HEADER
    private JPanel buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);

        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,0,UIConstants.BORDER),
                new EmptyBorder(16,28,16,28)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        left.setOpaque(false);

        ImageIcon ico = IconManager.getIcon(IconManager.ICON_DASHBOARD,22);

        JLabel title = new JLabel("Dashboard");

        if(ico!=null) title.setIcon(ico);

        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        left.add(title);
        left.add(makePill("LIVE",UIConstants.ACCENT_GREEN));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);

        lblLastUpdate = new JLabel("Actualizando...");
        lblLastUpdate.setFont(UIConstants.FONT_SMALL);
        lblLastUpdate.setForeground(UIConstants.TEXT_MUTED);

        JButton btnRefresh = new JButton("Actualizar");

        btnRefresh.setBackground(UIConstants.BG_INPUT);
        btnRefresh.setForeground(UIConstants.TEXT_SECONDARY);
        btnRefresh.setFont(UIConstants.FONT_SMALL);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                new EmptyBorder(4,12,4,12)
        ));

        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e->refreshAll());

        right.add(lblLastUpdate);
        right.add(btnRefresh);

        header.add(left,BorderLayout.WEST);
        header.add(right,BorderLayout.EAST);

        return header;
    }

    //CONTENT
    private JPanel buildContent() {

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIConstants.BG_DARK);
        wrapper.setBorder(new EmptyBorder(32,40,40,40));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setOpaque(false);

        //RESUMEN
        content.add(section("TAREAS & RECURSOS"));
        content.add(Box.createVerticalStrut(12));

        JPanel gridLocal = createGrid(4);

        JLabel lblTotal = new JLabel("···");
        gridLocal.add(buildCard("Correctivos","Total registrados",UIConstants.ACCENT_BLUE,lblTotal));

        Runnable t1 = ()->{
            int v = queryInt("SELECT COUNT(*) FROM correctivos");
            SwingUtilities.invokeLater(()->lblTotal.setText(String.valueOf(v)));
        };

        refreshTasks.add(t1);
        t1.run();

        JLabel lblOpen = new JLabel("···");

        gridLocal.add(buildCard("Abiertos","Pendientes",UIConstants.ACCENT_RED,lblOpen));

        Runnable t2 = ()->{
            int v=queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Abierto'");
            SwingUtilities.invokeLater(()->lblOpen.setText(String.valueOf(v)));
        };

        refreshTasks.add(t2);
        t2.run();

        JLabel lblProgress=new JLabel("···");

        gridLocal.add(buildCard("En Progreso","Atención actual",UIConstants.ACCENT_ORANGE,lblProgress));

        Runnable t3=()->{
            int v=queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='En Progreso'");
            SwingUtilities.invokeLater(()->lblProgress.setText(String.valueOf(v)));
        };

        refreshTasks.add(t3);
        t3.run();

        JLabel lblDone=new JLabel("···");

        gridLocal.add(buildCard("Resueltos Hoy","Cerrados hoy",UIConstants.ACCENT_GREEN,lblDone));

        Runnable t4=()->{
            String hoy=LocalDate.now().toString();
            int v=queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Resuelto' AND fecha_solucion LIKE '%"+hoy+"%'");
            SwingUtilities.invokeLater(()->lblDone.setText(String.valueOf(v)));
        };

        refreshTasks.add(t4);
        t4.run();

        content.add(gridLocal);
        
        //TAREAS
        content.add(Box.createVerticalStrut(12));

        JPanel gridTareas=createGrid(4);

        JLabel lblTareas=new JLabel("···");

        gridTareas.add(buildCard("Tareas Pendientes","Sin iniciar",UIConstants.ACCENT_ORANGE,lblTareas));

        Runnable t5=()->{
            int v=queryInt("SELECT COUNT(*) FROM tareas WHERE estado='Pendiente'");
            SwingUtilities.invokeLater(()->lblTareas.setText(String.valueOf(v)));
        };

        refreshTasks.add(t5);
        t5.run();

        JLabel lblHoy=new JLabel("···");

        gridTareas.add(buildCard("Vencen Hoy","Fecha límite",UIConstants.ACCENT_RED,lblHoy));

        Runnable t6=()->{
            String hoy=LocalDate.now().toString();
            int v=queryInt("SELECT COUNT(*) FROM tareas WHERE fecha_limite='"+hoy+"' AND estado!='Completada'");
            SwingUtilities.invokeLater(()->lblHoy.setText(String.valueOf(v)));
        };

        refreshTasks.add(t6);
        t6.run();

        JLabel lblServ=new JLabel("···");

        gridTareas.add(buildCard("Servidores","Activos",UIConstants.ACCENT_CYAN,lblServ));

        Runnable t7=()->{
            int v=queryInt("SELECT COUNT(*) FROM servidores WHERE estado='Activo'");
            SwingUtilities.invokeLater(()->lblServ.setText(String.valueOf(v)));
        };

        refreshTasks.add(t7);
        t7.run();

        JLabel lblPass=new JLabel("···");

        gridTareas.add(buildCard("Credenciales","Guardadas",UIConstants.ACCENT_PURPLE,lblPass));

        Runnable t8=()->{
            int v=queryInt("SELECT COUNT(*) FROM contrasenas");
            SwingUtilities.invokeLater(()->lblPass.setText(String.valueOf(v)));
        };

        refreshTasks.add(t8);
        t8.run();

        content.add(gridTareas);

        content.add(Box.createVerticalStrut(28));

        //API
        content.add(section("MONITOREO API"));
        content.add(Box.createVerticalStrut(12));

        JPanel gridApi=createGrid(4);

        Object[][] apiCards={
                {"Cajas Abiertas","/api/cajas/abiertas",UIConstants.ACCENT_GREEN},
                {"Cajas Cerradas","/api/cajas/cerradas",UIConstants.ACCENT_ORANGE},
                {"Usuarios Activos","/api/usuarios/activos",UIConstants.ACCENT_PURPLE},
                {"Transacciones Hoy","/api/transacciones/hoy",UIConstants.ACCENT_CYAN},
                {"Errores API","/api/errores/24h",UIConstants.ACCENT_RED},
                {"Alertas","/api/alertas/pendientes",UIConstants.ACCENT_ORANGE}
        };

        for(Object[] def:apiCards){

            JLabel value=new JLabel("···");

            gridApi.add(buildCard((String)def[0],"API",(Color)def[2],value));

            String endpoint=(String)def[1];

            Runnable task=()->ApiService.fetchCount(endpoint,value);

            refreshTasks.add(task);
            task.run();
        }

        content.add(gridApi);

        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.weightx=1;
        gbc.anchor=GridBagConstraints.NORTH;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        wrapper.add(content,gbc);

        return wrapper;
    }

    //GRID
    private JPanel createGrid(int columns){

        JPanel grid=new JPanel(new GridLayout(0,columns,16,16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        return grid;
    }

    //CARD
    private JPanel buildCard(String title,String subtitle,Color accent,JLabel value){

        var card = getJPanel(accent);

        JPanel text=new JPanel();
        text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.setBorder(new EmptyBorder(14,16,12,14));

        JLabel t=new JLabel(title);
        t.setFont(UIConstants.FONT_HEADING);
        t.setForeground(UIConstants.TEXT_SECONDARY);

        value.setFont(new Font("Segoe UI",Font.BOLD,30));
        value.setForeground(accent);

        JLabel sub=new JLabel(subtitle);
        sub.setFont(UIConstants.FONT_SMALL);
        sub.setForeground(UIConstants.TEXT_MUTED);

        text.add(t);
        text.add(Box.createVerticalStrut(4));
        text.add(value);
        text.add(sub);

        card.add(text,BorderLayout.CENTER);

        return card;
    }

    private JPanel getJPanel(Color accent) {
        JPanel card=new JPanel(){

            protected void paintComponent(Graphics g){

                Graphics2D g2=(Graphics2D)g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(UIConstants.BG_CARD);

                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));

                g2.setColor(UIConstants.BORDER);

                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,12,12));

                g2.setColor(accent);

                g2.fillRoundRect(0,0,4,getHeight(),4,4);
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(0,110));
        card.setOpaque(false);
        return card;
    }

    //SECTION HEADER
    private JComponent section(String text) {

        JLabel lbl = new JLabel(text);

        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UIConstants.TEXT_SECONDARY);

        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(8, 2, 6, 2));

        return lbl;
    }

    //PILL
    private JLabel makePill(String text,Color accent){

        JLabel pill=new JLabel(text);

        pill.setFont(new Font("Segoe UI",Font.BOLD,10));
        pill.setForeground(accent);
        pill.setBorder(new EmptyBorder(2,8,2,8));

        pill.setOpaque(true);
        pill.setBackground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),30));

        return pill;
    }

    //REFRESH
    private void refreshAll(){

        refreshTasks.forEach(Runnable::run);

        SwingUtilities.invokeLater(()-> lblLastUpdate.setText("Actualizado: "+
                new SimpleDateFormat("HH:mm:ss").format(new Date())));
    }

    private void startAutoRefresh(){

        Timer t=new Timer(30000,e->refreshAll());

        t.setInitialDelay(1000);
        t.start();
    }

    //DB
    private int queryInt(String sql){

        try(
                Statement s= DatabaseManager.getInstance().getConnection().createStatement();
                ResultSet rs=s.executeQuery(sql)
        ){
            return rs.next()?rs.getInt(1):0;
        }
        catch(Exception e){
            return 0;
        }
    }
}
