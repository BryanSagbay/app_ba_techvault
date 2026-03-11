package baustro.fin.ec.ui;

import baustro.fin.ec.ui.panels.*;
import baustro.fin.ec.util.CryptoUtil;
import baustro.fin.ec.util.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {

    private static final int SIDEBAR_W = 220;

    private JPanel contentCards;
    private CardLayout cardLayout;
    private JLabel lblCurrentSection;
    private final ButtonGroup sidebarGroup = new ButtonGroup();

    private static final Object[][] NAV = {
            {"🐛", "Incidencias",  "INCIDENCIAS"},
            {"🖧",  "Servidores",   "SERVIDORES"},
            {"🔐", "Contraseñas", "CONTRASENAS"},
            {"📝", "Notas",        "NOTAS"},
            {"💻", "Comandos",     "COMANDOS"},
    };

    public MainWindow() {
        super("TechOps Manager");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { salir(); }
        });
        buildUI();
        // Tamaño fijo antes de setVisible para que el layout se calcule bien
        setSize(1360, 820);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        // Usar un JSplitPane fijo para sidebar + contenido
        // Esto garantiza que el sidebar nunca "rompa" el layout
        JPanel sidebar    = buildSidebar();
        JPanel mainArea   = buildMainArea();

        // Panel raíz con BorderLayout puro — el más predecible en Swing
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG_DARK);

        // Wrapper del sidebar con ancho fijo
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidebar.setMinimumSize(new Dimension(SIDEBAR_W, 0));
        sidebar.setMaximumSize(new Dimension(SIDEBAR_W, Integer.MAX_VALUE));

        root.add(sidebar,  BorderLayout.WEST);
        root.add(mainArea, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ─────────────────────────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_DEEPEST);
        sidebar.setOpaque(true);

        // ── Logo ──────────────────────────────────────────────────────
        JPanel logo = new JPanel(new GridLayout(2, 1, 0, 3));
        logo.setBackground(UITheme.BG_DEEPEST);
        logo.setBorder(new EmptyBorder(22, 20, 18, 20));
        logo.setMaximumSize(new Dimension(SIDEBAR_W, 72));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoTitle = new JLabel("⚙  TechOps");
        logoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel logoSub = new JLabel("Manager  v1.0");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoSub.setForeground(UITheme.TEXT_GHOST);

        logo.add(logoTitle);
        logo.add(logoSub);
        sidebar.add(logo);
        sidebar.add(hDivider());

        // ── Section label ─────────────────────────────────────────────
        JLabel sec = new JLabel("   MÓDULOS");
        sec.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sec.setForeground(UITheme.TEXT_GHOST);
        sec.setBorder(new EmptyBorder(10, 0, 4, 0));
        sec.setAlignmentX(Component.LEFT_ALIGNMENT);
        sec.setMaximumSize(new Dimension(SIDEBAR_W, 28));
        sidebar.add(sec);

        // ── Nav buttons ───────────────────────────────────────────────
        boolean first = true;
        for (Object[] item : NAV) {
            JToggleButton btn = makeNavBtn((String)item[0], (String)item[1]);
            String card  = (String) item[2];
            String label = (String) item[1];
            String icon  = (String) item[0];
            btn.addActionListener(e -> navigateTo(card, icon + "   " + label));
            sidebarGroup.add(btn);
            sidebar.add(btn);
            if (first) { btn.setSelected(true); first = false; }
        }

        // ── Spacer ────────────────────────────────────────────────────
        JPanel spacer = new JPanel();
        spacer.setBackground(UITheme.BG_DEEPEST);
        spacer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(spacer);
        sidebar.add(Box.createVerticalGlue());

        sidebar.add(hDivider());

        // ── DB status ─────────────────────────────────────────────────
        JPanel dbRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        dbRow.setBackground(UITheme.BG_DEEPEST);
        dbRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbRow.setMaximumSize(new Dimension(SIDEBAR_W, 38));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(UITheme.GREEN);
        JLabel dbTxt = new JLabel("techops.db  activa");
        dbTxt.setFont(new Font("Consolas", Font.PLAIN, 10));
        dbTxt.setForeground(UITheme.TEXT_GHOST);
        dbRow.add(dot); dbRow.add(dbTxt);
        sidebar.add(dbRow);

        sidebar.add(hDivider());

        // ── Salir ─────────────────────────────────────────────────────
        JButton btnSalir = new JButton("  ⏻   Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSalir.setForeground(UITheme.TEXT_GHOST);
        btnSalir.setBackground(UITheme.BG_DEEPEST);
        btnSalir.setBorder(new EmptyBorder(12, 12, 12, 12));
        btnSalir.setHorizontalAlignment(SwingConstants.LEFT);
        btnSalir.setContentAreaFilled(true);
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSalir.setMaximumSize(new Dimension(SIDEBAR_W, 44));
        btnSalir.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnSalir.setForeground(UITheme.RED); }
            @Override public void mouseExited(MouseEvent e)  { btnSalir.setForeground(UITheme.TEXT_GHOST); }
        });
        btnSalir.addActionListener(e -> salir());
        sidebar.add(btnSalir);

        return sidebar;
    }

    private JToggleButton makeNavBtn(String icon, String label) {
        JToggleButton btn = new JToggleButton(icon + "   " + label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int m = 8;
                if (isSelected()) {
                    g2.setColor(UITheme.ACCENT_DIM);
                    g2.fillRoundRect(m, 3, getWidth()-m*2, getHeight()-6, 8, 8);
                    g2.setColor(UITheme.ACCENT_BLUE);
                    g2.fillRoundRect(0, 6, 4, getHeight()-12, 3, 3);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x161C2C));
                    g2.fillRoundRect(m, 3, getWidth()-m*2, getHeight()-6, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(UITheme.TEXT_DIM);
        btn.setBackground(UITheme.BG_DEEPEST);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 16));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(SIDEBAR_W, 44));
        btn.setPreferredSize(new Dimension(SIDEBAR_W, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.getModel().addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                btn.setForeground(UITheme.TEXT_DIM);
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        });
        return btn;
    }

    private JPanel hDivider() {
        JPanel d = new JPanel();
        d.setBackground(UITheme.BORDER);
        d.setPreferredSize(new Dimension(SIDEBAR_W, 1));
        d.setMaximumSize(new Dimension(SIDEBAR_W, 1));
        d.setMinimumSize(new Dimension(SIDEBAR_W, 1));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }

    // ─────────────────────────────────────────────────────────────────
    // MAIN AREA
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout(0, 0));
        area.setBackground(UITheme.BG_PANEL);
        area.setOpaque(true);

        // ── Top header ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setBackground(UITheme.BG_DARK);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(0, 24, 0, 24)));

        lblCurrentSection = new JLabel("🐛   Incidencias");
        lblCurrentSection.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCurrentSection.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblRight = new JLabel("Área Correctivo  •  Banco Ops");
        lblRight.setFont(new Font("Consolas", Font.PLAIN, 11));
        lblRight.setForeground(UITheme.TEXT_GHOST);

        header.add(lblCurrentSection, BorderLayout.WEST);
        header.add(lblRight,          BorderLayout.EAST);

        // ── Content cards ─────────────────────────────────────────────
        cardLayout   = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(UITheme.BG_PANEL);
        contentCards.setOpaque(true);

        contentCards.add(new IncidenciasPanel(), "INCIDENCIAS");
        contentCards.add(new ServidoresPanel(),  "SERVIDORES");
        contentCards.add(new ContrasenasPanel(), "CONTRASENAS");
        contentCards.add(new NotasPanel(),       "NOTAS");
        contentCards.add(new ComandosPanel(),    "COMANDOS");

        area.add(header,       BorderLayout.NORTH);
        area.add(contentCards, BorderLayout.CENTER);

        return area;
    }

    // ─────────────────────────────────────────────────────────────────
    private void navigateTo(String card, String title) {
        cardLayout.show(contentCards, card);
        lblCurrentSection.setText(title);
        contentCards.revalidate();
        contentCards.repaint();
    }

    private void salir() {
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar TechOps Manager?",
                "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            CryptoUtil.clearSession();
            DatabaseManager.getInstance().close();
            System.exit(0);
        }
    }
}
