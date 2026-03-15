package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.ProductionDAO;
import baustro.fin.ec.model.Production;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ProductionPanel extends JPanel {

    private final ProductionDAO dao = new ProductionDAO();

    private DefaultTableModel tableModel;
    private JTable table;
    private HeaderSearchFilter hsf;
    private JLabel statsLabel;

    private List<Production> allData = new ArrayList<>();

    public ProductionPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        loadData();
    }

    private void buildUI() {

        // HEADER
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);

        JLabel title = new JLabel("Producción");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        ImageIcon ico = IconManager.getIcon(IconManager.ICON_CORRECTIVO, 22);
        if (ico != null && ico.getIconWidth() > 1) {
            title.setIcon(ico);
            title.setIconTextGap(8);
        }
        titlePane.add(title);

        // BOTONES
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setOpaque(false);

        JButton btnNew    = StyledComponents.addButton("Nuevo");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnView   = StyledComponents.iconTextButton("Ver Detalle", IconManager.ICON_INFO, UIConstants.ACCENT_PURPLE);
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");

        btnNew.addActionListener(e -> openForm(null));
        btnEdit.addActionListener(e -> editSelected());
        btnView.addActionListener(e -> viewSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        headerButtons.add(btnNew);
        headerButtons.add(btnEdit);
        headerButtons.add(btnView);
        headerButtons.add(btnDelete);

        header.add(titlePane, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        // BUSCADOR
        hsf = new HeaderSearchFilter(
                "Buscar N tarea, servicio, error...",
                new HeaderSearchFilter.ComboConfig("Estado",   UIConstants.ESTADOS_CORRECTIVO, "Todos"),
                new HeaderSearchFilter.ComboConfig("Prioridad", UIConstants.PRIORIDADES,       "Todas"),
                new HeaderSearchFilter.ComboConfig("Ambiente",  UIConstants.AMBIENTES,         "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar", new String[]{
                        "Fecha antigua", "Prioridad Alta", "Estado", "N Tarea", "Servicio"},
                        "Fecha reciente")
        ).onChanged(this::applyFilters);

        // STATS BAR
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));

        statsLabel = new JLabel("Cargando...");
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        // TABLA
        String[] cols = {"#", "N Tarea", "Titulo", "Servicio", "Ambiente", "Prioridad", "Estado", "Fecha"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        StyledComponents.styleTable(table);
        table.setRowHeight(36);

        int[] widths = {40, 110, 250, 130, 90, 90, 100, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(40);

        // Renderer de badge para Prioridad (col 5) y Estado (col 6)
        table.getColumnModel().getColumn(5).setCellRenderer(badgeRenderer("prioridad"));
        table.getColumnModel().getColumn(6).setCellRenderer(badgeRenderer("estado"));

        // Renderer estilizado para N Tarea (col 1)
        table.getColumnModel().getColumn(1).setCellRenderer(codigoRenderer());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        // BOTTOM - BUSCADOR
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bottom.add(hsf, BorderLayout.CENTER);

        // CENTER
        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table), BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // RENDERERS

    /**
     * Badge pill con fondo semitransparente del color del estado/prioridad.
     * Un solo JLabel custom: pinta el fondo de la celda + la pill encima.
     */
    private DefaultTableCellRenderer badgeRenderer(String type) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {

                final String text   = val != null ? val.toString() : "";
                final Color accent  = type.equals("prioridad")
                        ? UIConstants.getPrioridadColor(text)
                        : UIConstants.getEstadoColor(text);
                final Color cellBg  = sel ? UIConstants.ACCENT_BLUE
                        : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER);

                JLabel lbl = new JLabel(text) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // 1. Fondo completo de la celda
                        g2.setColor(cellBg);
                        g2.fillRect(0, 0, getWidth(), getHeight());

                        // 2. Pill centrada
                        int ph = 20, pw = Math.max(getFontMetrics(getFont()).stringWidth(text) + 22, 54);
                        int px = (getWidth()  - pw) / 2;
                        int py = (getHeight() - ph) / 2;

                        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), sel ? 55 : 32));
                        g2.fillRoundRect(px, py, pw, ph, ph, ph);

                        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), sel ? 160 : 100));
                        g2.setStroke(new BasicStroke(0.9f));
                        g2.drawRoundRect(px, py, pw - 1, ph - 1, ph, ph);

                        g2.dispose();
                        super.paintComponent(g);
                    }
                };

                lbl.setText(text);
                lbl.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                lbl.setForeground(sel ? Color.WHITE : accent);
                lbl.setOpaque(false);           // nosotros pintamos el fondo
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(new EmptyBorder(0, 4, 0, 4));
                return lbl;
            }
        };
    }

    /**
     * Renderer para la columna N Tarea — texto en color acento con prefijo pill.
     */
    private DefaultTableCellRenderer codigoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(sel ? UIConstants.ACCENT_BLUE
                        : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER));
                setForeground(sel ? Color.WHITE : UIConstants.SKY);
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(new EmptyBorder(0, 10, 0, 8));
                return this;
            }
        };
    }

    // DATA

    private void loadData() {
        try {
            allData = dao.findAll();
            applyFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void applyFilters() {
        String q      = hsf.getQuery().toLowerCase();
        String estado = hsf.getFilter(0);
        String prio   = hsf.getFilter(1);
        String amb    = hsf.getFilter(2);
        String sort   = hsf.getFilter(3);

        Stream<Production> s = allData.stream();
        if (!q.isEmpty())      s = s.filter(c -> matches(c, q));
        if (!estado.isEmpty()) s = s.filter(c -> estado.equals(c.getEstado()));
        if (!prio.isEmpty())   s = s.filter(c -> prio.equals(c.getPrioridad()));
        if (!amb.isEmpty())    s = s.filter(c -> amb.equals(c.getAmbiente()));

        Comparator<Production> cmp = switch (sort) {
            case "Fecha antigua"  -> Comparator.comparing(c -> nvl(c.getFechaReporte()));
            case "Prioridad Alta" -> Comparator.comparingInt(c -> prioOrd(c.getPrioridad()));
            case "Estado"         -> Comparator.comparing(c -> nvl(c.getEstado()));
            case "N Tarea"        -> Comparator.comparing(c -> nvl(c.getNumeroTarea()));
            case "Servicio"       -> Comparator.comparing(c -> nvl(c.getServicio()));
            default               -> Comparator.comparing((Production c) -> nvl(c.getFechaReporte())).reversed();
        };

        List<Production> result = s.sorted(cmp).toList();
        refreshTable(result);
        updateStats(result);
    }

    private boolean matches(Production c, String q) {
        return nv(c.getNumeroTarea(), q) || nv(c.getTitulo(), q)
                || nv(c.getServicio(), q) || nv(c.getErrorPresentado(), q)
                || nv(c.getSolucion(), q) || nv(c.getAmbiente(), q);
    }

    private boolean nv(String f, String q) { return f != null && f.toLowerCase().contains(q); }
    private String  nvl(String s)          { return s == null ? "" : s; }
    private int prioOrd(String p) {
        return switch (p != null ? p : "") { case "Alta" -> 0; case "Media" -> 1; default -> 2; };
    }

    private void refreshTable(List<Production> data) {
        tableModel.setRowCount(0);
        int i = 1;
        for (Production c : data)
            tableModel.addRow(new Object[]{
                    i++, c.getNumeroTarea(), c.getTitulo(),
                    c.getServicio(), c.getAmbiente(),
                    c.getPrioridad(), c.getEstado(), c.getFechaReporte()
            });
    }

    private void updateStats(List<Production> data) {
        long ab  = data.stream().filter(c -> "Abierto".equals(c.getEstado())).count();
        long ep  = data.stream().filter(c -> "En Progreso".equals(c.getEstado())).count();
        long res = data.stream().filter(c -> "Resuelto".equals(c.getEstado())).count();
        long alt = data.stream().filter(c -> "Alta".equals(c.getPrioridad())).count();
        statsLabel.setText(String.format(
                " Total: %d | Abiertos: %d | En Progreso: %d | Resueltos: %d | Prioridad Alta: %d",
                data.size(), ab, ep, res, alt));
    }

    // SELECTION HELPERS

    private Production getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un registro."); return null; }
        String num = (String) tableModel.getValueAt(row, 1);
        return allData.stream().filter(c -> num.equals(c.getNumeroTarea())).findFirst().orElse(null);
    }

    private void editSelected()   { Production c = getSelected(); if (c != null) openForm(c); }
    private void viewSelected()   { Production c = getSelected(); if (c != null) showDetail(c); }

    private void deleteSelected() {
        Production c = getSelected();
        if (c == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Eliminar [" + c.getNumeroTarea() + "] " + c.getTitulo() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    // DETALLE

    private void showDetail(Production c) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Detalle del Producción", true);
        d.setSize(720, 620);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_BASE);
        d.setLayout(new BorderLayout());

        //  HEADER del diálogo
        JPanel dHeader = new JPanel(new BorderLayout());
        dHeader.setBackground(UIConstants.BG_CARD);
        dHeader.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel dTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        dTitleRow.setOpaque(false);

        // Pill de número de tarea
        JLabel pillNum = makePill(nvl(c.getNumeroTarea()));
        JLabel dTitle  = new JLabel(nvl(c.getTitulo()));
        dTitle.setFont(UIConstants.FONT_HEADING);
        dTitle.setForeground(UIConstants.TEXT_BRIGHT);

        dTitleRow.add(pillNum);
        dTitleRow.add(dTitle);

        // Badges de estado y prioridad en la misma fila
        JPanel dBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dBadges.setOpaque(false);
        dBadges.add(makeBadgeLabel(nvl(c.getEstado()),    UIConstants.getEstadoColor(c.getEstado())));
        dBadges.add(makeBadgeLabel(nvl(c.getPrioridad()), UIConstants.getPrioridadColor(c.getPrioridad())));

        dHeader.add(dTitleRow, BorderLayout.WEST);
        dHeader.add(dBadges,   BorderLayout.EAST);

        //  CONTENT: cards de metadatos + secciones de texto
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG_BASE);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Fila única de metadatos — siempre 5 cards en una sola fila
        JPanel metaRow = new JPanel(new GridLayout(1, 5, 10, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        metaRow.add(metaCard("Servicio",    nvl(c.getServicio()),     UIConstants.TEAL_PRIMARY));
        metaRow.add(metaCard("Ambiente",    nvl(c.getAmbiente()),     UIConstants.INDIGO));
        metaRow.add(metaCard("Responsable", nvl(c.getResponsable()),  UIConstants.AMBER));
        metaRow.add(metaCard("F. Reporte",  nvl(c.getFechaReporte()), UIConstants.TEXT_MID));
        metaRow.add(metaCard("F. Solución", nvl(c.getFechaSolucion()), UIConstants.EMERALD));

        content.add(metaRow);

        content.add(Box.createVerticalStrut(18));

        // Secciones de texto largo
        if (!nvl(c.getDescripcion()).isBlank())
            content.add(textSection("Descripción",     IconManager.ICON_FOLDER,    c.getDescripcion(),       UIConstants.SKY));

        if (!nvl(c.getErrorPresentado()).isBlank())
            content.add(textSection("Error Presentado", IconManager.ICON_WARNING, c.getErrorPresentado(),   UIConstants.ROSE));

        if (!nvl(c.getSolucion()).isBlank())
            content.add(textSection("Solución Aplicada", IconManager.ICON_SUCCESS, c.getSolucion(),         UIConstants.EMERALD));

        if (!nvl(c.getObservaciones()).isBlank())
            content.add(textSection("Observaciones",   IconManager.ICON_FILTER,    c.getObservaciones(),     UIConstants.AMBER));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_BASE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        //  FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(UIConstants.BG_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_SUBTLE));

        JButton btnEdit  = StyledComponents.editButton("Editar");
        JButton btnClose = StyledComponents.cancelButton("Cerrar");
        btnEdit.addActionListener(e -> { d.dispose(); openForm(c); });
        btnClose.addActionListener(e -> d.dispose());
        footer.add(btnEdit);
        footer.add(btnClose);

        d.add(dHeader, BorderLayout.NORTH);
        d.add(scroll,  BorderLayout.CENTER);
        d.add(footer,  BorderLayout.SOUTH);
        d.setVisible(true);
    }

    /** Tarjeta pequeña de meta dato con label + valor. */
    private JPanel metaCard(String label, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.setColor(UIConstants.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 10, 10);
                // Accent top stripe
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
                g2.fillRoundRect(0, 0, getWidth() - 2, 3, 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(UIConstants.FONT_SECTION);
        lbl.setForeground(UIConstants.TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value.isBlank() ? "—" : value);
        val.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD));
        val.setForeground(UIConstants.TEXT_BRIGHT);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(val);
        return card;
    }

    /** Sección de texto largo con icono real, barra de acento y área de texto estilizada. */
    private JPanel textSection(String title, String iconName, String body, Color accent) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        section.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Título de sección
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Barra de acento vertical
        JPanel bar = getJPanel(accent);

        // Icono real tintado con el color del acento
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(UIConstants.FONT_HEADING);
        lTitle.setForeground(UIConstants.TEXT_BRIGHT);
        lTitle.setIconTextGap(7);
        ImageIcon ico = IconManager.getIcon(iconName, 16);
        if (ico != null && ico.getIconWidth() > 1) {
            // Tintamos el icono con el color del acento para coherencia visual
            lTitle.setIcon(tintIcon(ico, accent));
        }

        titleRow.add(bar);
        titleRow.add(lTitle);

        // Área de texto del cuerpo
        JTextArea ta = new JTextArea(body);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(UIConstants.FONT_BODY);
        ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setBackground(UIConstants.BG_CARD);
        ta.setBorder(new EmptyBorder(12, 14, 12, 14));
        ta.setOpaque(true);

        // Wrap en panel con borde
        JPanel taWrap = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);
                g2.dispose();
            }
        };
        taWrap.setOpaque(false);
        taWrap.add(ta, BorderLayout.CENTER);
        taWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        taWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        section.add(titleRow);
        section.add(Box.createVerticalStrut(6));
        section.add(taWrap);
        return section;
    }

    private JPanel getJPanel(Color accent) {
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2, 2);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(3, 16));
        bar.setOpaque(false);
        return bar;
    }

    /** Pill compacto (para número de tareas en header). */
    private JLabel makePill(String text) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(UIConstants.SKY.getRed(), UIConstants.SKY.getGreen(), UIConstants.SKY.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(UIConstants.SKY.getRed(), UIConstants.SKY.getGreen(), UIConstants.SKY.getBlue(), 100));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(UIConstants.SKY);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(4, 12, 4, 12));
        return lbl;
    }

    /** Badge pequeño (para estado/prioridad en header del diálogo). */
    private JLabel makeBadgeLabel(String text, Color accent) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 100));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
        lbl.setForeground(accent);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        return lbl;
    }

    // FORMULARIO

    private void openForm(Production existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nuevo Tarea de Producción" : "Editar Tarea de Producción", true);
        dialog.setSize(720, 660);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField        fNum  = StyledComponents.styledTextField("Ej: INC-2024-001");
        JTextField        fTit  = StyledComponents.styledTextField("Descripción breve");
        JTextArea         fDesc = StyledComponents.styledTextArea(3, 20);
        JComboBox<String> fAmb  = StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JTextField        fSvc  = StyledComponents.styledTextField("Ej: Servicio de pagos");
        JTextArea         fErr  = StyledComponents.styledTextArea(4, 20);
        JTextArea         fSol  = StyledComponents.styledTextArea(4, 20);
        JComboBox<String> fEst  = StyledComponents.styledCombo(UIConstants.ESTADOS_CORRECTIVO);
        JComboBox<String> fPri  = StyledComponents.styledCombo(UIConstants.PRIORIDADES);
        JTextField        fFR   = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField        fFS   = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField        fResp = StyledComponents.styledTextField("Responsable");
        JTextArea         fObs  = StyledComponents.styledTextArea(3, 20);

        if (existing != null) {
            fNum.setText(existing.getNumeroTarea());  fTit.setText(existing.getTitulo());
            fDesc.setText(existing.getDescripcion()); setCombo(fAmb, existing.getAmbiente());
            fSvc.setText(existing.getServicio());     fErr.setText(existing.getErrorPresentado());
            fSol.setText(existing.getSolucion());     setCombo(fEst, existing.getEstado());
            setCombo(fPri, existing.getPrioridad());  fFR.setText(existing.getFechaReporte());
            fFS.setText(existing.getFechaSolucion()); fResp.setText(existing.getResponsable());
            fObs.setText(existing.getObservaciones());
        } else {
            fFR.setText(LocalDate.now().toString());
        }

        int r = 0;
        addRow(form, gbc, r++, "N Tarea *", fNum,  "Titulo *", fTit);
        addRow(form, gbc, r++, "Ambiente",  fAmb,  "Servicio", fSvc);
        addRow(form, gbc, r++, "Estado",    fEst,  "Prioridad", fPri);
        addRow(form, gbc, r++, "Fecha Reporte", fFR, "Fecha Solución", fFS);
        addRow(form, gbc, r++, "Responsable", fResp, null, null);
        addFull(form, gbc, r++, "Descripción",       new JScrollPane(fDesc));
        addFull(form, gbc, r++, "Error presentado",  new JScrollPane(fErr));
        addFull(form, gbc, r++, "Solución aplicada", new JScrollPane(fSol));
        addFull(form, gbc, r,   "Observaciones",     new JScrollPane(fObs));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_BASE);
        JButton btnSave   = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (fNum.getText().trim().isEmpty() || fTit.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Número y título son obligatorios."); return;
            }
            Production cas = existing != null ? existing : new Production();
            cas.setNumeroTarea(fNum.getText().trim());   cas.setTitulo(fTit.getText().trim());
            cas.setDescripcion(fDesc.getText().trim());  cas.setAmbiente((String) fAmb.getSelectedItem());
            cas.setServicio(fSvc.getText().trim());      cas.setErrorPresentado(fErr.getText().trim());
            cas.setSolucion(fSol.getText().trim());      cas.setEstado((String) fEst.getSelectedItem());
            cas.setPrioridad((String) fPri.getSelectedItem()); cas.setFechaReporte(fFR.getText().trim());
            cas.setFechaSolucion(fFS.getText().trim());  cas.setResponsable(fResp.getText().trim());
            cas.setObservaciones(fObs.getText().trim());
            try {
                if (existing == null) dao.insert(cas); else dao.update(cas);
                loadData(); dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        sp.setBorder(null);
        dialog.add(sp,       BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // FORM HELPERS

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        String l1, Component c1, String l2, Component c2) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 1; g.weightx = 0; p.add(fl(l1), g);
        g.gridx = 1; g.weightx = .5; p.add(c1, g);
        if (l2 != null) {
            g.gridx = 2; g.weightx = 0; p.add(fl(l2), g);
            g.gridx = 3; g.weightx = .5; p.add(c2, g);
        }
    }

    private void addFull(JPanel p, GridBagConstraints g, int row, String label, Component c) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 4; g.weightx = 1; p.add(fl(label), g);
        g.gridy = row * 2 + 1; p.add(c, g); g.gridwidth = 1;
    }

    private JLabel fl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UIConstants.FONT_SMALL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private void setCombo(JComboBox<String> cb, String v) {
        if (v == null) return;
        for (int i = 0; i < cb.getItemCount(); i++)
            if (v.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }

    /**
     * Aplica un tinte de color sobre un ImageIcon usando AlphaComposite SRC_ATOP.
     * El icono original se dibuja primero y luego se superpone el color con alpha 160.
     */
    private ImageIcon tintIcon(ImageIcon src, Color tint) {
        int w = src.getIconWidth(), h = src.getIconHeight();
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src.getImage(), 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));
        g2.setColor(tint);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        return new ImageIcon(img);
    }
}