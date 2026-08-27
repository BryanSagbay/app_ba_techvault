package baustro.fin.ec.ui.panels;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.service.ApiService;
import baustro.fin.ec.ui.UIConstants;
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



    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        startAutoRefresh();
    }

    //  BUILD UI

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_BASE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setBackground(UIConstants.BG_BASE);
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
        title.setFont(UIConstants.FONT_HEADER);
        title.setForeground(UIConstants.TEXT_BRIGHT);
        left.add(title);
        left.add(makePill());

        // RIGHT: last update + refresh button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        lblLastUpdate = new JLabel("—");
        lblLastUpdate.setFont(UIConstants.FONT_META);
        lblLastUpdate.setForeground(UIConstants.TEXT_DIM);
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
                        0, 0, UIConstants.TEAL_PRIMARY,
                        getWidth() / 2f, 0, UIConstants.INDIGO,
                        false
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
                g2.setColor(UIConstants.BG_SURFACE);
                g2.fillRect(0, 2, getWidth(), getHeight() - 2);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_SUBTLE),
                new EmptyBorder(16, 32, 16, 32)
        ));
        return header;
    }

    private JButton createRefreshButton() {
        JButton btn = new JButton("Actualizar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(UIConstants.TEAL_PRIMARY.getRed(), UIConstants.TEAL_PRIMARY.getGreen(), UIConstants.TEAL_PRIMARY.getBlue(), 20)
                        : new Color(0, 0, 0, 0);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                Color border = getModel().isRollover() ? UIConstants.TEAL_PRIMARY : UIConstants.BORDER_ACTIVE;
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIConstants.FONT_BTN);
        btn.setForeground(UIConstants.TEXT_MID);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(UIConstants.TEAL_PRIMARY); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(UIConstants.TEXT_MID);    btn.repaint(); }
        });
        btn.addActionListener(e -> refreshAll());
        return btn;
    }

    //  CONTENT
    private JPanel buildContent() {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BG_BASE);
        wrapper.setBorder(new EmptyBorder(36, 44, 48, 44));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // SECTION: CAJAS (API)
        content.add(sectionLabel("CAJAS  ·  MONITOREO EN TIEMPO REAL"));
        content.add(Box.createVerticalStrut(14));

        JPanel cajasGrid = createGrid(3);
        cajasGrid.setMaximumSize(new Dimension(1050, 200));

        JLabel lblCajasAbiertas = heroValueLabel(UIConstants.EMERALD);
        cajasGrid.add(buildHeroCard(
                "Cajas Aperturadas",
                "Total Cajas Activos",
                UIConstants.EMERALD,
                lblCajasAbiertas,
                "●"
        ));
        Runnable rCajasAbiertas = () -> ApiService.fetchCount("/api/cajas/abiertas", lblCajasAbiertas);
        refreshTasks.add(rCajasAbiertas);
        rCajasAbiertas.run();

        JLabel lblCajasCerradas = heroValueLabel(UIConstants.AMBER);
        cajasGrid.add(buildHeroCard(
                "Cajas Cerradas",
                "Total Cierre de Cajas",
                UIConstants.AMBER,
                lblCajasCerradas,
                "○"
        ));
        Runnable rCajasCerradas = () -> ApiService.fetchCount("/api/cajas/cerradas", lblCajasCerradas);
        refreshTasks.add(rCajasCerradas);
        rCajasCerradas.run();

        JLabel lblCajasFaltantes = heroValueLabel(UIConstants.VIOLET);
        cajasGrid.add(buildHeroCard(
                "Cajas Faltantes",
                "Total de Cajas Faltantes",
                UIConstants.VIOLET,
                lblCajasFaltantes,
                "○"
        ));
        Runnable rCajasFaltantes = () -> ApiService.fetchCount("/api/cajas/cajasFaltantes", lblCajasFaltantes);
        refreshTasks.add(rCajasFaltantes);
        rCajasFaltantes.run();

        content.add(cajasGrid);
        content.add(Box.createVerticalStrut(36));

        // FACTURAS
        content.add(sectionLabel("FACTURACIÓN ELECTRÓNICA - MENSUALES"));
        content.add(Box.createVerticalStrut(10));
        JPanel facGrid = createGrid(4);
        facGrid.setMaximumSize(new Dimension(1400, 160));

        JLabel lblFacPend = statLabel(UIConstants.AMBER);
        facGrid.add(buildStatCard("Pendientes", "Facturas en espera", UIConstants.AMBER, lblFacPend));
        Runnable rFacPend = () -> ApiService.fetchFeCount("Facturas - PENDIENTES", lblFacPend);
        refreshTasks.add(rFacPend);
        rFacPend.run();

        JLabel lblFacErr = statLabel(UIConstants.ROSE);
        facGrid.add(buildStatCard("Con Error", "Facturas rechazadas", UIConstants.ROSE, lblFacErr));
        Runnable rFacErr = () -> ApiService.fetchFeCount("Facturas - ERROR", lblFacErr);
        refreshTasks.add(rFacErr);
        rFacErr.run();

        JLabel lblFacRec = statLabel(UIConstants.EMERALD);
        facGrid.add(buildStatCard("Recibidas", "Facturas aceptadas SRI", UIConstants.EMERALD, lblFacRec));
        Runnable rFacRec = () -> ApiService.fetchFeCount("Facturas - RECIBIDAS", lblFacRec);
        refreshTasks.add(rFacRec);
        rFacRec.run();

        JLabel lblFacDev = statLabel(UIConstants.VIOLET);
        facGrid.add(buildStatCard("Devueltas", "Facturas devueltas SRI", UIConstants.VIOLET, lblFacDev));
        Runnable rFacDev = () -> ApiService.fetchFeCount("Facturas - DEVUELTA", lblFacDev);
        refreshTasks.add(rFacDev);
        rFacDev.run();

        content.add(facGrid);
        content.add(Box.createVerticalStrut(20));

        // RETENCIONES
        content.add(sectionLabel("RETENCIONES - MENSUALES"));
        content.add(Box.createVerticalStrut(10));
        JPanel retGrid = createGrid(4);
        retGrid.setMaximumSize(new Dimension(1400, 160));

        JLabel lblRetPend = statLabel(UIConstants.AMBER);
        retGrid.add(buildStatCard("Pendientes", "Retenciones en espera", UIConstants.AMBER, lblRetPend));
        Runnable rRetPend = () -> ApiService.fetchFeCount("Retenciones - PENDIENTES", lblRetPend);
        refreshTasks.add(rRetPend);
        rRetPend.run();

        JLabel lblRetErr = statLabel(UIConstants.ROSE);
        retGrid.add(buildStatCard("Con Error", "Retenciones rechazadas", UIConstants.ROSE, lblRetErr));
        Runnable rRetErr = () -> ApiService.fetchFeCount("Retenciones - ERROR", lblRetErr);
        refreshTasks.add(rRetErr);
        rRetErr.run();

        JLabel lblRetRec = statLabel(UIConstants.EMERALD);
        retGrid.add(buildStatCard("Recibidas", "Retenciones aceptadas SRI", UIConstants.EMERALD, lblRetRec));
        Runnable rRetRec = () -> ApiService.fetchFeCount("Retenciones - RECIBIDAS", lblRetRec);
        refreshTasks.add(rRetRec);
        rRetRec.run();

        JLabel lblRetDev = statLabel(UIConstants.VIOLET);
        retGrid.add(buildStatCard("Devueltas", "Retenciones devueltas SRI", UIConstants.VIOLET, lblRetDev));
        Runnable rRetDev = () -> ApiService.fetchFeCount("Retenciones - DEVUELTA", lblRetDev);
        refreshTasks.add(rRetDev);
        rRetDev.run();

        content.add(retGrid);

        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    //HERO CARD
    private JPanel buildHeroCard(String title, String subtitle, Color accent, JLabel value, String indicator) {

        JPanel card = new JPanel() {
            boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, hovered ? 60 : 35));
                g2.fillRoundRect(4, 6, getWidth() - 5, getHeight() - 5, 16, 16);

                // Body
                g2.setColor(hovered ? UIConstants.BG_CARD_HOVER : UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 16, 16);

                // Border
                g2.setColor(hovered
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80)
                        : UIConstants.BORDER_SUBTLE);

                g2.setStroke(new BasicStroke(hovered ? 1.2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 16, 16);

                // Glow on hover
                if (hovered) {
                    GradientPaint glow = new GradientPaint(
                            0, 0,
                            new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25),
                            0, getHeight() / 2f,
                            new Color(0, 0, 0, 0)
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

        // Indicator + title
        JPanel titleRow = getJPanel(title, accent, indicator);

        // Alineación izquierda (FIX)
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(titleRow);
        inner.add(Box.createVerticalStrut(8));
        inner.add(value);
        inner.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel(subtitle);
        sub.setFont(UIConstants.FONT_CARD_SUB);
        sub.setForeground(UIConstants.TEXT_DIM);

        // Alineación izquierda (FIX)
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        lTitle.setForeground(UIConstants.TEXT_MID);
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

                g2.setColor(hovered ? UIConstants.BG_CARD_HOVER : UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 14, 14);

                g2.setColor(hovered ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60) : UIConstants.BORDER_SUBTLE);
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
        lTitle.setFont(UIConstants.FONT_CARD_TITLE);
        lTitle.setForeground(UIConstants.TEXT_MID);

        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(UIConstants.FONT_CARD_SUB);
        lSub.setForeground(UIConstants.TEXT_DIM);

        inner.add(lTitle);
        inner.add(Box.createVerticalStrut(8));
        inner.add(value);
        inner.add(Box.createVerticalStrut(3));
        inner.add(lSub);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // HELPERS
    private JLabel heroValueLabel(Color accent) {
        JLabel lbl = new JLabel("—");
        lbl.setFont(UIConstants.FONT_HERO);
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
        var bar = getPanel();

        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_SECTION);
        lbl.setForeground(UIConstants.TEXT_DIM);
        lbl.setBorder(new EmptyBorder(0, 10, 0, 0));

        row.add(bar);
        row.add(lbl);
        return row;
    }

    private JPanel getPanel() {
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,UIConstants.TEAL_PRIMARY, 0, getHeight(), UIConstants.INDIGO);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2, 2);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(3, 14));
        bar.setOpaque(false);
        return bar;
    }

    private JLabel makePill() {
        JLabel pill = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(UIConstants.EMERALD.getRed(), UIConstants.EMERALD.getGreen(), UIConstants.EMERALD.getBlue(), 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(UIConstants.EMERALD.getRed(), UIConstants.EMERALD.getGreen(), UIConstants.EMERALD.getBlue(), 80));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                super.paintComponent(g);
            }
        };

        pill.setText("  ● " + "EN VIVO" + "  ");
        pill.setFont(UIConstants.FONT_PILL);
        pill.setForeground(UIConstants.EMERALD);
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(3, 4, 3, 4));
        return pill;
    }

    //  REFRESH
    private void refreshAll() {
        if (isRefreshing) return;
        isRefreshing = true;
        lblLastUpdate.setText("Actualizando...");
        lblLastUpdate.setForeground(UIConstants.TEAL_PRIMARY);

        new Thread(() -> {
            refreshTasks.forEach(Runnable::run);
            SwingUtilities.invokeLater(() -> {
                lblLastUpdate.setText("Actualizado: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                lblLastUpdate.setForeground(UIConstants.TEXT_DIM);
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
            System.err.println("[DashboardPanel] Error en query: " + sql + " → " + e.getMessage());
            return 0;
        }
    }
}