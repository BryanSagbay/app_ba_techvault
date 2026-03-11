package baustro.fin.ec.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Tema oscuro centralizado — estilo "ops dashboard" profesional.
 * Todos los colores, fuentes y factories están aquí.
 */
public class UITheme {

    // ── Paleta de colores ────────────────────────────────────────────
    public static final Color BG_DEEPEST   = new Color(0x080C14);
    public static final Color BG_DARK      = new Color(0x0A0E1A);
    public static final Color BG_PANEL     = new Color(0x0F1117);
    public static final Color BG_CARD      = new Color(0x111827);
    public static final Color BG_INPUT     = new Color(0x1A1F2E);
    public static final Color BG_ROW_ALT   = new Color(0x111827);
    public static final Color BG_ROW_SEL   = new Color(0x1E2D4A);
    public static final Color BG_HOVER     = new Color(0x161C2C);

    public static final Color BORDER       = new Color(0x1A2035);
    public static final Color BORDER_INPUT = new Color(0x2A3348);
    public static final Color BORDER_FOCUS = new Color(0x3B82F6);

    public static final Color ACCENT_BLUE  = new Color(0x3B82F6);
    public static final Color ACCENT_LIGHT = new Color(0x60A5FA);
    public static final Color ACCENT_DIM   = new Color(0x1E3A5F);

    public static final Color TEXT_PRIMARY  = new Color(0xE2E8F0);
    public static final Color TEXT_SECOND   = new Color(0x94A3B8);
    public static final Color TEXT_DIM      = new Color(0x475569);
    public static final Color TEXT_GHOST    = new Color(0x334155);

    public static final Color GREEN         = new Color(0x4ADE80);
    public static final Color GREEN_BG      = new Color(0x052E16);
    public static final Color RED           = new Color(0xF87171);
    public static final Color RED_BG        = new Color(0x450A0A);
    public static final Color ORANGE        = new Color(0xFB923C);
    public static final Color ORANGE_BG     = new Color(0x431407);
    public static final Color YELLOW        = new Color(0xFCD34D);
    public static final Color YELLOW_BG     = new Color(0x78350F);
    public static final Color BLUE_SOFT     = new Color(0x93C5FD);
    public static final Color BLUE_BG       = new Color(0x1E3A5F);

    // Prioridades
    public static final Color PRIO_CRITICA_BG   = new Color(0x7F1D1D);
    public static final Color PRIO_CRITICA_FG   = new Color(0xFCA5A5);
    public static final Color PRIO_ALTA_BG      = new Color(0x7C2D12);
    public static final Color PRIO_ALTA_FG      = new Color(0xFDBA74);
    public static final Color PRIO_MEDIA_BG     = new Color(0x78350F);
    public static final Color PRIO_MEDIA_FG     = new Color(0xFCD34D);
    public static final Color PRIO_BAJA_BG      = new Color(0x1E3A5F);
    public static final Color PRIO_BAJA_FG      = new Color(0x93C5FD);

    // Ambientes
    public static final Color AMB_PROD_BG = new Color(0x450A0A);
    public static final Color AMB_PROD_FG = new Color(0xF87171);
    public static final Color AMB_QA_BG   = new Color(0x0C1445);
    public static final Color AMB_QA_FG   = new Color(0x93C5FD);
    public static final Color AMB_DEV_BG  = new Color(0x052E16);
    public static final Color AMB_DEV_FG  = new Color(0x4ADE80);
    public static final Color AMB_HOM_BG  = new Color(0x3D2A00);
    public static final Color AMB_HOM_FG  = new Color(0xFCD34D);

    // ── Fuentes ─────────────────────────────────────────────────────
    public static final Font FONT_UI      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_MONO_B  = new Font("Consolas", Font.BOLD,  13);
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 11);

    // ── Borders ─────────────────────────────────────────────────────
    public static final Border BORDER_INPUT_NORMAL = BorderFactory.createLineBorder(BORDER_INPUT);
    public static final Border BORDER_BOTTOM = BorderFactory.createMatteBorder(0,0,1,0, BORDER);

    // ── Global FlatLaf setup ─────────────────────────────────────────
    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background",            BG_DARK);
        UIManager.put("Frame.background",            BG_DARK);
        UIManager.put("Table.background",            BG_PANEL);
        UIManager.put("Table.foreground",            TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground",   BG_ROW_SEL);
        UIManager.put("Table.selectionForeground",   TEXT_PRIMARY);
        UIManager.put("Table.gridColor",             BORDER);
        UIManager.put("Table.alternateRowColor",     BG_ROW_ALT);
        UIManager.put("TableHeader.background",      BG_DEEPEST);
        UIManager.put("TableHeader.foreground",      TEXT_DIM);
        UIManager.put("TableHeader.separatorColor",  BORDER);
        UIManager.put("ScrollPane.background",       BG_PANEL);
        UIManager.put("Viewport.background",         BG_PANEL);
        UIManager.put("TextField.background",        BG_INPUT);
        UIManager.put("TextField.foreground",        TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",   ACCENT_BLUE);
        UIManager.put("TextField.border",            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT),
                BorderFactory.createEmptyBorder(6,10,6,10)));
        UIManager.put("TextArea.background",         BG_INPUT);
        UIManager.put("TextArea.foreground",         TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",    ACCENT_BLUE);
        UIManager.put("ComboBox.background",         BG_INPUT);
        UIManager.put("ComboBox.foreground",         TEXT_SECOND);
        UIManager.put("ComboBox.selectionBackground",ACCENT_BLUE);
        UIManager.put("ComboBox.selectionForeground",Color.WHITE);
        UIManager.put("Button.background",           BG_INPUT);
        UIManager.put("Button.foreground",           TEXT_SECOND);
        UIManager.put("CheckBox.background",         BG_PANEL);
        UIManager.put("CheckBox.foreground",         TEXT_SECOND);
        UIManager.put("Label.foreground",            TEXT_SECOND);
        UIManager.put("ToolTip.background",          BG_CARD);
        UIManager.put("ToolTip.foreground",          TEXT_PRIMARY);
        UIManager.put("ToolTip.border",              BorderFactory.createLineBorder(BORDER_INPUT));
        UIManager.put("PopupMenu.background",        BG_CARD);
        UIManager.put("PopupMenu.border",            BorderFactory.createLineBorder(BORDER_INPUT));
        UIManager.put("MenuItem.background",         BG_CARD);
        UIManager.put("MenuItem.foreground",         TEXT_SECOND);
        UIManager.put("MenuItem.selectionBackground",ACCENT_DIM);
        UIManager.put("MenuItem.selectionForeground",TEXT_PRIMARY);
        UIManager.put("SplitPane.background",        BG_DARK);
        UIManager.put("SplitPaneDivider.background", BORDER);
        UIManager.put("TabbedPane.background",       BG_DARK);
        UIManager.put("TabbedPane.foreground",       TEXT_SECOND);
        UIManager.put("List.background",             BG_PANEL);
        UIManager.put("List.foreground",             TEXT_PRIMARY);
        UIManager.put("List.selectionBackground",    BG_ROW_SEL);
        UIManager.put("List.selectionForeground",    TEXT_PRIMARY);
        UIManager.put("PasswordField.background",    BG_INPUT);
        UIManager.put("PasswordField.foreground",    TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT_BLUE);
        UIManager.put("OptionPane.background",       BG_CARD);
        UIManager.put("OptionPane.messageForeground",TEXT_PRIMARY);
        UIManager.put("Separator.foreground",        BORDER);
    }

    // ── Factories ────────────────────────────────────────────────────

    /** JTextField estilizado */
    public static JTextField textField(String placeholder, int cols) {
        JTextField tf = new JTextField(cols);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setFont(FONT_UI);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        return tf;
    }

    /** JTextArea estilizado */
    public static JTextArea textArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(BG_INPUT);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(ACCENT_BLUE);
        ta.setFont(FONT_UI);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return ta;
    }

    /** JTextArea terminal (fondo oscuro, texto verde) */
    public static JTextArea terminalArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(BG_DEEPEST);
        ta.setForeground(GREEN);
        ta.setCaretColor(GREEN);
        ta.setFont(FONT_MONO);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return ta;
    }

    /** JPasswordField estilizado */
    public static JPasswordField passwordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_BLUE);
        pf.setFont(FONT_UI);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        return pf;
    }

    /** JComboBox estilizado */
    public static <T> JComboBox<T> comboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_SECOND);
        cb.setFont(FONT_UI);
        cb.setFocusable(false);
        return cb;
    }

    /** Botón primario azul */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACCENT_BLUE.darker()
                        : getModel().isRollover() ? ACCENT_BLUE.brighter().darker()
                        : ACCENT_BLUE;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_BLUE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    /** Botón secundario (outline) */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? BG_HOVER : BG_INPUT;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.setColor(BORDER_INPUT);
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,8,8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_UI);
        btn.setForeground(TEXT_SECOND);
        btn.setBackground(BG_INPUT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /** Botón peligro (rojo) */
    public static JButton dangerButton(String text) {
        JButton btn = secondaryButton(text);
        btn.setForeground(RED);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(0xFF6B6B)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(RED); }
        });
        return btn;
    }

    /** Label de sección (header de columna en toolbar) */
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);
        return lbl;
    }

    /** Badge con colores de estado/prioridad */
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 9, 2, 9));
        return lbl;
    }

    /** Panel toolbar estándar */
    public static JPanel toolbarPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(BG_PANEL);
        p.setOpaque(true);
        p.setBorder(BORDER_BOTTOM);
        return p;
    }

    /** JScrollPane sin borde visible */
    public static JScrollPane scrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.getVerticalScrollBar().setBackground(BG_PANEL);
        sp.getHorizontalScrollBar().setBackground(BG_PANEL);
        styleScrollBar(sp.getVerticalScrollBar());
        styleScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }

    private static void styleScrollBar(JScrollBar bar) {
        bar.setBackground(BG_PANEL);
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x2A3348);
                trackColor = BG_PANEL;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? new Color(0x3B82F6) : thumbColor);
                g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
                g2.dispose();
            }
        });
    }

    /** Colores de prioridad */
    public static Color[] prioBadgeColors(String prio) {
        return switch (prio == null ? "" : prio) {
            case "CRITICA" -> new Color[]{PRIO_CRITICA_BG, PRIO_CRITICA_FG};
            case "ALTA"    -> new Color[]{PRIO_ALTA_BG,    PRIO_ALTA_FG};
            case "MEDIA"   -> new Color[]{PRIO_MEDIA_BG,   PRIO_MEDIA_FG};
            case "BAJA"    -> new Color[]{PRIO_BAJA_BG,    PRIO_BAJA_FG};
            default        -> new Color[]{BG_CARD, TEXT_DIM};
        };
    }

    /** Colores de estado de incidencia */
    public static Color[] estadoBadgeColors(String estado) {
        return switch (estado == null ? "" : estado) {
            case "ABIERTO"    -> new Color[]{RED_BG,    RED};
            case "EN_PROCESO" -> new Color[]{ORANGE_BG, ORANGE};
            case "CERRADO"    -> new Color[]{GREEN_BG,  GREEN};
            default           -> new Color[]{BG_CARD,   TEXT_DIM};
        };
    }

    /** Colores de ambiente de servidor */
    public static Color[] ambienteBadgeColors(String amb) {
        return switch (amb == null ? "" : amb) {
            case "PROD" -> new Color[]{AMB_PROD_BG, AMB_PROD_FG};
            case "QA"   -> new Color[]{AMB_QA_BG,   AMB_QA_FG};
            case "DEV"  -> new Color[]{AMB_DEV_BG,  AMB_DEV_FG};
            case "HOM"  -> new Color[]{AMB_HOM_BG,  AMB_HOM_FG};
            default     -> new Color[]{BG_CARD,      TEXT_DIM};
        };
    }

    /** TableCellRenderer con badge pill */
    public static TableCellRenderer badgeRenderer(java.util.function.Function<String, Color[]> colorFn) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean sel, boolean focus, int row, int col) {
                String text = value == null ? "" : value.toString();
                Color[] colors = colorFn.apply(text);
                JLabel lbl = badge(text, colors[0], colors[1]);
                lbl.setBackground(sel ? BG_ROW_SEL : (row%2==0 ? BG_PANEL : BG_ROW_ALT));
                lbl.setOpaque(false);
                JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                wrap.setBackground(sel ? BG_ROW_SEL : (row%2==0 ? BG_PANEL : BG_ROW_ALT));
                wrap.add(lbl);
                return wrap;
            }
        };
    }

    /** TableCellRenderer estándar oscuro */
    public static TableCellRenderer darkRenderer() {
        return darkRenderer(SwingConstants.LEFT, FONT_UI, TEXT_PRIMARY);
    }

    public static TableCellRenderer darkRenderer(int align, Font font, Color fg) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                lbl.setHorizontalAlignment(align);
                lbl.setFont(font);
                lbl.setForeground(sel ? TEXT_PRIMARY : fg);
                lbl.setBackground(sel ? BG_ROW_SEL : (row%2==0 ? BG_PANEL : BG_ROW_ALT));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                lbl.setOpaque(true);
                return lbl;
            }
        };
    }

    /** Estiliza una JTable completa */
    public static void styleTable(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(BG_ROW_SEL);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(34);
        table.setFont(FONT_UI);
        table.setFocusable(false);
        table.getTableHeader().setBackground(BG_DEEPEST);
        table.getTableHeader().setForeground(TEXT_DIM);
        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0,BORDER));
        table.getTableHeader().setReorderingAllowed(false);
        // Default renderer for all columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(darkRenderer());
        }
    }

    /** Panel con borde inferior */
    public static JPanel headerBar(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 0));

        if (subtitle != null) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(FONT_SMALL);
            lblSub.setForeground(TEXT_GHOST);
            lblSub.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 24));
            panel.add(lblSub, BorderLayout.EAST);
        }
        panel.add(lblTitle, BorderLayout.WEST);
        return panel;
    }

    /** Línea separadora oscura */
    public static JSeparator darkSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BG_DARK);
        return sep;
    }
}
