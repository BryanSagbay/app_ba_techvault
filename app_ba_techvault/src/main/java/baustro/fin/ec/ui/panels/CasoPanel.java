package baustro.fin.ec.ui.panels;

import baustro.fin.ec.dao.CasoDAO;
import baustro.fin.ec.model.Caso;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.*;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class CasoPanel extends JPanel {

    //  Áreas predefinidas
    private static final String[] AREAS = {
        "CAJAS", "CUENTAS"
    };

    //  Estado
    private final CasoDAO          dao       = new CasoDAO();
    private       DefaultTableModel tableModel;
    private       JTable            table;
    private       HeaderSearchFilter hsf;
    private       JLabel            statsLabel;
    private       List<Caso>        allData   = new ArrayList<>();

    public CasoPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_BASE);
        buildUI();
        loadData();
    }

    //  CONSTRUCCIÓN DE UI
    private void buildUI() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildBottom(),  BorderLayout.SOUTH);
    }

    /** Barra superior: título + botones de acción */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(UIConstants.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 16)));

        // Título
        JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePane.setOpaque(false);
        JLabel title = new JLabel("Soporte Cajas");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon ico = IconManager.getIcon(IconManager.ICON_CASO, 22);
        if (ico != null && ico.getIconWidth() > 1) { title.setIcon(ico); title.setIconTextGap(8); }
        titlePane.add(title);

        // Botones
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPane.setOpaque(false);
        JButton btnNew    = StyledComponents.addButton("Nuevo Caso");
        JButton btnEdit   = StyledComponents.editButton("Editar");
        JButton btnView   = StyledComponents.iconTextButton("Ver Detalle", IconManager.ICON_INFO, UIConstants.ACCENT_PURPLE);
        JButton btnDelete = StyledComponents.dangerButton("Eliminar");

        btnNew   .addActionListener(e -> openForm(null));
        btnEdit  .addActionListener(e -> editSelected());
        btnView  .addActionListener(e -> viewSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        btnPane.add(btnNew);
        btnPane.add(btnEdit);
        btnPane.add(btnView);
        btnPane.add(btnDelete);

        header.add(titlePane, BorderLayout.WEST);
        header.add(btnPane,   BorderLayout.EAST);
        return header;
    }

    /** Área central: barra de estadísticas + tabla */
    private JPanel buildCenter() {
        // Stats bar
        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        statsBar.setBackground(UIConstants.BG_SURFACE);
        statsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        statsLabel = new JLabel("Cargando...");
        statsLabel.setFont(UIConstants.FONT_SMALL);
        statsLabel.setForeground(UIConstants.TEXT_MUTED);
        statsBar.add(statsLabel);

        // Tabla
        String[] cols = {"#", "N° Caso", "Área", "Descripción", "Solución", "Script", "Registrado"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        StyledComponents.styleTable(table);

        // Anchos de columna
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(0).setMinWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        table.getColumnModel().getColumn(4).setPreferredWidth(220);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);

        // Renderer de área (badge de color)
        table.getColumnModel().getColumn(2).setCellRenderer(areaRenderer());

        // Doble click = editar
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.add(statsBar,                                 BorderLayout.NORTH);
        center.add(StyledComponents.darkScrollPane(table),   BorderLayout.CENTER);
        return center;
    }

    /** Barra inferior: filtros de búsqueda */
    private JPanel buildBottom() {
        hsf = new HeaderSearchFilter(
                "Buscar por N° caso, descripción, script, área...",
                new HeaderSearchFilter.ComboConfig("Área",   AREAS,                                         "Todas"),
                new HeaderSearchFilter.ComboConfig("Orden",  new String[]{"Más antiguo primero"},           "Más reciente primero")
        ).onChanged(this::applyFilters);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UIConstants.BG_BASE);
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        bottom.add(hsf, BorderLayout.CENTER);
        return bottom;
    }

    //  DATOS
    private void loadData() {
        try {
            allData = dao.findAll();   // ya viene ordenado por created_at DESC
            applyFilters();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando datos: " + ex.getMessage());
        }
    }

    private void applyFilters() {
        String q    = hsf.getQuery().toLowerCase().trim();
        String area = hsf.getFilter(0);
        String ord  = hsf.getFilter(1);

        var res = getCasos(q, area, ord);

        tableModel.setRowCount(0);
        int i = 1;
        for (Caso c : res) {
            tableModel.addRow(new Object[]{
                i++,
                nvl(c.getNumeroCaso()),
                nvl(c.getArea()),
                truncate(nvl(c.getDescripcion()), 80),
                truncate(nvl(c.getSolucion()),    60),
                truncate(nvl(c.getScript()),       50),
                formatDate(c.getCreatedAt())
            });
        }

        // Stats
        long total = res.size();
        Map<String, Long> porArea = new LinkedHashMap<>();
        for (Caso c : res) {
            String a = nvl(c.getArea());
            if (!a.isBlank()) porArea.merge(a, 1L, Long::sum);
        }
        StringBuilder sb = new StringBuilder("  Total: ").append(total);
        porArea.entrySet().stream().limit(4).forEach(e ->
            sb.append("   |   ").append(e.getKey()).append(": ").append(e.getValue())
        );
        statsLabel.setText(sb.toString());
    }

    private List<Caso> getCasos(String q, String area, String ord) {
        Stream<Caso> s = allData.stream();

        // Búsqueda en número de caso, descripción, script y área
        if (!q.isEmpty()) {
            s = s.filter(c ->
                nv(c.getNumeroCaso(), q) ||
                nv(c.getDescripcion(), q) ||
                nv(c.getScript(), q) ||
                nv(c.getArea(), q)
            );
        }

        // Filtro por área
        if (area != null && !area.isBlank() && !area.equalsIgnoreCase("Todas")) {
            s = s.filter(c -> area.equalsIgnoreCase(c.getArea()));
        }

        // Ordenamiento siempre por número de caso formato YYMMDD-NNNNNN descendente.
        // Comparamos el string completo: como el formato es numérico puro (con guión),
        // el orden lexicográfico descendente equivale al orden numérico descendente.
        List<Caso> res = s.sorted(
            Comparator.comparing((Caso c) -> caseNumberSortKey(c.getNumeroCaso())).reversed()
        ).toList();

        if ("Más antiguo primero".equals(ord)) {
            res = new ArrayList<>(res);
            Collections.reverse(res);
        }
        return res;
    }

    /**
     * Genera una clave de ordenamiento normalizada para el número de caso.
     * Formato esperado: YYMMDD-NNNNNN (ej: 260408-000101).
     * Elimina caracteres no alfanuméricos y devuelve el string resultante,
     * que al ser ordenado lexicográficamente produce el orden cronológico correcto.
     */
    private String caseNumberSortKey(String numeroCaso) {
        if (numeroCaso == null || numeroCaso.isBlank()) return "";
        // Quitar todo excepto dígitos para comparación numérica pura
        return numeroCaso.replaceAll("[^0-9]", "");
    }

    //  ACCIONES CRUD
    private Caso getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un caso.");
            return null;
        }
        String num = tableModel.getValueAt(row, 1).toString();
        return allData.stream()
                .filter(c -> num.equals(c.getNumeroCaso()))
                .findFirst().orElse(null);
    }

    private void editSelected()   { Caso c = getSelected(); if (c != null) openForm(c); }

    private void deleteSelected() {
        Caso c = getSelected(); if (c == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar caso [" + c.getNumeroCaso() + "]?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    private void viewSelected() {
        Caso c = getSelected(); if (c == null) return;
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Detalle: " + c.getNumeroCaso(), true);
        d.setSize(700, 540); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);

        JTextArea ta = StyledComponents.monoTextArea(14, 70);
        ta.setEditable(false);
        ta.setBackground(UIConstants.BG_BASE);
        ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String sep = "═".repeat(58) + "\n";
        ta.setText(sep
            + "  CASO : " + nvl(c.getNumeroCaso()) + "\n"
            + "  ÁREA : " + nvl(c.getArea())       + "\n"
            + "  FECHA: " + formatDate(c.getCreatedAt()) + "\n"
            + sep
            + "\n DESCRIPCIÓN\n"
            + nvl(c.getDescripcion()) + "\n\n"
            + " SOLUCIÓN\n"
            + nvl(c.getSolucion()) + "\n\n"
            + " SCRIPT\n"
            + nvl(c.getScript()) + "\n"
            + sep);

        d.add(StyledComponents.darkScrollPane(ta));
        d.setVisible(true);
    }

    //  FORMULARIO (nuevo / editar)
    private void openForm(Caso existing) {
        boolean isNew = (existing == null);
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isNew ? "Nuevo Caso de Soporte" : "Editar Caso de Soporte", true);
        d.setSize(740, 580);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(UIConstants.BG_CARD);
        d.setLayout(new BorderLayout());

        //  Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(22, 28, 12, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(5, 6, 5, 6);

        JTextField         fNum  = StyledComponents.styledTextField("Ej: INC-2024-001, TKT-8823...");
        JComboBox<String>  fArea = StyledComponents.styledCombo(AREAS);
        JTextArea          fDesc = StyledComponents.styledTextArea(4, 40);
        JTextArea          fSol  = StyledComponents.styledTextArea(4, 40);
        JTextArea          fScr  = StyledComponents.styledTextArea(4, 40);

        if (!isNew) {
            fNum.setText(existing.getNumeroCaso());
            selectCombo(fArea, existing.getArea());
            fDesc.setText(existing.getDescripcion());
            fSol .setText(existing.getSolucion());
            fScr .setText(existing.getScript());
        }

        int row = 0;

        // Fila 1: Número de caso + Área (misma fila)
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.weightx = 0;
        form.add(label("N° Caso *"), g);
        g.gridx = 1; g.weightx = .5;
        form.add(fNum, g);
        g.gridx = 2; g.weightx = 0;
        form.add(label("Área"), g);
        g.gridx = 3; g.weightx = .5;
        form.add(fArea, g);
        row++;

        // Fila 2: Descripción (full width)
        addFullRow(form, g, row++, "Descripción",        new JScrollPane(fDesc));
        addFullRow(form, g, row++, "Solución",           new JScrollPane(fSol));
        addFullRow(form, g, row,   "Script / Comando",   new JScrollPane(fScr));

        styleScrollPanes(fDesc, fSol, fScr);

        //  Botones
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bp.setBackground(UIConstants.BG_BASE);
        JButton bSave   = StyledComponents.successButton("Guardar");
        JButton bCancel = StyledComponents.cancelButton("Cancelar");

        bCancel.addActionListener(e -> d.dispose());
        bSave  .addActionListener(e -> {
            if (fNum.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(d, "El número de caso es obligatorio.");
                return;
            }
            Caso c = isNew ? new Caso() : existing;
            c.setNumeroCaso(fNum.getText().trim());
            c.setArea      (Objects.requireNonNullElse((String) fArea.getSelectedItem(), ""));
            c.setDescripcion(fDesc.getText().trim());
            c.setSolucion  (fSol .getText().trim());
            c.setScript    (fScr .getText().trim());
            try {
                if (isNew) dao.insert(c); else dao.update(c);
                loadData();
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error al guardar: " + ex.getMessage());
            }
        });

        bp.add(bSave);
        bp.add(bCancel);

        JScrollPane sp = new JScrollPane(form);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        sp.setBorder(null);

        d.add(sp, BorderLayout.CENTER);
        d.add(bp, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    //  RENDERER DE ÁREA
    private static final Map<String, Color> AREA_COLOR = new LinkedHashMap<>();
    static {
        AREA_COLOR.put("ATM",          new Color(0xFF, 0x8C, 0x42));
        AREA_COLOR.put("CORE",         new Color(0x4D, 0x9A, 0xFF));
        AREA_COLOR.put("WEB",          new Color(0x4D, 0xC4, 0x72));
        AREA_COLOR.put("MÓVIL",        new Color(0xAA, 0x99, 0xFF));
        AREA_COLOR.put("BASE DE DATOS",new Color(0xFF, 0x4D, 0x4D));
        AREA_COLOR.put("REDES",        new Color(0xFF, 0xD7, 0x00));
        AREA_COLOR.put("SEGURIDAD",    new Color(0xFF, 0x6B, 0x6B));
        AREA_COLOR.put("MIDDLEWARE",   new Color(0x00, 0xBF, 0xD8));
        AREA_COLOR.put("REPORTES",     new Color(0x7B, 0xC8, 0x7B));
    }

    private DefaultTableCellRenderer areaRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String area = val != null ? val.toString() : "";
                Color base  = AREA_COLOR.getOrDefault(area, UIConstants.TEXT_MUTED);
                setForeground(base);
                setBackground(sel ? UIConstants.ACCENT_BLUE
                                  : (row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER));
                setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    // HELPERS
    private void addFullRow(JPanel p, GridBagConstraints g, int row, String lbl, Component c) {
        g.gridy = row * 2;     g.gridx = 0; g.gridwidth = 4; g.weightx = 1;
        p.add(label(lbl), g);
        g.gridy = row * 2 + 1; g.weightx = 1;
        p.add(c, g);
        g.gridwidth = 1;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_SMALL);
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private void selectCombo(JComboBox<String> cb, String v) {
        if (v == null) return;
        for (int i = 0; i < cb.getItemCount(); i++)
            if (v.equalsIgnoreCase(cb.getItemAt(i))) { cb.setSelectedIndex(i); return; }
    }

    private void styleScrollPanes(JTextArea... areas) {
        for (JTextArea ta : areas) {
            Component parent = ta.getParent();
            if (parent instanceof JViewport vp) {
                Component sp = vp.getParent();
                if (sp instanceof JScrollPane scrollPane) {
                    scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
                    scrollPane.getViewport().setBackground(UIConstants.BG_SURFACE);
                }
            }
            ta.setBackground(UIConstants.BG_SURFACE);
            ta.setForeground(UIConstants.TEXT_PRIMARY);
            ta.setCaretColor(UIConstants.ACCENT_BLUE);
        }
    }

    private boolean nv(String field, String q) {
        return field != null && field.toLowerCase().contains(q);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isBlank()) return "";
        // SQLite guarda como "yyyy-MM-dd HH:mm:ss", mostramos compacto
        return raw.length() >= 10 ? raw.substring(0, 16).replace("T", " ") : raw;
    }
}
