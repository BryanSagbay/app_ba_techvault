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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;
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

    // ── HEADER ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 28, 16, 28)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_DASHBOARD, 22);
        JLabel title = new JLabel("Dashboard");
        if (ico != null && ico.getIconWidth() > 1) title.setIcon(ico);
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        left.add(title);
        left.add(makePill("LIVE", UIConstants.ACCENT_GREEN));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        lblLastUpdate = new JLabel("Actualizando...");
        lblLastUpdate.setFont(UIConstants.FONT_SMALL);
        lblLastUpdate.setForeground(UIConstants.TEXT_MUTED);

        // Boton refrescar manual
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.setBackground(UIConstants.BG_INPUT);
        btnRefresh.setForeground(UIConstants.TEXT_SECONDARY);
        btnRefresh.setFont(UIConstants.FONT_SMALL);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshAll());

        right.add(lblLastUpdate);
        right.add(btnRefresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── CONTENT ───────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BG_DARK);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // ── SECCIÓN 1: Resumen interno ────────────────────────────
        content.add(sectionLabel("RESUMEN INTERNO"));
        content.add(Box.createVerticalStrut(10));

        JPanel gridLocal = new JPanel(new GridLayout(1, 4, 14, 14));
        gridLocal.setOpaque(false);
        gridLocal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Correctivos totales
        JLabel lblTotalCorrectivos = new JLabel("···");
        gridLocal.add(buildCard("Correctivos", "Total registrados", UIConstants.ACCENT_BLUE, Icons.CLIPBOARD, lblTotalCorrectivos));
        Runnable t1 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos");
            SwingUtilities.invokeLater(() -> lblTotalCorrectivos.setText(String.valueOf(v)));
        };
        refreshTasks.add(t1); t1.run();

        // Correctivos abiertos
        JLabel lblAbiertos = new JLabel("···");
        gridLocal.add(buildCard("Abiertos", "Pendientes de atencion", UIConstants.ACCENT_RED, Icons.ALERT, lblAbiertos));
        Runnable t2 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Abierto'");
            SwingUtilities.invokeLater(() -> lblAbiertos.setText(String.valueOf(v)));
        };
        refreshTasks.add(t2); t2.run();

        // Correctivos en progreso
        JLabel lblEnProgreso = new JLabel("···");
        gridLocal.add(buildCard("En Progreso", "En atencion ahora", UIConstants.ACCENT_ORANGE, Icons.TRANSFER, lblEnProgreso));
        Runnable t3 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='En Progreso'");
            SwingUtilities.invokeLater(() -> lblEnProgreso.setText(String.valueOf(v)));
        };
        refreshTasks.add(t3); t3.run();

        // Correctivos resueltos hoy
        JLabel lblResueltos = new JLabel("···");
        gridLocal.add(buildCard("Resueltos Hoy", "Cerrados en el dia", UIConstants.ACCENT_GREEN, Icons.DONE, lblResueltos));
        Runnable t4 = () -> {
            String hoy = LocalDate.now().toString();
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Resuelto' AND fecha_solucion LIKE '%" + hoy + "%'");
            SwingUtilities.invokeLater(() -> lblResueltos.setText(String.valueOf(v)));
        };
        refreshTasks.add(t4); t4.run();

        content.add(gridLocal);
        content.add(Box.createVerticalStrut(20));

        // ── SECCIÓN 2: Tareas y Recursos ──────────────────────────
        content.add(sectionLabel("TAREAS & RECURSOS"));
        content.add(Box.createVerticalStrut(10));

        JPanel gridTareas = new JPanel(new GridLayout(1, 4, 14, 14));
        gridTareas.setOpaque(false);
        gridTareas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel lblTareasPend = new JLabel("···");
        gridTareas.add(buildCard("Tareas Pendientes", "Sin iniciar", UIConstants.ACCENT_ORANGE, Icons.BELL, lblTareasPend));
        Runnable t5 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM tareas WHERE estado='Pendiente'");
            SwingUtilities.invokeLater(() -> lblTareasPend.setText(String.valueOf(v)));
        };
        refreshTasks.add(t5); t5.run();

        JLabel lblTareasHoy = new JLabel("···");
        gridTareas.add(buildCard("Vencen Hoy", "Fecha limite hoy", UIConstants.ACCENT_RED, Icons.ALERT, lblTareasHoy));
        Runnable t6 = () -> {
            String hoy = LocalDate.now().toString();
            int v = queryInt("SELECT COUNT(*) FROM tareas WHERE fecha_limite='" + hoy + "' AND estado != 'Completada'");
            SwingUtilities.invokeLater(() -> lblTareasHoy.setText(String.valueOf(v)));
        };
        refreshTasks.add(t6); t6.run();

        JLabel lblServidores = new JLabel("···");
        gridTareas.add(buildCard("Servidores Activos", "En inventario", UIConstants.ACCENT_CYAN, Icons.SERVER, lblServidores));
        Runnable t7 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM servidores WHERE estado='Activo'");
            SwingUtilities.invokeLater(() -> lblServidores.setText(String.valueOf(v)));
        };
        refreshTasks.add(t7); t7.run();

        JLabel lblContrasenas = new JLabel("···");
        gridTareas.add(buildCard("Contrasenas", "Credenciales guardadas", UIConstants.ACCENT_PURPLE, Icons.KEY, lblContrasenas));
        Runnable t8 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM contrasenas");
            SwingUtilities.invokeLater(() -> lblContrasenas.setText(String.valueOf(v)));
        };
        refreshTasks.add(t8); t8.run();

        content.add(gridTareas);
        content.add(Box.createVerticalStrut(20));

        // ── SECCIÓN 3: API Cajas ──────────────────────────────────
        content.add(sectionLabel("MONITOREO API — CAJAS Y TRANSACCIONES"));
        content.add(Box.createVerticalStrut(10));

        JPanel gridApi = new JPanel(new GridLayout(0, 4, 14, 14));
        gridApi.setOpaque(false);
        gridApi.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Cards API con endpoints reales
        Object[][] apiCards = {
            {"Cajas Abiertas",   "/api/cajas/abiertas",         "Turno actual",     UIConstants.ACCENT_GREEN,  Icons.BOX_OPEN},
            {"Cajas Cerradas",   "/api/cajas/cerradas",         "Turno actual",     UIConstants.ACCENT_ORANGE, Icons.BOX_CLOSED},
            {"Usuarios Activos", "/api/usuarios/activos",       "En sistema",       UIConstants.ACCENT_PURPLE, Icons.USERS},
            {"Transacciones",    "/api/transacciones/hoy",      "Procesadas hoy",   UIConstants.ACCENT_CYAN,   Icons.TRANSFER},
            {"Errores API",      "/api/errores/24h",            "Ultimas 24h",      UIConstants.ACCENT_RED,    Icons.ERROR},
            {"Alertas",          "/api/alertas/pendientes",     "Sin atender",      UIConstants.ACCENT_ORANGE, Icons.BELL},
            {"Lotes Pendientes", "/api/lotes/pendientes",       "Por procesar",     UIConstants.ACCENT_BLUE,   Icons.BOX_OPEN},
            {"Lotes Procesados", "/api/lotes/procesados/hoy",   "Completados hoy",  UIConstants.ACCENT_GREEN,  Icons.DONE},
        };

        for (Object[] def : apiCards) {
            JLabel valueLabel = new JLabel("···");
            gridApi.add(buildCard(
                    (String) def[0], (String) def[2], (Color) def[3], (String) def[4], valueLabel));
            String endpoint = (String) def[1];
            Runnable task = () -> ApiService.fetchCount(endpoint, valueLabel);
            refreshTasks.add(task);
            task.run();
        }

        content.add(gridApi);
        content.add(Box.createVerticalStrut(24));

        // ── SECCIÓN 4: Correctivos por prioridad (mini-table) ─────
        content.add(sectionLabel("CORRECTIVOS POR PRIORIDAD"));
        content.add(Box.createVerticalStrut(10));
        content.add(buildPrioridadBar());
        content.add(Box.createVerticalStrut(24));

        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    // ── PRIORIDAD BAR ─────────────────────────────────────────────
    private JPanel buildPrioridadBar() {
        JPanel p = new JPanel(new GridLayout(1, 3, 14, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String[][] prios = {
            {"Alta",  "ACCENT_RED"},
            {"Media", "ACCENT_ORANGE"},
            {"Baja",  "ACCENT_GREEN"},
        };

        for (String[] pr : prios) {
            String prio = pr[0];
            Color  col  = pr[1].equals("ACCENT_RED")    ? UIConstants.ACCENT_RED
                        : pr[1].equals("ACCENT_ORANGE") ? UIConstants.ACCENT_ORANGE
                                                        : UIConstants.ACCENT_GREEN;
            JPanel pill = new JPanel(new BorderLayout(8, 0));
            pill.setBackground(UIConstants.BG_CARD);
            pill.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(col.getRed(), col.getGreen(), col.getBlue(), 80)),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)));

            JLabel lblName = new JLabel("Prioridad " + prio);
            lblName.setFont(UIConstants.FONT_HEADING);
            lblName.setForeground(col);

            JLabel lblCount = new JLabel("···");
            lblCount.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblCount.setForeground(UIConstants.TEXT_PRIMARY);
            lblCount.setHorizontalAlignment(SwingConstants.RIGHT);

            pill.add(lblName, BorderLayout.WEST);
            pill.add(lblCount, BorderLayout.EAST);
            p.add(pill);

            Runnable task = () -> {
                int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE prioridad='" + prio + "' AND estado != 'Resuelto' AND estado != 'Cerrado'");
                SwingUtilities.invokeLater(() -> lblCount.setText(String.valueOf(v)));
            };
            refreshTasks.add(task);
            task.run();
        }
        return p;
    }

    // ── CARD ──────────────────────────────────────────────────────
    private JPanel buildCard(String title, String subtitle, Color accent, String icon, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(UIConstants.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 12, 12));
                // Franja izquierda color
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                // Brillo superior
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 15));
                g2.fillRect(4, 0, getWidth()-4, 2);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(0, 108));
        card.setLayout(new BorderLayout(10, 0));
        card.setOpaque(false);

        // Icono
        JPanel iconWrapper = new JPanel(new GridBagLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.setBorder(new EmptyBorder(0, 14, 0, 0));
        iconWrapper.add(buildIconLabel(icon, accent));

        // Textos
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_HEADING);
        titleLbl.setForeground(UIConstants.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accent);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UIConstants.FONT_SMALL);
        subLbl.setForeground(UIConstants.TEXT_MUTED);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(14, 0, 12, 14));
        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subLbl);

        card.add(iconWrapper, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ── ICONO DIBUJADO ────────────────────────────────────────────
    private JLabel buildIconLabel(String type, Color accent) {
        Color bg     = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22);
        Color border = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55);

        return new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, 39, 39);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 20, cy = 20;
                switch (type) {
                    case Icons.CLIPBOARD -> {
                        g2.drawRoundRect(cx-7, cy-9, 14, 18, 3, 3);
                        g2.drawLine(cx-3, cy-2, cx+3, cy-2);
                        g2.drawLine(cx-3, cy+2, cx+1, cy+2);
                        g2.drawRoundRect(cx-4, cy-12, 8, 5, 2, 2);
                    }
                    case Icons.ALERT -> {
                        g2.drawPolygon(new int[]{cx,cx-9,cx+9}, new int[]{cy-9,cy+7,cy+7}, 3);
                        g2.drawLine(cx, cy-3, cx, cy+1);
                        g2.fillOval(cx-1, cy+3, 3, 3);
                    }
                    case Icons.BOX_OPEN -> {
                        g2.drawRoundRect(cx-8, cy-1, 16, 10, 2, 2);
                        g2.drawLine(cx-8, cy+2, cx+8, cy+2);
                        g2.drawLine(cx-10, cy-5, cx-8, cy-1);
                        g2.drawLine(cx+10, cy-5, cx+8, cy-1);
                        g2.drawLine(cx-10, cy-5, cx+10, cy-5);
                    }
                    case Icons.BOX_CLOSED -> {
                        g2.drawRoundRect(cx-8, cy-7, 16, 14, 2, 2);
                        g2.drawLine(cx-8, cy-1, cx+8, cy-1);
                        g2.drawLine(cx-2, cy-7, cx+2, cy-7);
                    }
                    case Icons.USERS -> {
                        g2.drawOval(cx-5, cy-9, 8, 8);
                        g2.drawArc(cx-9, cy-1, 14, 10, 0, -180);
                        g2.drawOval(cx+2, cy-8, 7, 7);
                        g2.drawArc(cx, cy, 12, 9, 0, -180);
                    }
                    case Icons.TRANSFER -> {
                        g2.drawLine(cx-8, cy-4, cx+8, cy-4);
                        g2.drawPolyline(new int[]{cx+4,cx+8,cx+4}, new int[]{cy-8,cy-4,cy}, 3);
                        g2.drawLine(cx-8, cy+4, cx+8, cy+4);
                        g2.drawPolyline(new int[]{cx-4,cx-8,cx-4}, new int[]{cy,cy+4,cy+8}, 3);
                    }
                    case Icons.ERROR -> {
                        g2.drawOval(cx-8, cy-8, 16, 16);
                        g2.drawLine(cx-4, cy-4, cx+4, cy+4);
                        g2.drawLine(cx+4, cy-4, cx-4, cy+4);
                    }
                    case Icons.BELL -> {
                        g2.drawArc(cx-7, cy-9, 14, 14, 0, 180);
                        g2.drawLine(cx-7, cy+5, cx+7, cy+5);
                        g2.drawLine(cx-7, cy-2, cx-7, cy+5);
                        g2.drawLine(cx+7, cy-2, cx+7, cy+5);
                        g2.drawArc(cx-3, cy+4, 6, 5, 180, 180);
                    }
                    case Icons.DONE -> {
                        g2.drawOval(cx-8, cy-8, 16, 16);
                        g2.drawPolyline(new int[]{cx-4,cx-1,cx+5}, new int[]{cy,cy+4,cy-4}, 3);
                    }
                    case Icons.SERVER -> {
                        g2.drawRoundRect(cx-8, cy-8, 16, 7, 2, 2);
                        g2.drawRoundRect(cx-8, cy+1, 16, 7, 2, 2);
                        g2.fillOval(cx+3, cy-5, 3, 3);
                        g2.fillOval(cx+3, cy+4, 3, 3);
                    }
                    case Icons.KEY -> {
                        g2.drawOval(cx-8, cy-7, 10, 10);
                        g2.drawLine(cx+2, cy-2, cx+9, cy+5);
                        g2.drawLine(cx+6, cy+3, cx+8, cy+1);
                        g2.drawLine(cx+8, cy+5, cx+10, cy+3);
                    }
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UIConstants.TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(0, 2, 0, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makePill(String text, Color accent) {
        JLabel pill = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pill.setForeground(accent);
        pill.setBorder(new EmptyBorder(2, 7, 2, 7));
        pill.setOpaque(false);
        return pill;
    }

    // ── AUTO REFRESH ──────────────────────────────────────────────
    private void refreshAll() {
        refreshTasks.forEach(Runnable::run);
        SwingUtilities.invokeLater(() ->
                lblLastUpdate.setText("Actualizado: " +
                        new SimpleDateFormat("HH:mm:ss").format(new Date())));
    }

    private void startAutoRefresh() {
        SwingUtilities.invokeLater(() ->
                lblLastUpdate.setText("Actualizado: " +
                        new SimpleDateFormat("HH:mm:ss").format(new Date())));
        Timer timer = new Timer(30_000, e -> refreshAll());
        timer.setInitialDelay(500);
        timer.start();
    }

    // ── DB HELPER ────────────────────────────────────────────────
    private int queryInt(String sql) {
        try (Statement s = DatabaseManager.getInstance().getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    // ── ICONOS ────────────────────────────────────────────────────
    static final class Icons {
        static final String CLIPBOARD  = "clipboard";
        static final String ALERT      = "alert";
        static final String BOX_OPEN   = "box_open";
        static final String BOX_CLOSED = "box_closed";
        static final String USERS      = "users";
        static final String TRANSFER   = "transfer";
        static final String ERROR      = "error";
        static final String BELL       = "bell";
        static final String DONE       = "done";
        static final String SERVER     = "server";
        static final String KEY        = "key";
    }
}
