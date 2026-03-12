package baustro.fin.ec.ui.panels;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.service.ApiService;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    // UI PRINCIPAL
    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // HEADER
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
        if (ico != null) title.setIcon(ico);
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        left.add(title);
        left.add(makePill("LIVE", UIConstants.ACCENT_GREEN));

        lblLastUpdate = new JLabel("—");
        lblLastUpdate.setFont(UIConstants.FONT_SMALL);
        lblLastUpdate.setForeground(UIConstants.TEXT_MUTED);

        header.add(left,          BorderLayout.WEST);
        header.add(lblLastUpdate, BorderLayout.EAST);
        return header;
    }

    // CONTENIDO
    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BG_DARK);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Fila local: 2 columnas, 1 fila
        CardDef[] locales = {
                new CardDef("Correctivos", null,                 "Total registrados", UIConstants.ACCENT_BLUE,   Icons.CLIPBOARD),
                new CardDef("Abiertos",    null,                 "Pendientes ahora",  UIConstants.ACCENT_RED,    Icons.ALERT),
        };

        JPanel gridLocal = new JPanel(new GridLayout(1, 2, 14, 14));
        gridLocal.setOpaque(false);
        gridLocal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));

        for (int i = 0; i < locales.length; i++) {
            JLabel valueLabel = new JLabel("···");
            gridLocal.add(buildCard(locales[i], valueLabel));
            final boolean isCorrectivos = (i == 0);
            Runnable task = () -> {
                int v = isCorrectivos ? countOf("correctivos") : countByEstado("Abierto");
                SwingUtilities.invokeLater(() -> valueLabel.setText(String.valueOf(v)));
            };
            task.run();
            refreshTasks.add(task);
        }

        //Grid API: 4 columnas, filas que sean necesarias
        CardDef[] apis = {
                new CardDef("Pendiente", "","Turno actual",   UIConstants.ACCENT_GREEN,  Icons.BOX_OPEN),
                new CardDef("Pendiente", "","Turno actual",   UIConstants.ACCENT_ORANGE, Icons.BOX_CLOSED),
                new CardDef("Pendiente", "","En sistema",     UIConstants.ACCENT_PURPLE, Icons.USERS),
                new CardDef("Pendiente", "","Procesadas hoy", UIConstants.ACCENT_CYAN,   Icons.TRANSFER),
                new CardDef("Pendiente", "","Últimas 24h",    UIConstants.ACCENT_RED,    Icons.ERROR),
                new CardDef("Pendiente", "","Sin atender",    UIConstants.ACCENT_ORANGE, Icons.BELL),
                new CardDef("Pendiente", "","Turno actual",   UIConstants.ACCENT_GREEN,  Icons.BOX_OPEN),
                new CardDef("Pendiente", "","Turno actual",   UIConstants.ACCENT_ORANGE, Icons.BOX_CLOSED),
                new CardDef("Pendiente", "","En sistema",     UIConstants.ACCENT_PURPLE, Icons.USERS),
                new CardDef("Pendiente", "","Procesadas hoy", UIConstants.ACCENT_CYAN,   Icons.TRANSFER),
                new CardDef("Pendiente", "","Últimas 24h",    UIConstants.ACCENT_RED,    Icons.ERROR),
                new CardDef("Pendiente", "","Sin atender",    UIConstants.ACCENT_ORANGE, Icons.BELL),
                new CardDef("Pendiente", "","Sin atender",    UIConstants.ACCENT_ORANGE, Icons.BELL),
                new CardDef("Pendiente", "","Sin atender",    UIConstants.ACCENT_ORANGE, Icons.BELL),
                new CardDef("Cajas Abiertas","/api/cajas/abiertas","Sin atender", UIConstants.ACCENT_GREEN,  Icons.BOX_OPEN),
                new CardDef("Cajas Cerradas","/api/cajas/cerradas","Sin atender", UIConstants.ACCENT_ORANGE, Icons.BOX_CLOSED)
        };

        JPanel gridApi = new JPanel(new GridLayout(0, 4, 14, 14));
        gridApi.setOpaque(false);

        for (CardDef def : apis) {
            JLabel valueLabel = new JLabel("···");
            gridApi.add(buildCard(def, valueLabel));
            Runnable task = () -> ApiService.fetchCount(def.endpoint, valueLabel);
            task.run();
            refreshTasks.add(task);
        }

        content.add(gridLocal);
        content.add(Box.createVerticalStrut(15));
        content.add(gridApi);

        wrapper.add(content, BorderLayout.NORTH);
        return wrapper;
    }

    // CARD
    private JPanel buildCard(CardDef def, JLabel valueLabel) {
        var card = getJPanel(def);

        // Ícono centrado verticalmente
        JPanel iconWrapper = new JPanel(new GridBagLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.setBorder(new EmptyBorder(0, 16, 0, 0));
        iconWrapper.add(buildIconLabel(def.icon, def.accent));

        // Textos
        JLabel titleLbl = new JLabel(def.title);
        titleLbl.setFont(UIConstants.FONT_HEADING);
        titleLbl.setForeground(UIConstants.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(def.accent);

        JLabel subLbl = new JLabel(def.subtitle);
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
        card.add(textPanel,   BorderLayout.CENTER);
        return card;
    }

    private JPanel getJPanel(CardDef def) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo
                g2.setColor(UIConstants.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // Borde
                g2.setColor(UIConstants.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));

                // Franja lateral izquierda (4px)
                g2.setColor(def.accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);

                // Brillo superior sutil
                g2.setColor(new Color(
                        def.accent.getRed(),
                        def.accent.getGreen(),
                        def.accent.getBlue(), 15));
                g2.fillRect(4, 0, getWidth() - 4, 2);

                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(0, 108));
        card.setLayout(new BorderLayout(10, 0));
        card.setOpaque(false);
        return card;
    }

    // ÍCONO DIBUJADO
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
                        g2.drawRoundRect(cx - 7, cy - 9, 14, 18, 3, 3);
                        g2.drawLine(cx - 3, cy - 2, cx + 3, cy - 2);
                        g2.drawLine(cx - 3, cy + 2, cx + 1, cy + 2);
                        g2.drawRoundRect(cx - 4, cy - 12, 8, 5, 2, 2);
                    }
                    case Icons.ALERT -> {
                        g2.drawPolygon(
                                new int[]{cx, cx - 9, cx + 9},
                                new int[]{cy - 9, cy + 7, cy + 7}, 3);
                        g2.drawLine(cx, cy - 3, cx, cy + 1);
                        g2.fillOval(cx - 1, cy + 3, 3, 3);
                    }
                    case Icons.BOX_OPEN -> {
                        g2.drawRoundRect(cx - 8, cy - 1, 16, 10, 2, 2);
                        g2.drawLine(cx - 8, cy + 2, cx + 8, cy + 2);
                        g2.drawLine(cx - 10, cy - 5, cx - 8, cy - 1);
                        g2.drawLine(cx + 10, cy - 5, cx + 8, cy - 1);
                        g2.drawLine(cx - 10, cy - 5, cx + 10, cy - 5);
                    }
                    case Icons.BOX_CLOSED -> {
                        g2.drawRoundRect(cx - 8, cy - 7, 16, 14, 2, 2);
                        g2.drawLine(cx - 8, cy - 1, cx + 8, cy - 1);
                        g2.drawLine(cx - 2, cy - 7, cx + 2, cy - 7);
                    }
                    case Icons.USERS -> {
                        g2.drawOval(cx - 5, cy - 9, 8, 8);
                        g2.drawArc(cx - 9, cy - 1, 14, 10, 0, -180);
                        g2.drawOval(cx + 2, cy - 8, 7, 7);
                        g2.drawArc(cx, cy, 12, 9, 0, -180);
                    }
                    case Icons.TRANSFER -> {
                        g2.drawLine(cx - 8, cy - 4, cx + 8, cy - 4);
                        g2.drawPolyline(
                                new int[]{cx + 4, cx + 8, cx + 4},
                                new int[]{cy - 8, cy - 4, cy}, 3);
                        g2.drawLine(cx - 8, cy + 4, cx + 8, cy + 4);
                        g2.drawPolyline(
                                new int[]{cx - 4, cx - 8, cx - 4},
                                new int[]{cy, cy + 4, cy + 8}, 3);
                    }
                    case Icons.ERROR -> {
                        g2.drawOval(cx - 8, cy - 8, 16, 16);
                        g2.drawLine(cx - 4, cy - 4, cx + 4, cy + 4);
                        g2.drawLine(cx + 4, cy - 4, cx - 4, cy + 4);
                    }
                    case Icons.BELL -> {
                        g2.drawArc(cx - 7, cy - 9, 14, 14, 0, 180);
                        g2.drawLine(cx - 7, cy + 5, cx + 7, cy + 5);
                        g2.drawLine(cx - 7, cy - 2, cx - 7, cy + 5);
                        g2.drawLine(cx + 7, cy - 2, cx + 7, cy + 5);
                        g2.drawArc(cx - 3, cy + 4, 6, 5, 180, 180);
                    }
                }
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
            @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
        };
    }

    // PILL
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
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
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

    // AUTO REFRESH
    private void startAutoRefresh() {
        SwingUtilities.invokeLater(() ->
                lblLastUpdate.setText("Actualizado: " +
                        new SimpleDateFormat("HH:mm:ss").format(new Date())));

        Timer timer = new Timer(30_000, e -> {
            refreshTasks.forEach(Runnable::run);
            SwingUtilities.invokeLater(() ->
                    lblLastUpdate.setText("Actualizado: " +
                            new SimpleDateFormat("HH:mm:ss").format(new Date())));
        });
        timer.setInitialDelay(30_000);
        timer.start();
    }

    // CONSULTAS BD
    private int countOf(String table) {
        try (Statement s = DatabaseManager.getInstance().getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private int countByEstado(String estado) {
        try (Statement s = DatabaseManager.getInstance().getConnection().createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM correctivos WHERE estado='" + estado + "'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    // DEFINICIÓN DE CARD
    record CardDef(String title, String endpoint, String subtitle, Color accent, String icon) {}

    // CONSTANTES DE ÍCONOS
    static final class Icons {
        static final String CLIPBOARD  = "clipboard";
        static final String ALERT      = "alert";
        static final String BOX_OPEN   = "box_open";
        static final String BOX_CLOSED = "box_closed";
        static final String USERS      = "users";
        static final String TRANSFER   = "transfer";
        static final String ERROR      = "error";
        static final String BELL       = "bell";
    }
}