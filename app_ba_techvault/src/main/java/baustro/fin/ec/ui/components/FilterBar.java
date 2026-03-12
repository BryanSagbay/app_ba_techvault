package baustro.fin.ec.ui.components;

import baustro.fin.ec.ui.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Barra de filtros reutilizable con chips clicables y combos.
 * Notifica al panel cuando cambia cualquier filtro via onChange.
 */
public class FilterBar extends JPanel {

    public static class FilterChip extends JToggleButton {
        private final String value;

        public FilterChip(String label, String value, Color activeColor) {
            super(label);
            this.value = value;
            setFont(UIConstants.FONT_SMALL);
            setForeground(UIConstants.TEXT_SECONDARY);
            setBackground(UIConstants.BG_CARD);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(true);
            setOpaque(true);

            addItemListener(e -> {
                if (isSelected()) {
                    setBackground(activeColor);
                    setForeground(Color.WHITE);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(activeColor),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                } else {
                    setBackground(UIConstants.BG_CARD);
                    setForeground(UIConstants.TEXT_SECONDARY);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UIConstants.BORDER),
                            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
                }
            });
        }

        public String getValue() { return value; }
    }

    // ── Combo filter (dropdown) ─────────────────────────────────
    public static class FilterCombo extends JPanel {
        private final JComboBox<String> combo;
        private final String allLabel;

        public FilterCombo(String label, String[] options, String allLabel) {
            this.allLabel = allLabel;
            setBackground(UIConstants.BG_DARK);
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

            JLabel lbl = new JLabel(label + ":");
            lbl.setFont(UIConstants.FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_MUTED);

            String[] opts = new String[options.length + 1];
            opts[0] = allLabel;
            System.arraycopy(options, 0, opts, 1, options.length);

            combo = new JComboBox<>(opts);
            combo.setBackground(UIConstants.BG_INPUT);
            combo.setForeground(UIConstants.TEXT_PRIMARY);
            combo.setFont(UIConstants.FONT_SMALL);
            combo.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
            combo.setPreferredSize(new Dimension(130, 28));

            add(lbl);
            add(combo);
        }

        public String getSelected() {
            String s = (String) combo.getSelectedItem();
            return (s == null || s.equals(allLabel)) ? "" : s;
        }

        public void addChangeListener(ActionListener l) { combo.addActionListener(l); }
        public JComboBox<String> getCombo() { return combo; }
        public void reset() { combo.setSelectedIndex(0); }
    }

    // ── Sort combo ──────────────────────────────────────────────
    public static class SortCombo extends JPanel {
        private final JComboBox<String> combo;

        public SortCombo(String[] options) {
            setBackground(UIConstants.BG_DARK);
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

            JLabel lbl = new JLabel("Ordenar:");
            lbl.setFont(UIConstants.FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_MUTED);

            combo = new JComboBox<>(options);
            combo.setBackground(UIConstants.BG_INPUT);
            combo.setForeground(UIConstants.TEXT_PRIMARY);
            combo.setFont(UIConstants.FONT_SMALL);
            combo.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
            combo.setPreferredSize(new Dimension(160, 28));

            add(lbl);
            add(combo);
        }

        public String getSelected() { return (String) combo.getSelectedItem(); }
        public void addChangeListener(ActionListener l) { combo.addActionListener(l); }
        public void reset() { combo.setSelectedIndex(0); }
    }

    // ── FilterBar container ─────────────────────────────────────
    private final List<FilterChip>  chips  = new ArrayList<>();
    private final List<FilterCombo> combos = new ArrayList<>();
    private SortCombo sortCombo;
    private Runnable onChange;
    private JLabel activeCountLabel;

    public FilterBar() {
        setBackground(UIConstants.BG_DARK);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
    }

    /** Agrega un grupo de chips de un solo color */
    public FilterBar addChipGroup(String groupLabel, String[] values, Color color) {
        if (groupLabel != null && !groupLabel.isEmpty()) {
            JLabel lbl = new JLabel(groupLabel + ":");
            lbl.setFont(UIConstants.FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_MUTED);
            add(lbl);
        }
        for (String v : values) {
            FilterChip chip = new FilterChip(v, v, color);
            chip.addItemListener(e -> notifyChange());
            chips.add(chip);
            add(chip);
        }
        addSeparator();
        return this;
    }

    /** Agrega chips coloreados por valor (ej: prioridad = rojo/naranja/verde) */
    public FilterBar addColoredChips(String groupLabel, String[] values, Color[] colors) {
        if (groupLabel != null && !groupLabel.isEmpty()) {
            JLabel lbl = new JLabel(groupLabel + ":");
            lbl.setFont(UIConstants.FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_MUTED);
            add(lbl);
        }
        for (int i = 0; i < values.length; i++) {
            Color c = (i < colors.length) ? colors[i] : UIConstants.ACCENT_BLUE;
            FilterChip chip = new FilterChip(values[i], values[i], c);
            chip.addItemListener(e -> notifyChange());
            chips.add(chip);
            add(chip);
        }
        addSeparator();
        return this;
    }

    /** Agrega un combo dropdown */
    public FilterBar addCombo(String label, String[] options, String allLabel) {
        FilterCombo fc = new FilterCombo(label, options, allLabel);
        fc.addChangeListener(e -> notifyChange());
        combos.add(fc);
        add(fc);
        return this;
    }

    /** Agrega combo de ordenamiento */
    public FilterBar addSort(String[] options) {
        sortCombo = new SortCombo(options);
        sortCombo.addChangeListener(e -> notifyChange());
        add(sortCombo);
        return this;
    }

    /** Agrega boton reset */
    public FilterBar addResetButton() {
        JButton reset = new JButton("X Limpiar");
        reset.setFont(UIConstants.FONT_SMALL);
        reset.setForeground(UIConstants.TEXT_MUTED);
        reset.setBackground(UIConstants.BG_CARD);
        reset.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        reset.setFocusPainted(false);
        reset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reset.addActionListener(e -> resetAll());
        add(reset);

        activeCountLabel = new JLabel("");
        activeCountLabel.setFont(UIConstants.FONT_SMALL);
        activeCountLabel.setForeground(UIConstants.ACCENT_BLUE);
        add(activeCountLabel);
        return this;
    }

    public FilterBar onChanged(Runnable r) {
        this.onChange = r;
        return this;
    }

    private void addSeparator() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(UIConstants.BORDER);
        sep.setPreferredSize(new Dimension(1, 22));
        add(sep);
    }

    private void notifyChange() {
        updateActiveCount();
        if (onChange != null) onChange.run();
    }

    private void updateActiveCount() {
        if (activeCountLabel == null) return;
        long active = chips.stream().filter(AbstractButton::isSelected).count()
                + combos.stream().filter(c -> !c.getSelected().isEmpty()).count();
        if (active > 0) {
            activeCountLabel.setText(active + " filtro(s) activo(s)");
        } else {
            activeCountLabel.setText("");
        }
    }

    public void resetAll() {
        chips.forEach(c -> c.setSelected(false));
        combos.forEach(FilterCombo::reset);
        if (sortCombo != null) sortCombo.reset();
        notifyChange();
    }

    // ── Getters para los paneles ────────────────────────────────

    /** Valores de chips seleccionados (ej: ["Alta","Media"]) */
    public List<String> getSelectedChips() {
        List<String> sel = new ArrayList<>();
        chips.stream().filter(AbstractButton::isSelected).forEach(c -> sel.add(c.getValue()));
        return sel;
    }

    /** Valor del combo por indice (0=primero, 1=segundo...) */
    public String getComboValue(int index) {
        if (index >= combos.size()) return "";
        return combos.get(index).getSelected();
    }

    public String getSortValue() {
        return sortCombo != null ? sortCombo.getSelected() : "";
    }

    /** true si algun filtro esta activo */
    public boolean hasActiveFilters() {
        return !getSelectedChips().isEmpty()
                || combos.stream().anyMatch(c -> !c.getSelected().isEmpty());
    }

    /** Filtra una lista segun una funcion de match */
    public static <T> List<T> filter(List<T> data, java.util.function.Predicate<T> predicate) {
        return data.stream().filter(predicate).toList();
    }
}
