package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.TareaDAO;
import baustro.fin.ec.model.Tarea;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Panel de Tareas estilo Kanban.
 * Fixes:
 *  - agrupación por estado corregida (usaba computeIfAbsent con lógica rota)
 *  - botones editar/eliminar visibles con iconos reales de IconManager
 *  - formulario simplificado: solo Descripción (sin Error, Solución, Observaciones)
 */
public class TareaPanel extends JPanel {

    private static final String[] COLUMNS = {"Nuevo", "En Progreso", "Completado"};

    private final TareaDAO dao = new TareaDAO();
    private List<Tarea> allData = new ArrayList<>();

    private final Map<String, JPanel> columnCards  = new LinkedHashMap<>();
    private final Map<String, JLabel> columnCounts = new LinkedHashMap<>();

    private HeaderSearchFilter hsf;
    private JLabel statsLabel;

    public TareaPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        loadData();
    }

    //  BUILD UI

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Tareas");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_TAREA, 22);
        if (ico != null && ico.getIconWidth() > 1) { title.setIcon(ico); title.setIconTextGap(8); }
        titlePane.add(title);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnNew = StyledComponents.addButton("Nueva Tarea");
        btnNew.addActionListener(e -> openForm(null));
        btns.add(btnNew);

        header.add(titlePane, BorderLayout.WEST);
        header.add(btns, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        statsLabel = new JLabel("Cargando...");
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        JPanel board = buildBoard();
        JScrollPane sp = new JScrollPane(board,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_BASE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildBottom() {
        hsf = new HeaderSearchFilter(
                "Buscar tarea, servicio...",
                new HeaderSearchFilter.ComboConfig("Prioridad", UIConstants.PRIORIDADES, "Todas"),
                new HeaderSearchFilter.ComboConfig("Ambiente",  UIConstants.AMBIENTES,   "Todos"),
                new HeaderSearchFilter.ComboConfig("Ordenar", new String[]{
                        "Fecha antigua", "Prioridad Alta", "N Tarea", "Servicio"},
                        "Fecha reciente")
        ).onChanged(this::applyFilters);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bottom.add(hsf, BorderLayout.CENTER);
        return bottom;
    }

    private JPanel buildBoard() {
        JPanel board = new JPanel(new GridLayout(1, COLUMNS.length, 12, 0));
        board.setBackground(UIConstants.BG_BASE);
        board.setBorder(new EmptyBorder(16, 16, 16, 16));
        Color[] accents = {
                UIConstants.ACCENT_BLUE,
                UIConstants.ACCENT_ORANGE,
                UIConstants.ACCENT_GREEN
        };
        for (int i = 0; i < COLUMNS.length; i++) board.add(buildColumn(COLUMNS[i], accents[i]));
        return board;
    }

    private JPanel buildColumn(String status, Color accent) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMinimumSize(new Dimension(240, 400));

        var colHeader = getJPanel(accent);

        JLabel lStatus = new JLabel(status);
        lStatus.setFont(UIConstants.FONT_HEADING);
        lStatus.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel lCount = new JLabel("0");
        lCount.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
        lCount.setForeground(accent);
        lCount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120), 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        columnCounts.put(status, lCount);

        colHeader.add(lStatus, BorderLayout.WEST);
        colHeader.add(lCount, BorderLayout.EAST);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(UIConstants.BG_BASE);
        cardsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        columnCards.put(status, cardsPanel);

        JScrollPane scroll = new JScrollPane(cardsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_SUBTLE));
        scroll.getViewport().setBackground(UIConstants.BG_BASE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(colHeader, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel getJPanel(Color accent) {
        JPanel colHeader = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 10, 10);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        colHeader.setOpaque(false);
        colHeader.setBorder(new EmptyBorder(12, 14, 10, 14));
        colHeader.setPreferredSize(new Dimension(0, 52));
        return colHeader;
    }

    //  TASK CARD

    private JPanel buildTaskCard(Tarea t) {
        Color prioColor = UIConstants.getPrioridadColor(t.getPrioridad());

        var card = getJPanel(t, prioColor);

        //  Top: número + badge prioridad
        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);
        JLabel lNum = new JLabel(nvl(t.getNumeroTarea()));
        lNum.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
        lNum.setForeground(UIConstants.SKY);
        topRow.add(lNum, BorderLayout.WEST);
        topRow.add(makeBadge(nvl(t.getPrioridad()), prioColor), BorderLayout.EAST);

        //  Título
        JLabel lTitle = new JLabel("<html><body style='width:190px'>" + nvl(t.getTitulo()) + "</body></html>");
        lTitle.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD));
        lTitle.setForeground(UIConstants.TEXT_PRIMARY);

        //  Meta: servicio · ambiente
        var metaRow = getJPanel(t);

        //  Bottom: responsable en una fila, fecha + botones en otra
        JLabel lResp = new JLabel(nvl(t.getResponsable()).isBlank() ? "—" : t.getResponsable());
        lResp.setFont(UIConstants.FONT_SMALL); lResp.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel lFecha = new JLabel(nvl(t.getFechaReporte()));
        lFecha.setFont(UIConstants.FONT_SMALL); lFecha.setForeground(UIConstants.TEXT_DIM);

        JButton bEdit = makeIconButton(IconManager.ICON_EDIT,   UIConstants.ACCENT_BLUE, "Editar");
        JButton bDel  = makeIconButton(IconManager.ICON_DELETE, UIConstants.ACCENT_RED,  "Eliminar");
        bEdit.addActionListener(e -> openForm(t));
        bDel.addActionListener(e -> deleteTask(t));

        // fila fecha + botones
        JPanel fechaActions = new JPanel(new BorderLayout(0, 0));
        fechaActions.setOpaque(false);
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        actionRow.setOpaque(false);
        actionRow.add(bEdit); actionRow.add(bDel);
        fechaActions.add(lFecha,    BorderLayout.WEST);
        fechaActions.add(actionRow, BorderLayout.EAST);

        // contenedor vertical: responsable arriba, fecha+botones abajo
        JPanel bottomRow = new JPanel();
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.Y_AXIS));
        bottomRow.setOpaque(false);
        lResp.setAlignmentX(Component.LEFT_ALIGNMENT);
        fechaActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.add(lResp);
        bottomRow.add(Box.createVerticalStrut(2));
        bottomRow.add(fechaActions);

        //  Ensamblar contenido
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(wrapLeft(topRow));
        content.add(Box.createVerticalStrut(4));
        content.add(wrapLeft(lTitle));
        content.add(Box.createVerticalStrut(4));
        content.add(wrapLeft(metaRow));
        content.add(Box.createVerticalStrut(6));
        content.add(wrapLeft(bottomRow));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel getJPanel(Tarea t) {
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        metaRow.setOpaque(false);
        if (!nvl(t.getServicio()).isBlank()) {
            JLabel s = new JLabel(t.getServicio());
            s.setFont(UIConstants.FONT_SMALL); s.setForeground(UIConstants.TEAL_PRIMARY);
            metaRow.add(s);
            JLabel sep = new JLabel("·");
            sep.setFont(UIConstants.FONT_SMALL); sep.setForeground(UIConstants.TEXT_DIM);
            metaRow.add(sep);
        }
        if (!nvl(t.getAmbiente()).isBlank()) {
            JLabel a = new JLabel(t.getAmbiente());
            a.setFont(UIConstants.FONT_SMALL); a.setForeground(UIConstants.INDIGO);
            metaRow.add(a);
        }
        return metaRow;
    }

    private JPanel getJPanel(Tarea t, Color prioColor) {
        JPanel card = new JPanel() {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    public void mouseClicked(MouseEvent e) {
                        // Solo doble clic abre el form desde el panel
                        if (e.getClickCount() == 2) openForm(t);
                    }
                });
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, hovered ? 40 : 18));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.setColor(hovered ? UIConstants.BG_CARD_HOVER : UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.setColor(hovered
                        ? new Color(prioColor.getRed(), prioColor.getGreen(), prioColor.getBlue(), 100)
                        : UIConstants.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 10, 10);
                // barra izquierda de prioridad
                g2.setColor(new Color(prioColor.getRed(), prioColor.getGreen(), prioColor.getBlue(), hovered ? 220 : 160));
                g2.fillRoundRect(0, 0, 4, getHeight() - 4, 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(0, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 8, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 148));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    //  DATA

    private void loadData() {
        try { allData = dao.findAll(); applyFilters(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void applyFilters() {
        String q    = hsf.getQuery().toLowerCase();
        String prio = hsf.getFilter(0);
        String amb  = hsf.getFilter(1);
        String sort = hsf.getFilter(2);

        Stream<Tarea> s = allData.stream()
                // Ocultar tareas Completadas con más de 7 días de antigüedad
                .filter(c -> {
                    if (!"Completado".equals(c.getEstado())) return true;
                    String fr = nvl(c.getFechaReporte());
                    if (fr.isBlank()) return true; // sin fecha → siempre visible
                    try {
                        LocalDate fecha = LocalDate.parse(fr.length() > 10 ? fr.substring(0, 10) : fr);
                        return ChronoUnit.DAYS.between(fecha, LocalDate.now()) <= 7;
                    } catch (Exception ex) { return true; }
                });
        if (!q.isEmpty())    s = s.filter(c -> matches(c, q));
        if (!prio.isEmpty()) s = s.filter(c -> prio.equals(c.getPrioridad()));
        if (!amb.isEmpty())  s = s.filter(c -> amb.equals(c.getAmbiente()));

        Comparator<Tarea> cmp = switch (sort) {
            case "Fecha antigua"  -> Comparator.comparing(c -> nvl(c.getFechaReporte()));
            case "Prioridad Alta" -> Comparator.comparingInt(c -> prioOrd(c.getPrioridad()));
            case "N Tarea"        -> Comparator.comparing(c -> nvl(c.getNumeroTarea()));
            case "Servicio"       -> Comparator.comparing(c -> nvl(c.getServicio()));
            default               -> Comparator.comparing((Tarea c) -> nvl(c.getFechaReporte())).reversed();
        };
        List<Tarea> result = s.sorted(cmp).toList();
        refreshKanban(result);
        updateStats(result);
    }

    private void refreshKanban(List<Tarea> data) {
        // BUG FIX: construir el mapa correctamente.
        // La lógica anterior usaba computeIfAbsent + containsKey que siempre evaluaba false
        // después del compute, mandando todo a "Abierto".
        Map<String, List<Tarea>> byStatus = new LinkedHashMap<>();
        for (String col : COLUMNS) byStatus.put(col, new ArrayList<>());

        for (Tarea t : data) {
            String estado = nvl(t.getEstado());
            // Si el estado no coincide con ninguna columna, va a "Abierto"
            if (!byStatus.containsKey(estado)) estado = "Abierto";
            byStatus.get(estado).add(t);
        }

        SwingUtilities.invokeLater(() -> {
            for (String col : COLUMNS) {
                JPanel panel = columnCards.get(col);
                JLabel count = columnCounts.get(col);
                List<Tarea> tareas = byStatus.get(col);

                panel.removeAll();
                for (Tarea t : tareas) {
                    panel.add(buildTaskCard(t));
                    panel.add(Box.createVerticalStrut(8));
                }
                if (tareas.isEmpty()) {
                    JLabel empty = new JLabel("Sin tareas");
                    empty.setFont(UIConstants.FONT_SMALL);
                    empty.setForeground(UIConstants.TEXT_DIM);
                    empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                    empty.setBorder(new EmptyBorder(24, 0, 0, 0));
                    panel.add(empty);
                }
                count.setText(String.valueOf(tareas.size()));
                panel.revalidate();
                panel.repaint();
            }
        });
    }

    private void updateStats(List<Tarea> data) {
        long nv  = data.stream().filter(c -> "Nuevo".equals(c.getEstado())).count();
        long ep  = data.stream().filter(c -> "En Progreso".equals(c.getEstado())).count();
        long cp  = data.stream().filter(c -> "Completado".equals(c.getEstado())).count();
        long alt = data.stream().filter(c -> "Alta".equals(c.getPrioridad())).count();
        statsLabel.setText(String.format(
                " Total: %d  |  Nuevos: %d  |  En Progreso: %d  |  Completados: %d  |  Prioridad Alta: %d",
                data.size(), nv, ep, cp, alt));
    }

    //  ACCIONES

    private void deleteTask(Tarea t) {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar [" + t.getNumeroTarea() + "] " + t.getTitulo() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(t.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    //  DETALLE

    private void showDetail(Tarea c) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Detalle de Tarea", true);
        d.setSize(640, 480); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_BASE);
        d.setLayout(new BorderLayout());

        JPanel dHeader = new JPanel(new BorderLayout());
        dHeader.setBackground(UIConstants.BG_CARD);
        dHeader.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel dTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        dTitleRow.setOpaque(false);
        dTitleRow.add(makeBadge(nvl(c.getNumeroTarea()), UIConstants.SKY));
        JLabel dTitle = new JLabel(nvl(c.getTitulo()));
        dTitle.setFont(UIConstants.FONT_HEADING); dTitle.setForeground(UIConstants.TEXT_BRIGHT);
        dTitleRow.add(dTitle);

        JPanel dBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        dBadges.setOpaque(false);
        dBadges.add(makeBadge(nvl(c.getEstado()),    UIConstants.getEstadoColor(c.getEstado())));
        dBadges.add(makeBadge(nvl(c.getPrioridad()), UIConstants.getPrioridadColor(c.getPrioridad())));
        dHeader.add(dTitleRow, BorderLayout.WEST);
        dHeader.add(dBadges,   BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG_BASE);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Meta cards: 4 campos en fila
        JPanel metaRow = new JPanel(new GridLayout(1, 4, 10, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        metaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaRow.add(metaCard("Servicio",    nvl(c.getServicio()),     UIConstants.TEAL_PRIMARY));
        metaRow.add(metaCard("Ambiente",    nvl(c.getAmbiente()),     UIConstants.INDIGO));
        metaRow.add(metaCard("Responsable", nvl(c.getResponsable()),  UIConstants.AMBER));
        metaRow.add(metaCard("F. Reporte",  nvl(c.getFechaReporte()), UIConstants.TEXT_MID));
        content.add(metaRow);
        content.add(Box.createVerticalStrut(18));

        if (!nvl(c.getDescripcion()).isBlank())
            content.add(textSection(c.getDescripcion()));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIConstants.BG_BASE);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(UIConstants.BG_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_SUBTLE));
        JButton bEdit  = StyledComponents.editButton("Editar");
        JButton bClose = StyledComponents.cancelButton("Cerrar");
        bEdit.addActionListener(e -> { d.dispose(); openForm(c); });
        bClose.addActionListener(e -> d.dispose());
        footer.add(bEdit); footer.add(bClose);

        d.add(dHeader, BorderLayout.NORTH);
        d.add(scroll,  BorderLayout.CENTER);
        d.add(footer,  BorderLayout.SOUTH);
        d.setVisible(true);
    }

    //  FORMULARIO
    // Campos: N Tarea | Título | Ambiente | Servicio | Estado | Prioridad
    //         Fecha Reporte | Responsable | Descripción

    private void openForm(Tarea existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Nueva Tarea" : "Editar Tarea", true);
        dialog.setSize(680, 500);
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
        JTextField        fTit  = StyledComponents.styledTextField("Descripción breve del problema");
        JComboBox<String> fAmb  = StyledComponents.styledCombo(UIConstants.AMBIENTES);
        JTextField        fSvc  = StyledComponents.styledTextField("Ej: Servicio de pagos");
        JComboBox<String> fEst  = StyledComponents.styledCombo(new String[]{"Nuevo", "En Progreso", "Completado"});
        JComboBox<String> fPri  = StyledComponents.styledCombo(UIConstants.PRIORIDADES);
        JTextField        fFR   = StyledComponents.styledTextField("yyyy-mm-dd");
        JTextField        fResp = StyledComponents.styledTextField("Responsable");
        JTextArea         fDesc = StyledComponents.styledTextArea(5, 20);

        if (existing != null) {
            fNum.setText(existing.getNumeroTarea());
            fTit.setText(existing.getTitulo());
            setCombo(fAmb, existing.getAmbiente());
            fSvc.setText(existing.getServicio());
            setCombo(fEst, existing.getEstado());
            setCombo(fPri, existing.getPrioridad());
            fFR.setText(existing.getFechaReporte());
            fResp.setText(existing.getResponsable());
            fDesc.setText(existing.getDescripcion());
        } else {
            fFR.setText(LocalDate.now().toString());
        }

        int r = 0;
        addRow(form, gbc, r++, "N Tarea *",     fNum,  "Título *",    fTit);
        addRow(form, gbc, r++, "Ambiente",       fAmb,  "Servicio",    fSvc);
        addRow(form, gbc, r++, "Estado",         fEst,  "Prioridad",   fPri);
        addRow(form, gbc, r++, "Fecha Reporte",  fFR,   "Responsable", fResp);
        addFull(form, gbc, r, new JScrollPane(fDesc));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(UIConstants.BG_BASE);
        JButton btnSave   = StyledComponents.successButton("Guardar");
        JButton btnCancel = StyledComponents.cancelButton("Cancelar");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            if (fNum.getText().trim().isEmpty() || fTit.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Número y título son obligatorios."); return;
            }
            Tarea t = existing != null ? existing : new Tarea();
            t.setNumeroTarea(fNum.getText().trim());
            t.setTitulo(fTit.getText().trim());
            t.setAmbiente((String) fAmb.getSelectedItem());
            t.setServicio(fSvc.getText().trim());
            t.setEstado((String) fEst.getSelectedItem());
            t.setPrioridad((String) fPri.getSelectedItem());
            t.setFechaReporte(fFR.getText().trim());
            t.setResponsable(fResp.getText().trim());
            t.setDescripcion(fDesc.getText().trim());
            // Limpiar campos viejos para no romper el DAO
            t.setErrorPresentado("");
            t.setSolucion("");
            t.setObservaciones("");
            t.setFechaSolucion("");
            try {
                if (existing == null) dao.insert(t); else dao.update(t);
                loadData(); dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(btnSave); btnPanel.add(btnCancel);

        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_CARD); sp.setBorder(null);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    //  HELPERS

    /** Botón con icono real de IconManager. Fallback a texto si el icono no carga. */
    private JButton makeIconButton(String iconName, Color hoverColor, String tooltip) {
        JButton btn = new JButton();
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(3, 5, 3, 5));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon icon = IconManager.getIcon(iconName, 14);
        if (icon != null && icon.getIconWidth() > 1) {
            btn.setIcon(icon);
        } else {
            // Fallback legible con texto
            btn.setText(IconManager.ICON_EDIT.equals(iconName) ? "✎" : "✕");
            btn.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD, 13f));
            btn.setForeground(UIConstants.TEXT_SECONDARY);
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(hoverColor);
                if (btn.getIcon() != null) btn.setForeground(hoverColor); // tint no aplica a icon directo
                btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                btn.setForeground(UIConstants.TEXT_SECONDARY);
                btn.repaint();
            }
        });
        return btn;
    }

    private JPanel wrapLeft(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 4));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JLabel makeBadge(String text, Color accent) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 100));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        lbl.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
        lbl.setForeground(accent); lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    private JPanel metaCard(String label, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.setColor(UIConstants.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
                g2.fillRoundRect(0, 0, getWidth() - 2, 3, 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(UIConstants.FONT_SECTION); lbl.setForeground(UIConstants.TEXT_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel val = new JLabel(value.isBlank() ? "—" : value);
        val.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD)); val.setForeground(UIConstants.TEXT_BRIGHT);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl); card.add(Box.createVerticalStrut(4)); card.add(val);
        return card;
    }

    private JPanel textSection(String body) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false); section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        section.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false); titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(UIConstants.SKY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2, 2); g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(3, 16)); bar.setOpaque(false);
        JLabel lTitle = new JLabel("Descripción");
        lTitle.setFont(UIConstants.FONT_HEADING); lTitle.setForeground(UIConstants.TEXT_BRIGHT);
        titleRow.add(bar); titleRow.add(lTitle);

        JTextArea ta = new JTextArea(body);
        ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setFont(UIConstants.FONT_BODY); ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setBackground(UIConstants.BG_CARD); ta.setBorder(new EmptyBorder(12, 14, 12, 14));
        JPanel taWrap = new JPanel(new BorderLayout());
        taWrap.setOpaque(false); taWrap.add(ta, BorderLayout.CENTER);
        taWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        taWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        section.add(titleRow); section.add(Box.createVerticalStrut(6)); section.add(taWrap);
        return section;
    }

    // form helpers
    private void addRow(JPanel p, GridBagConstraints g, int row,
                        String l1, Component c1, String l2, Component c2) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 1; g.weightx = 0; p.add(fl(l1), g);
        g.gridx = 1; g.weightx = .5; p.add(c1, g);
        if (l2 != null) {
            g.gridx = 2; g.weightx = 0; p.add(fl(l2), g);
            g.gridx = 3; g.weightx = .5; p.add(c2, g);
        }
    }

    private void addFull(JPanel p, GridBagConstraints g, int row, Component c) {
        g.gridy = row * 2; g.gridx = 0; g.gridwidth = 4; g.weightx = 1; p.add(fl("Descripción"), g);
        g.gridy = row * 2 + 1; p.add(c, g); g.gridwidth = 1;
    }

    private JLabel fl(String t) {
        JLabel l = new JLabel(t); l.setFont(UIConstants.FONT_SMALL); l.setForeground(UIConstants.TEXT_SECONDARY); return l;
    }

    private void setCombo(JComboBox<String> cb, String v) {
        if (v == null) return;
        for (int i = 0; i < cb.getItemCount(); i++)
            if (v.equals(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }

    private boolean matches(Tarea c, String q) {
        return nv(c.getNumeroTarea(), q) || nv(c.getTitulo(), q)
                || nv(c.getServicio(), q) || nv(c.getDescripcion(), q)
                || nv(c.getAmbiente(), q);
    }

    private boolean nv(String f, String q) { return f != null && f.toLowerCase().contains(q); }
    private String nvl(String s) { return s == null ? "" : s; }
    private int prioOrd(String p) {
        return switch (p != null ? p : "") { case "Alta" -> 0; case "Media" -> 1; default -> 2; };
    }
}