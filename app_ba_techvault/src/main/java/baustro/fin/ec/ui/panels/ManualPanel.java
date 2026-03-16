package baustro.fin.ec.ui.panels;

import baustro.fin.ec.ui.components.HeaderSearchFilter;
import baustro.fin.ec.util.IconManager;

import baustro.fin.ec.ui.UIConstants;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class ManualPanel extends JPanel {

    // Badge colors por extensión
    private static final Map<String, Color> BADGE_COLOR = new LinkedHashMap<>();
    static {
        BADGE_COLOR.put("PDF",  new Color(0xFF, 0x4D, 0x4D));
        BADGE_COLOR.put("DOCX", new Color(0x4D, 0x9A, 0xFF));
        BADGE_COLOR.put("XLSX", new Color(0x4D, 0xC4, 0x72));
        BADGE_COLOR.put("PPTX", new Color(0xFF, 0x8C, 0x42));
        BADGE_COLOR.put("TXT",  new Color(0xAA, 0x99, 0xFF));
    }

    // Fuentes
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BADGE  = new Font("Segoe UI", Font.BOLD,  10);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  12);

    // Datos
    private JTable table;
    private DefaultTableModel model;
    private HeaderSearchFilter hsf;
    private JLabel lblCounter;
    private final List<File> allFiles = new ArrayList<>();
    private long lastDirSnapshot = -1;

    private static final String MANUAL_PATH =
            "D:/USERS/" + System.getProperty("user.name") + "/Documents/Manuales";

    //
    public ManualPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);
        buildUI();
        loadFiles();
        startFolderWatcher();   // ← detecta archivos nuevos automáticamente
    }

    /**
     * Revisa la carpeta cada 3 s. Si cambió (archivos agregados/eliminados),
     * recarga la tabla automáticamente sin que el usuario tenga que presionar Actualizar.
     */
    private void startFolderWatcher() {
        Timer watcher = new Timer(3000, e -> {
            File dir = new File(MANUAL_PATH);
            if (!dir.exists()) return;
            // Snapshot: suma de lastModified de todos los archivos + cantidad
            File[] files = dir.listFiles(File::isFile);
            if (files == null) return;
            long snapshot = files.length;
            for (File f : files) snapshot += f.lastModified();
            if (snapshot != lastDirSnapshot) {
                lastDirSnapshot = snapshot;
                SwingUtilities.invokeLater(this::loadFiles);
            }
        });
        watcher.setRepeats(true);
        watcher.start();
    }

    //  Construcción de UI
    private void buildUI() {
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    /** Barra superior: título + filtros */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(UIConstants.BG_CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_LINE),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Título con icono
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleGroup.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setForeground(UIConstants.ACCENT);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JLabel title = new JLabel("Manuales");
        title.setFont(FONT_TITLE);
        title.setForeground(UIConstants.TEXT_1);

        ImageIcon ico = IconManager.getIcon(IconManager.ICON_MANUAL, 20);
        if (ico != null) {
            title.setIcon(ico);
            title.setIconTextGap(8);
        }

        titleGroup.add(title);
        titleGroup.add(dot);

        // Filtros
        hsf = new HeaderSearchFilter(
                "Buscar manual...",
                new HeaderSearchFilter.ComboConfig("Tipo",
                        new String[]{"PDF", "DOCX", "XLSX", "PPTX", "TXT"}, "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar",
                        new String[]{"Nombre Z-A","Fecha", "Tamaño"}, "Nombre A-Z")
        ).onChanged(this::applyFilters);

        styleSearchFilter(hsf);

        bar.add(titleGroup, BorderLayout.WEST);
        bar.add(hsf,        BorderLayout.EAST);
        return bar;
    }

    /** Área central con tabla */
    private JPanel buildTableArea() {
        String[] cols = {"", "Nombre", "Tipo", "Tamaño", "Fecha modificación"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleModernTable(table);

        // Renderer personalizado por columna
        table.getColumnModel().getColumn(0).setCellRenderer(new IndexRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new BadgeRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new SizeRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new DateRenderer());

        // Anchos
        table.getColumnModel().getColumn(0).setPreferredWidth(36);
        table.getColumnModel().getColumn(0).setMaxWidth(36);
        table.getColumnModel().getColumn(1).setPreferredWidth(340);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });

        JScrollPane scroll = buildDarkScrollPane(table);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIConstants.BG_SURFACE);
        wrapper.setBorder(new EmptyBorder(12, 16, 0, 16));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    /** Barra inferior: botones + contador */
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.BG_CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_LINE),
                new EmptyBorder(10, 24, 10, 24)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton btnOpen    = accentButton();
        JButton btnRefresh = ghostButton();

        btnOpen.addActionListener(e -> openSelected());
        btnRefresh.addActionListener(e -> loadFiles());

        left.add(btnOpen);
        left.add(btnRefresh);

        lblCounter = new JLabel("0 archivos");
        lblCounter.setFont(FONT_SMALL);
        lblCounter.setForeground(UIConstants.TEXT_3);
        JLabel counter = lblCounter;

        bar.add(left,    BorderLayout.WEST);
        bar.add(counter, BorderLayout.EAST);
        return bar;
    }

    //  Estilos de tabla
    private void styleModernTable(JTable t) {
        t.setBackground(UIConstants.BG_CARD);
        t.setForeground(UIConstants.TEXT_1);
        t.setFont(FONT_BODY);
        t.setRowHeight(42);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(UIConstants.BG_ROW_SEL);
        t.setSelectionForeground(UIConstants.TEXT_1);
        t.setFocusable(false);
        t.setBorder(null);

        // Header
        JTableHeader header = t.getTableHeader();
        header.setBackground(UIConstants.BG_SURFACE);
        header.setForeground(UIConstants.TEXT_2);
        header.setFont(FONT_HEADER);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_LINE));
        header.setDefaultRenderer(new ModernHeaderRenderer());
        header.setReorderingAllowed(false);

        // Hover effect
        table.addMouseMotionListener(new MouseMotionAdapter() {
            int lastHover = -1;
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != lastHover) {
                    lastHover = row;
                    table.repaint();
                }
            }
        });
    }

    private JScrollPane buildDarkScrollPane(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(UIConstants.BORDER_LINE, 1, true));
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        sp.setBackground(UIConstants.BG_SURFACE);

        // Scrollbar vertical
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setBackground(UIConstants.BG_CARD);
        vsb.setUI(new SlimScrollBarUI());

        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    /** Aplica estilo oscuro al HeaderSearchFilter existente */
    private void styleSearchFilter(JPanel p) {
        applyRecursive(p, c -> {
            if (c instanceof JTextField tf) {
                tf.setBackground(UIConstants.BG_SURFACE);
                tf.setForeground(UIConstants.TEXT_1);
                tf.setCaretColor(UIConstants.ACCENT);
                tf.setBorder(new CompoundBorder(
                        new LineBorder(UIConstants.BORDER_LINE, 1, true),
                        new EmptyBorder(4, 10, 4, 10)));
                tf.setFont(FONT_BODY);
            } else if (c instanceof JComboBox<?> cb) {
                cb.setBackground(UIConstants.BG_SURFACE);
                cb.setForeground(UIConstants.TEXT_1);
                cb.setFont(FONT_BODY);
            } else {
                c.setBackground(null);
                c.isOpaque();
            }
        });
    }

    //  Carga y filtros
    private void loadFiles() {
        model.setRowCount(0);
        allFiles.clear();
        File dir = new File(MANUAL_PATH);
        if (!dir.exists()) {
            JOptionPane.showMessageDialog(this,
                    "La carpeta de manuales no existe:\n" + dir.getAbsolutePath());
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) return;

        int idx = 1;
        for (File f : files) {
            if (f.isFile()) {
                allFiles.add(f);
                model.addRow(new Object[]{
                        idx++,
                        f.getName(),
                        getExtension(f.getName()),
                        f.length() / 1024 + " KB",
                        new SimpleDateFormat("dd MMM yyyy  HH:mm")
                                .format(new Date(f.lastModified()))
                });
            }
        }
        updateCounter();
    }

    private void applyFilters() {
        model.setRowCount(0);
        String q    = hsf.getQuery().toLowerCase().trim();
        String tipo = hsf.getFilter(0);
        String sort = hsf.getFilter(1);

        Stream<File> stream = allFiles.stream();
        if (!q.isEmpty())
            stream = stream.filter(f -> f.getName().toLowerCase().contains(q));
        if (tipo != null && !tipo.equalsIgnoreCase("Todos") && !tipo.isBlank())
            stream = stream.filter(f -> getExtension(f.getName()).equalsIgnoreCase(tipo));

        Comparator<File> cmp = switch (sort) {
            case "Fecha" -> Comparator.comparing(File::lastModified).reversed();
            case "Tamaño" -> Comparator.comparing(File::length).reversed();
            case "Nombre Z-A" -> Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER).reversed();
            case null, default -> Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER);
        };

        List<File> res = stream.sorted(cmp).toList();
        int idx = 1;
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy  HH:mm");
        for (File f : res) {
            model.addRow(new Object[]{
                    idx++,
                    f.getName(),
                    getExtension(f.getName()),
                    (f.length() / 1024) + " KB",
                    df.format(new Date(f.lastModified()))
            });
        }
        updateCounter();
    }

    private void updateCounter() {
        if (lblCounter != null) {
            int n = model.getRowCount();
            lblCounter.setText(n + " archivo" + (n != 1 ? "s" : ""));
        }
    }

    private void openSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un manual."); return; }
        String name = table.getValueAt(row, 1).toString();
        File file = new File(MANUAL_PATH + "/" + name);
        try { Desktop.getDesktop().open(file); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo."); }
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i + 1).toUpperCase() : "";
    }

    // Renderers

    /** Número de fila */
    class IndexRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            lbl.setText(v != null ? v.toString() : "");
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_3);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setBackground(rowBg(t, row, sel));
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(0, 4, 0, 4));
            return lbl;
        }
    }

    /** Nombre de archivo */
    class NameRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            lbl.setText(v != null ? v.toString() : "");
            lbl.setFont(FONT_BODY);
            lbl.setForeground(sel ? UIConstants.TEXT_1 : new Color(0xD0, 0xD5, 0xE8));
            lbl.setBackground(rowBg(t, row, sel));
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 8));
            return lbl;
        }
    }

    /** Badge de tipo con color */
    class BadgeRenderer extends JPanel implements TableCellRenderer {
        private String ext = "";
        BadgeRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            ext = v != null ? v.toString() : "";
            setBackground(rowBg(t, row, sel));
            return this;
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (ext.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = BADGE_COLOR.getOrDefault(ext, UIConstants.TEXT_3);
            Color bg   = new Color(base.getRed(), base.getGreen(), base.getBlue(), 28);
            FontMetrics fm = g2.getFontMetrics(FONT_BADGE);
            int tw = fm.stringWidth(ext);
            int bw = tw + 16, bh = 20;
            int x  = (getWidth() - bw) / 2, y = (getHeight() - bh) / 2;
            g2.setColor(bg);
            g2.fillRoundRect(x, y, bw, bh, 6, 6);
            g2.setColor(base);
            g2.drawRoundRect(x, y, bw, bh, 6, 6);
            g2.setFont(FONT_BADGE);
            g2.drawString(ext, x + 8, y + bh - fm.getDescent() - 4);
            g2.dispose();
        }
    }

    /** Tamaño con barra visual */
    class SizeRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            lbl.setText(v != null ? v.toString() : "");
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_2);
            lbl.setHorizontalAlignment(RIGHT);
            lbl.setBackground(rowBg(t, row, sel));
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(0, 0, 0, 12));
            return lbl;
        }
    }

    /** Fecha con formato suave */
    class DateRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            lbl.setText(v != null ? v.toString() : "");
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_2);
            lbl.setBackground(rowBg(t, row, sel));
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            return lbl;
        }
    }

    /** Header elegante */
    static class ModernHeaderRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v,
                                                       boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            lbl.setText(v != null ? v.toString().toUpperCase() : "");
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(UIConstants.TEXT_2);
            lbl.setBackground(UIConstants.BG_SURFACE);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(0, col == 1 ? 12 : 8, 0, 8));
            return lbl;
        }
    }

    // Botones
    private JButton accentButton() {
        JButton btn = new JButton("Abrir") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(UIConstants.ACCENT.darker());
                else if (getModel().isRollover())
                    g2.setColor(UIConstants.ACCENT.brighter());
                else
                    g2.setColor(UIConstants.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SMALL);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    private JButton ghostButton() {
        JButton btn = new JButton("Actualizar") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(0x25, 0x2A, 0x3A)
                        : UIConstants.BG_BASE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UIConstants.BORDER_LINE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SMALL);
        btn.setForeground(UIConstants.TEXT_2);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 32));
        return btn;
    }

    // Scrollbar slim
    private static class SlimScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        protected void configureScrollBarColors() {
            thumbColor     = new Color(0x35, 0x3C, 0x55);
            trackColor     = UIConstants.BG_SURFACE;
            thumbDarkShadowColor = trackColor;
            thumbHighlightColor  = thumbColor;
            thumbLightShadowColor = thumbColor;
        }
        protected JButton createDecreaseButton(int o) { return zeroButton(); }
        protected JButton createIncreaseButton(int o) { return zeroButton(); }
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    // Helpers
    private Color rowBg(JTable t, int row, boolean sel) {
        if (sel) return UIConstants.BG_ROW_SEL;
        Point mouse = t.getMousePosition();
        if (mouse != null && t.rowAtPoint(mouse) == row) return UIConstants.BG_CARD_HOVER;
        return (row % 2 == 0) ? UIConstants.BG_CARD : UIConstants.BG_BASE;
    }

    @FunctionalInterface
    interface ComponentConsumer { void accept(Component c); }

    private void applyRecursive(Container root, ComponentConsumer action) {
        for (Component c : root.getComponents()) {
            action.accept(c);
            if (c instanceof Container ct) applyRecursive(ct, action);
        }
    }
}