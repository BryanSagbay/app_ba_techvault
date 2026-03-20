package baustro.fin.ec.ui;

import baustro.fin.ec.ui.panels.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private JPanel      contentPanel;
    private CardLayout  cardLayout;
    private JPanel      activeNavBtn;
    private JLabel      themeToggleLabel;

    private final Map<String, JPanel> loadedPanels = new HashMap<>();

    private static final String PANEL_DASHBOARD    = "dashboard";
    private static final String PANEL_SERVCRED     = "servcred";
    private static final String PANEL_TAREA        = "tareas";
    private static final String PANEL_NOTA         = "notas";
    private static final String PANEL_COMANDO      = "comandos";
    private static final String PANEL_PRODUCTION   = "produccion";
    private static final String PANEL_MANUAL       = "manual";
    private static final String PANEL_SCRIPT       = "script";
    private static final String PANEL_TRANSACCION  = "transacciones";
    private static final String PANEL_EMERGENTE    = "emergentes";

    public MainFrame() {
        super("TechOps Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);

        ImageIcon appIcon = IconManager.getLargeIcon(IconManager.ICON_APP);
        if (appIcon != null && appIcon.getIconWidth() > 1) setIconImage(appIcon.getImage());

        buildUI();
    }

    private void buildUI() {
        getContentPane().setBackground(UIConstants.BG_BASE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout   = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(UIConstants.BG_BASE);

        showPanel(PANEL_DASHBOARD);

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel,   BorderLayout.CENTER);
    }

    private void showPanel(String panelName) {
        if (PANEL_SERVCRED.equals(panelName) && loadedPanels.containsKey(panelName)) {
            JPanel old = loadedPanels.remove(panelName);
            contentPanel.remove(old);
        }

        if (loadedPanels.containsKey(panelName)) {
            cardLayout.show(contentPanel, panelName);
            return;
        }

        JPanel spinner    = buildSpinner();
        String spinnerKey = panelName + "_loading";
        contentPanel.add(spinner, spinnerKey);
        cardLayout.show(contentPanel, spinnerKey);

        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            @Override protected JPanel doInBackground() { return createPanel(panelName); }
            @Override protected void done() {
                try {
                    JPanel panel = get();
                    loadedPanels.put(panelName, panel);
                    contentPanel.add(panel, panelName);
                    cardLayout.show(contentPanel, panelName);
                    contentPanel.remove(spinner);
                } catch (Exception e) {
                    cardLayout.show(contentPanel, panelName);
                }
            }
        };
        worker.execute();
    }

    private JPanel createPanel(String panelName) {
        return switch (panelName) {
            case PANEL_DASHBOARD   -> new DashboardPanel();
            case PANEL_SERVCRED    -> new ServidorContrasenaPanel();
            case PANEL_TAREA       -> new TareaPanel();
            case PANEL_NOTA        -> new NotaPanel();
            case PANEL_COMANDO     -> new ComandoPanel();
            case PANEL_PRODUCTION  -> new ProductionPanel();
            case PANEL_MANUAL      -> new ManualPanel();
            case PANEL_SCRIPT      -> new ScriptPanel();
            case PANEL_TRANSACCION -> new TransaccionPanel();
            case PANEL_EMERGENTE   -> new EmergentePanel();
            default                -> new JPanel();
        };
    }

    private JPanel buildSpinner() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIConstants.BG_BASE);
        JLabel lbl = new JLabel("Cargando...");
        lbl.setFont(UIConstants.FONT_HEADING);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl);
        return p;
    }

    //  SIDEBAR

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.BG_CARD);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER));

        // Logo
        JPanel logoPanel = new JPanel(new BorderLayout(10, 0));
        logoPanel.setBackground(UIConstants.BG_BASE);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        JLabel appName = new JLabel("TechOps Manager");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        appName.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon appIcon = IconManager.getNavIcon(IconManager.ICON_APP);
        if (appIcon != null && appIcon.getIconWidth() > 1) appName.setIcon(appIcon);
        logoPanel.add(appName, BorderLayout.CENTER);
        sidebar.add(logoPanel);

        // PRINCIPAL
        sidebar.add(sectionLabel("PRINCIPAL"));
        JPanel btnDash = navButton("Dashboard", IconManager.ICON_DASHBOARD, PANEL_DASHBOARD);
        sidebar.add(btnDash);
        activeNavBtn = btnDash;
        setNavActive(btnDash, true);

        // HERRAMIENTAS
        sidebar.add(sectionLabel("HERRAMIENTAS"));
        sidebar.add(navButton("Tareas",      IconManager.ICON_TAREA,    PANEL_TAREA));
        sidebar.add(navButton("Notas",       IconManager.ICON_NOTA,     PANEL_NOTA));
        sidebar.add(navButton("Servidores",  IconManager.ICON_SERVIDOR, PANEL_SERVCRED));

        // GESTIÓN
        sidebar.add(sectionLabel("GESTIÓN"));
        sidebar.add(navButton("Comandos",      IconManager.ICON_COMANDO, PANEL_COMANDO));
        sidebar.add(navButton("Transacciones", IconManager.ICON_TRX,     PANEL_TRANSACCION));

        // DOCUMENTACIÓN
        sidebar.add(sectionLabel("DOCUMENTACIÓN"));
        sidebar.add(navButton("CAB",        IconManager.ICON_CORRECTIVO, PANEL_PRODUCTION));
        sidebar.add(navButton("Manuales",   IconManager.ICON_MANUAL,     PANEL_MANUAL));
        sidebar.add(navButton("Scripts",    IconManager.ICON_SQL,     PANEL_SCRIPT));   // ← ícono correcto
        sidebar.add(navButton("Emergentes", IconManager.ICON_AZURE,      PANEL_EMERGENTE));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(buildThemeToggle());
        sidebar.add(buildBottomInfo());

        return sidebar;
    }

    private JPanel buildThemeToggle() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(UIConstants.BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Ícono dinámico: sol cuando está en oscuro (cambiar a claro), luna cuando está en claro
        String iconName = ThemeManager.isDark() ? IconManager.ICON_SUN : IconManager.ICON_MOON;
        String labelText = ThemeManager.isDark() ? "Tema Claro" : "Tema Oscuro";

        ImageIcon themeIcon = IconManager.getIcon(iconName, 16);
        themeToggleLabel = new JLabel(labelText);
        if (themeIcon != null && themeIcon.getIconWidth() > 1) {
            themeToggleLabel.setIcon(themeIcon);
            themeToggleLabel.setIconTextGap(8);
        }
        themeToggleLabel.setFont(UIConstants.FONT_SMALL);
        themeToggleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        themeToggleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        themeToggleLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e)  { toggleTheme(); }
            @Override public void mouseEntered(MouseEvent e)  { themeToggleLabel.setForeground(UIConstants.TEAL_PRIMARY); }
            @Override public void mouseExited(MouseEvent e)   { themeToggleLabel.setForeground(UIConstants.TEXT_SECONDARY); }
        });

        row.add(themeToggleLabel, BorderLayout.CENTER);
        return row;
    }

    private void toggleTheme() {
        ThemeManager.toggle();
        SwingUtilities.invokeLater(() -> {
            getContentPane().setBackground(UIConstants.BG_BASE);
            loadedPanels.clear();
            contentPanel.removeAll();
            getContentPane().removeAll();
            buildUI();
            revalidate();
            repaint();
        });
    }

    private JPanel buildBottomInfo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 16, 10, 16)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel version = new JLabel("v1.1  |  Java 21 + SQLite");
        version.setFont(UIConstants.FONT_SMALL);
        version.setForeground(UIConstants.TEXT_MUTED);
        p.add(version, BorderLayout.CENTER);
        return p;
    }

    //  NAV HELPERS

    private void setNavActive(JPanel btn, boolean active) {
        if (btn == null) return;
        if (active) {
            btn.setBackground(UIConstants.BG_SURFACE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, UIConstants.ACCENT_BLUE),
                    BorderFactory.createEmptyBorder(11, 15, 11, 18)));
        } else {
            btn.setBackground(UIConstants.BG_CARD);
            btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        }
        for (Component c : btn.getComponents()) {
            if (c instanceof JLabel lbl)
                lbl.setForeground(active ? UIConstants.TEXT_PRIMARY : UIConstants.TEXT_SECONDARY);
        }
        btn.repaint();
    }

    private JPanel navButton(String text, String iconName, String panelName) {
        JPanel btn = new JPanel(new BorderLayout(10, 0));
        btn.setBackground(UIConstants.BG_CARD);
        btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        lbl.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { navigateTo(btn, panelName); }
        });

        ImageIcon icon = IconManager.getNavIcon(iconName);
        if (icon != null && icon.getIconWidth() > 1) { lbl.setIcon(icon); lbl.setIconTextGap(10); }

        btn.add(lbl, BorderLayout.CENTER);
        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { navigateTo(btn, panelName); }
        });
        return btn;
    }

    private void navigateTo(JPanel btn, String panelName) {
        if (activeNavBtn == btn) return;
        setNavActive(activeNavBtn, false);
        setNavActive(btn, true);
        activeNavBtn = btn;
        showPanel(panelName);
    }

    private JPanel sectionLabel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(10, 18, 3, 18));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }
}