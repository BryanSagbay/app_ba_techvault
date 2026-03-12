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

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel activeNavBtn;

    // Lazy-loaded panels
    private final Map<String, JPanel> loadedPanels = new HashMap<>();

    private static final String PANEL_DASHBOARD  = "dashboard";
    private static final String PANEL_SERVIDOR = "servidores";
    private static final String PANEL_PASSWORD = "contraseñas";
    private static final String PANEL_TAREA = "tareas";
    private static final String PANEL_NOTA = "notas";
    private static final String PANEL_COMANDO  = "comandos";
    private static final String PANEL_CASOS = "casos";
    private static final String PANEL_MANUAL = "manual";


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
        getContentPane().setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        cardLayout   = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(UIConstants.BG_DARK);

        // Solo carga el Dashboard al inicio
        showPanel(PANEL_DASHBOARD);

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel,   BorderLayout.CENTER);
    }

    /**
     * Muestra un panel: si ya fue creado lo reutiliza, si no lo crea en un
     * SwingWorker para no bloquear el EDT durante la consulta inicial a SQLite.
     */
    private void showPanel(String panelName) {
        if (loadedPanels.containsKey(panelName)) {
            cardLayout.show(contentPanel, panelName);
            return;
        }

        // Primera vez mostrar spinner mientras carga
        JPanel spinner = buildSpinner();
        String spinnerKey = panelName + "_loading";
        contentPanel.add(spinner, spinnerKey);
        cardLayout.show(contentPanel, spinnerKey);

        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected JPanel doInBackground() {
                // Crear el panel fuera del EDT para no congelar la UI
                return createPanel(panelName);
            }

            @Override
            protected void done() {
                try {
                    JPanel panel = get();
                    loadedPanels.put(panelName, panel);
                    contentPanel.add(panel, panelName);
                    cardLayout.show(contentPanel, panelName);
                    // Quitar spinner
                    contentPanel.remove(spinner);
                } catch (Exception e) {
                    e.getSuppressed();
                    cardLayout.show(contentPanel, panelName);
                }
            }
        };
        worker.execute();
    }

    private JPanel createPanel(String panelName) {
        return switch (panelName) {
            case PANEL_DASHBOARD  -> new DashboardPanel();
            case PANEL_SERVIDOR   -> new ServidorPanel();
            case PANEL_PASSWORD   -> new ContrasenaPanel();
            case PANEL_TAREA      -> new TareaPanel();
            case PANEL_NOTA       -> new NotaPanel();
            case PANEL_COMANDO    -> new ComandoPanel();
            case PANEL_CASOS -> new CasoPanel();
            case PANEL_MANUAL -> new ManualPanel();
            default               -> new JPanel();
        };
    }

    private JPanel buildSpinner() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIConstants.BG_DARK);
        JLabel lbl = new JLabel("Cargando...");
        lbl.setFont(UIConstants.FONT_HEADING);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl);
        return p;
    }

    // SIDEBAR
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.BG_PANEL);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER));

        // Logo
        JPanel logoPanel = new JPanel(new BorderLayout(10, 0));
        logoPanel.setBackground(UIConstants.BG_DARK);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        JLabel appName = new JLabel("TechOps Manager");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        appName.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon appIcon = IconManager.getNavIcon(IconManager.ICON_APP);
        if (appIcon != null && appIcon.getIconWidth() > 1) appName.setIcon(appIcon);
        logoPanel.add(appName, BorderLayout.CENTER);
        sidebar.add(logoPanel);

        sidebar.add(sectionLabel("PRINCIPAL"));
        JPanel btnDash = navButton("Dashboard", IconManager.ICON_DASHBOARD,  PANEL_DASHBOARD);
        sidebar.add(btnDash);
        activeNavBtn = btnDash;
        setNavActive(btnDash, true);

        sidebar.add(sectionLabel("HERRAMIENTAS"));
        sidebar.add(navButton("Tareas", IconManager.ICON_TAREA, PANEL_TAREA));
        sidebar.add(navButton("Notas", IconManager.ICON_NOTA, PANEL_NOTA));
        sidebar.add(navButton("Contraseñas", IconManager.ICON_PASSWORD, PANEL_PASSWORD));

        sidebar.add(sectionLabel("GESTIÓN"));
        sidebar.add(navButton("Servidores", IconManager.ICON_SERVIDOR, PANEL_SERVIDOR));
        sidebar.add(navButton("Comandos", IconManager.ICON_COMANDO, PANEL_COMANDO));

        sidebar.add(sectionLabel("DOCUMENTACIÓN"));
        sidebar.add(navButton("Casos", IconManager.ICON_CORRECTIVO, PANEL_CASOS));
        sidebar.add(navButton("Manuales", IconManager.ICON_MANUAL, PANEL_MANUAL));

        sidebar.add(Box.createVerticalGlue());

        var bottomInfo = getJPanel();
        sidebar.add(bottomInfo);

        return sidebar;
    }

    private JPanel getJPanel() {
        JPanel bottomInfo = new JPanel(new BorderLayout());
        bottomInfo.setBackground(UIConstants.BG_PANEL);
        bottomInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        bottomInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JLabel version = new JLabel("v1.0  |  Java 21 + SQLite");
        version.setFont(UIConstants.FONT_SMALL);
        version.setForeground(UIConstants.TEXT_MUTED);
        bottomInfo.add(version, BorderLayout.CENTER);
        return bottomInfo;
    }

    private JPanel navButton(String text, String iconName, String panelName) {
        JPanel btn = new JPanel(new BorderLayout(10, 0));
        btn.setBackground(UIConstants.BG_PANEL);
        btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        // Pasar eventos del label al panel para que todas las áreas sea clickeable
        lbl.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { navigateTo(btn, panelName); }
        });

        ImageIcon icon = IconManager.getNavIcon(iconName);
        if (icon != null && icon.getIconWidth() > 1) {
            lbl.setIcon(icon);
            lbl.setIconTextGap(10);
        }

        btn.add(lbl, BorderLayout.CENTER);

        // mousePressed en vez de mouseClicked — responde al primer toque sin
        btn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { navigateTo(btn, panelName); }
            public void mouseEntered(MouseEvent e) {
                if (activeNavBtn != btn) btn.setBackground(UIConstants.BG_CARD);
            }
            public void mouseExited(MouseEvent e) {
                if (activeNavBtn != btn) btn.setBackground(UIConstants.BG_PANEL);
            }
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

    private void setNavActive(JPanel btn, boolean active) {
        if (btn == null) return;
        btn.setBackground(active ? UIConstants.BG_INPUT : UIConstants.BG_PANEL);
        if (active) {
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, UIConstants.ACCENT_BLUE),
                    BorderFactory.createEmptyBorder(11, 15, 11, 18)));
        } else {
            btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        }
        for (Component c : btn.getComponents()) {
            if (c instanceof JLabel lbl) {
                lbl.setForeground(active ? UIConstants.TEXT_PRIMARY : UIConstants.TEXT_SECONDARY);
                lbl.setFont(active ? UIConstants.FONT_BODY.deriveFont(Font.BOLD) : UIConstants.FONT_BODY);
            }
        }
    }

    private JPanel sectionLabel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIConstants.BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(10, 18, 3, 18));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }
}
