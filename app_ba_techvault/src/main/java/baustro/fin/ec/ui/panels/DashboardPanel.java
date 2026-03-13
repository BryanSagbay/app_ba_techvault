package baustro.fin.ec.ui.panels;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.service.ApiService;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final List<Runnable> refreshTasks = new ArrayList<>();
    private JLabel lblLastUpdate;
    private boolean isRefreshing = false;

    // PALETTE
    private static final Color BG_BASE        = new Color(0x0D0F14);
    private static final Color BG_SURFACE     = new Color(0x13161E);
    private static final Color BG_CARD        = new Color(0x181C26);
    private static final Color BG_CARD_HOVER  = new Color(0x1E2330);
    private static final Color BORDER_SUBTLE  = new Color(0x252A38);
    private static final Color BORDER_ACTIVE  = new Color(0x2E3447);

    private static final Color TEAL_PRIMARY   = new Color(0x00D2C8);
    private static final Color INDIGO         = new Color(0x6C6FFF);
    private static final Color AMBER          = new Color(0xF5A623);
    private static final Color ROSE           = new Color(0xFF4F6D);
    private static final Color EMERALD        = new Color(0x00C87A);
    private static final Color VIOLET         = new Color(0xA855F7);
    private static final Color SKY            = new Color(0x38BDF8);

    private static final Color TEXT_BRIGHT    = new Color(0xF1F3F9);
    private static final Color TEXT_MID       = new Color(0x8891A8);
    private static final Color TEXT_DIM       = new Color(0x4D566B);

    //  FONTS
    private static final Font FONT_HERO       = new Font("Segoe UI", Font.BOLD, 38);
    private static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_CARD_SUB   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_PILL       = new Font("Segoe UI", Font.BOLD, 9);
    private static final Font FONT_META       = new Font("Segoe UI", Font.PLAIN, 11);

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_BASE);
        buildUI();
        startAutoRefresh();
    }

    //  BUILD UI

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_BASE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setBackground(BG_BASE);
        add(scroll, BorderLayout.CENTER);
    }

    //  HEADER
    private JPanel buildHeader() {

        var header = getJPanel();

        // LEFT: icon + title + pill
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        ImageIcon ico = IconManager.getIcon(IconManager.ICON_DASHBOARD, 20);
        JLabel title = new JLabel("Dashboard");
        if (ico != null) title.setIcon(ico);
        title.setFont(FONT_HEADER);
        title.setForeground(TEXT_BRIGHT);
        left.add(title);
        left.add(makePill("EN VIVO", EMERALD));

        // RIGHT: last update + refresh button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        lblLastUpdate = new JLabel("—");
        lblLastUpdate.setFont(FONT_META);
        lblLastUpdate.setForeground(TEXT_DIM);
        right.add(lblLastUpdate);

        JButton btnRefresh = createRefreshButton();
        right.add(btnRefresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel getJPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle top accent line
                GradientPaint gp = new GradientPaint(
                        0, 0, TEAL_PRIMARY,
                        getWidth() / 2f, 0, INDIGO,
                        false
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
                g2.setColor(BG_SURFACE);
                g2.fillRect(0, 2, getWidth(), getHeight() - 2);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(16, 32, 16, 32)
        ));
        return header;
    }

    private JButton createRefreshButton() {
        JButton btn = new JButton("↻  Actualizar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(TEAL_PRIMARY.getRed(), TEAL_PRIMARY.getGreen(), TEAL_PRIMARY.getBlue(), 20)
                        : new Color(0, 0, 0, 0);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                Color border = getModel().isRollover() ? TEAL_PRIMARY : BORDER_ACTIVE;
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_MID);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(TEAL_PRIMARY); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_MID);    btn.repaint(); }
        });
        btn.addActionListener(e -> refreshAll());
        return btn;
    }

    //  CONTENT
    private JPanel buildContent() {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_BASE);
        wrapper.setBorder(new EmptyBorder(36, 44, 48, 44));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // SECTION: CAJAS (API)
        content.add(sectionLabel("CAJAS  ·  MONITOREO EN TIEMPO REAL"));
        content.add(Box.createVerticalStrut(14));

        JPanel cajasGrid = createGrid(3);
        cajasGrid.setMaximumSize(new Dimension(1050, 200));

        JLabel lblCajasAbiertas = heroValueLabel(EMERALD);
        cajasGrid.add(buildHeroCard(
                "Cajas Abiertas",
                "Puntos de venta activos ahora",
                EMERALD,
                lblCajasAbiertas,
                "●"
        ));
        Runnable rCajasAbiertas = () -> ApiService.fetchCount("/api/cajas/abiertas", lblCajasAbiertas);
        refreshTasks.add(rCajasAbiertas);
        rCajasAbiertas.run();

        JLabel lblCajasCerradas = heroValueLabel(AMBER);
        cajasGrid.add(buildHeroCard(
                "Cajas Cerradas",
                "Puntos de venta inactivos",
                AMBER,
                lblCajasCerradas,
                "○"
        ));
        Runnable rCajasCerradas = () -> ApiService.fetchCount("/api/cajas/cerradas", lblCajasCerradas);
        refreshTasks.add(rCajasCerradas);
        rCajasCerradas.run();

        JLabel lblCajasFaltantes = heroValueLabel(VIOLET);
        cajasGrid.add(buildHeroCard(
                "Cajas Faltantes",
                "Total de Cajas Faltantes",
                VIOLET,
                lblCajasFaltantes,
                "○"
        ));
        Runnable rCajasFaltantes = () -> ApiService.fetchCount("/api/cajas/cajasFaltantes", lblCajasFaltantes);
        refreshTasks.add(rCajasFaltantes);
        rCajasFaltantes.run();

        content.add(cajasGrid);
        content.add(Box.createVerticalStrut(36));

        // SECTION: CORRECTIVOS
        content.add(sectionLabel("INCIDENTES ·  GESTIÓN DE TAREAS"));
        content.add(Box.createVerticalStrut(14));

        JPanel corrGrid = createGrid(4);

        JLabel lblTotal = statLabel(INDIGO);
        corrGrid.add(buildStatCard("Total Registrados", "Correctivos en sistema", INDIGO, lblTotal));
        Runnable t1 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos");
            SwingUtilities.invokeLater(() -> lblTotal.setText(String.valueOf(v)));
        };
        refreshTasks.add(t1); t1.run();

        JLabel lblOpen = statLabel(ROSE);
        corrGrid.add(buildStatCard("Abiertos", "Pendientes de atención", ROSE, lblOpen));
        Runnable t2 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Abierto'");
            SwingUtilities.invokeLater(() -> lblOpen.setText(String.valueOf(v)));
        };
        refreshTasks.add(t2); t2.run();

        JLabel lblProg = statLabel(AMBER);
        corrGrid.add(buildStatCard("En Progreso", "Atención en curso", AMBER, lblProg));
        Runnable t3 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='En Progreso'");
            SwingUtilities.invokeLater(() -> lblProg.setText(String.valueOf(v)));
        };
        refreshTasks.add(t3); t3.run();

        JLabel lblDone = statLabel(EMERALD);
        corrGrid.add(buildStatCard("Resueltos Hoy", "Cerrados en el día", EMERALD, lblDone));
        Runnable t4 = () -> {
            String hoy = LocalDate.now().toString();
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Resuelto' AND fecha_solucion LIKE '%" + hoy + "%'");
            SwingUtilities.invokeLater(() -> lblDone.setText(String.valueOf(v)));
        };
        refreshTasks.add(t4); t4.run();

        content.add(corrGrid);
        content.add(Box.createVerticalStrut(36));

        // SECTION: RESUMEN SEMANA
        content.add(sectionLabel("ESTA SEMANA  ·  RESUMEN"));
        content.add(Box.createVerticalStrut(14));

        JPanel weekGrid = createGrid(3);

        JLabel lblWeekNew = statLabel(SKY);
        weekGrid.add(buildStatCard("Nuevos", "Correctivos esta semana", SKY, lblWeekNew));
        Runnable tw1 = () -> {
            String from = LocalDate.now().minusDays(7).toString();
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE fecha_creacion >= '" + from + "'");
            SwingUtilities.invokeLater(() -> lblWeekNew.setText(String.valueOf(v)));
        };
        refreshTasks.add(tw1); tw1.run();

        JLabel lblWeekSolved = statLabel(EMERALD);
        weekGrid.add(buildStatCard("Resueltos", "Cerrados esta semana", EMERALD, lblWeekSolved));
        Runnable tw2 = () -> {
            String from = LocalDate.now().minusDays(7).toString();
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE estado='Resuelto' AND fecha_solucion >= '" + from + "'");
            SwingUtilities.invokeLater(() -> lblWeekSolved.setText(String.valueOf(v)));
        };
        refreshTasks.add(tw2); tw2.run();

        JLabel lblWeekCritical = statLabel(ROSE);
        weekGrid.add(buildStatCard("Críticos", "Alta prioridad activos", ROSE, lblWeekCritical));
        Runnable tw3 = () -> {
            int v = queryInt("SELECT COUNT(*) FROM correctivos WHERE prioridad='Alta' AND estado!='Resuelto'");
            SwingUtilities.invokeLater(() -> lblWeekCritical.setText(String.valueOf(v)));
        };
        refreshTasks.add(tw3); tw3.run();

        content.add(weekGrid);

        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    //HERO CARD (para Cajas)
    private JPanel buildHeroCard(String title, String subtitle, Color accent, JLabel value, String indicator) {

        JPanel card = new JPanel() {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, hovered ? 60 : 35));
                g2.fillRoundRect(4, 6, getWidth() - 5, getHeight() - 5, 16, 16);

                // Body
                g2.setColor(hovered ? BG_CARD_HOVER : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);

                // Border
                g2.setColor(hovered ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80) : BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(hovered ? 1.2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 16, 16);

                // Glow on top
                if (hovered) {
                    GradientPaint glow = new GradientPaint(
                            0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25),
                            0, getHeight() / 2f, new Color(0, 0, 0, 0)
                    );
                    g2.setPaint(glow);
                    g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);
                }

                // Accent left bar
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1f));
                RoundRectangle2D bar = new RoundRectangle2D.Float(0, 18, 3, getHeight() - 50, 3, 3);
                g2.fill(bar);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(0, 155));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 22, 18, 20));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        //Indicator dot + title row
        var titleRow = getJPanel(title, accent, indicator);
        inner.add(titleRow);
        inner.add(Box.createVerticalStrut(8));
        inner.add(value);
        inner.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel(subtitle);
        sub.setFont(FONT_CARD_SUB);
        sub.setForeground(TEXT_DIM);
        inner.add(sub);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel getJPanel(String title, Color accent, String indicator) {
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);

        JLabel dot = new JLabel(indicator);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(accent);

        JLabel lTitle = new JLabel(title.toUpperCase());
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lTitle.setForeground(TEXT_MID);
        lTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleRow.add(dot);
        titleRow.add(lTitle);
        return titleRow;
    }

    //  STAT CARD (para métricas)
    private JPanel buildStatCard(String title, String subtitle, Color accent, JLabel value) {

        JPanel card = new JPanel() {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            }); }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, hovered ? 50 : 28));
                g2.fillRoundRect(3, 4, getWidth() - 3, getHeight() - 3, 14, 14);

                g2.setColor(hovered ? BG_CARD_HOVER : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 14, 14);

                g2.setColor(hovered ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60) : BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 14, 14);

                // Top accent stripe
                GradientPaint stripe = new GradientPaint(
                        0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), hovered ? 180 : 120),
                        getWidth() / 3f, 0, new Color(0, 0, 0, 0)
                );
                g2.setPaint(stripe);
                g2.fillRoundRect(0, 0, getWidth() - 4, 3, 14, 14);

                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(0, 122));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 14, 16));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(FONT_CARD_TITLE);
        lTitle.setForeground(TEXT_MID);

        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(FONT_CARD_SUB);
        lSub.setForeground(TEXT_DIM);

        inner.add(lTitle);
        inner.add(Box.createVerticalStrut(8));
        inner.add(value);
        inner.add(Box.createVerticalStrut(3));
        inner.add(lSub);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    //  HELPERS
    private JLabel heroValueLabel(Color accent) {
        JLabel lbl = new JLabel("—");
        lbl.setFont(FONT_HERO);
        lbl.setForeground(accent);
        return lbl;
    }

    private JLabel statLabel(Color accent) {
        JLabel lbl = new JLabel("—");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lbl.setForeground(accent);
        return lbl;
    }

    private JPanel createGrid(int columns) {
        JPanel grid = new JPanel(new GridLayout(0, columns, 16, 16));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        return grid;
    }

    private JComponent sectionLabel(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Accent bar
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, TEAL_PRIMARY, 0, getHeight(), INDIGO);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2, 2);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(3, 14));
        bar.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT_DIM);
        lbl.setBorder(new EmptyBorder(0, 10, 0, 0));

        row.add(bar);
        row.add(lbl);
        return row;
    }

    private JLabel makePill(String text, Color accent) {
        JLabel pill = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                super.paintComponent(g);
            }
        };

        pill.setText("  ● " + text + "  ");
        pill.setFont(FONT_PILL);
        pill.setForeground(accent);
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(3, 4, 3, 4));
        return pill;
    }

    //  REFRESH
    private void refreshAll() {
        if (isRefreshing) return;
        isRefreshing = true;
        lblLastUpdate.setText("Actualizando...");
        lblLastUpdate.setForeground(TEAL_PRIMARY);

        new Thread(() -> {
            refreshTasks.forEach(Runnable::run);
            SwingUtilities.invokeLater(() -> {
                lblLastUpdate.setText("Actualizado: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                lblLastUpdate.setForeground(TEXT_DIM);
                isRefreshing = false;
            });
        }).start();
    }

    private void startAutoRefresh() {
        Timer t = new Timer(30_000, e -> refreshAll());
        t.setInitialDelay(1200);
        t.start();
    }

    //  DB QUERY
    private int queryInt(String sql) {
        try (
                Statement s = DatabaseManager.getInstance().getConnection().createStatement();
                ResultSet rs = s.executeQuery(sql)
        ) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
