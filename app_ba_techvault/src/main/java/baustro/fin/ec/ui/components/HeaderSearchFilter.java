package baustro.fin.ec.ui.components;

import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Barra compacta con:
 *  - SearchBar con icono a la izquierda y texto de ejemplo
 *  - Combos de filtro inline
 *  - Boton limpiar filtros
 * Va en el lado EAST del header de cada panel.
 */
public class HeaderSearchFilter extends JPanel {

    public record ComboConfig(String label, String[] options, String allLabel) {}

    private final JTextField searchField;
    private final String placeholder;
    private final List<JComboBox<String>> combos = new ArrayList<>();
    private JCheckBox contentToggle;
    private Runnable onChange;

    public HeaderSearchFilter(String searchPlaceholder, ComboConfig... filters) {
        this.placeholder = searchPlaceholder;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 0));

        //  SearchBar con icono
        JPanel searchWrap = new JPanel(new BorderLayout(0, 0));
        searchWrap.setBackground(UIConstants.BG_SURFACE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        searchWrap.setPreferredSize(new Dimension(260, 32));

        // Icono busqueda
        JLabel iconLbl = new JLabel();
        iconLbl.setOpaque(true);
        iconLbl.setBackground(UIConstants.BG_SURFACE);
        iconLbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        ImageIcon searchIco = IconManager.getSmallIcon(IconManager.ICON_SEARCH);
        if (searchIco != null && searchIco.getIconWidth() > 1) {
            iconLbl.setIcon(searchIco);
        } else {
            iconLbl.setText("Q");
            iconLbl.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
            iconLbl.setForeground(UIConstants.TEXT_MUTED);
        }

        // Campo
        searchField = new JTextField();
        searchField.setBackground(UIConstants.BG_SURFACE);
        searchField.setForeground(UIConstants.TEXT_MUTED);
        searchField.setCaretColor(UIConstants.TEXT_PRIMARY);
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        searchField.setText(searchPlaceholder);

        // Placeholder behavior
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(searchPlaceholder)) {
                    searchField.setText("");
                    searchField.setForeground(UIConstants.TEXT_PRIMARY);
                }
                searchWrap.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIConstants.ACCENT_BLUE),
                        BorderFactory.createEmptyBorder(0,0,0,0)));
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(searchPlaceholder);
                    searchField.setForeground(UIConstants.TEXT_MUTED);
                }
                searchWrap.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIConstants.BORDER),
                        BorderFactory.createEmptyBorder(0,0,0,0)));
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { if (onChange != null) onChange.run(); }
        });

        searchWrap.add(iconLbl, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        add(searchWrap);

        //  Separador visual
        if (filters.length > 0) {
            JSeparator sep = new JSeparator(JSeparator.VERTICAL);
            sep.setForeground(UIConstants.BORDER);
            sep.setPreferredSize(new Dimension(1, 28));
            add(sep);
        }

        //  Combos de filtro inline
        for (ComboConfig cfg : filters) {
            JLabel lbl = new JLabel(cfg.label() + ":");
            lbl.setFont(UIConstants.FONT_SMALL);
            lbl.setForeground(UIConstants.TEXT_MUTED);
            add(lbl);

            String[] opts = new String[cfg.options().length + 1];
            opts[0] = cfg.allLabel();
            System.arraycopy(cfg.options(), 0, opts, 1, cfg.options().length);

            JComboBox<String> combo = new JComboBox<>(opts);
            combo.setBackground(UIConstants.BG_SURFACE);
            combo.setForeground(UIConstants.TEXT_PRIMARY);
            combo.setFont(UIConstants.FONT_SMALL);
            combo.setPreferredSize(new Dimension(120, 32));
            combo.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
            combo.addActionListener(e -> { if (onChange != null) onChange.run(); });
            combos.add(combo);
            add(combo);
        }

        //  Boton limpiar
        if (filters.length > 0) {
            JButton btnClear = new JButton("x");
            btnClear.setFont(UIConstants.FONT_SMALL);
            btnClear.setForeground(UIConstants.TEXT_MUTED);
            btnClear.setBackground(UIConstants.BG_CARD);
            btnClear.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            btnClear.setFocusPainted(false);
            btnClear.setToolTipText("Limpiar filtros");
            btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnClear.addActionListener(e -> clearAll());
            add(btnClear);
        }
    }

    public HeaderSearchFilter onChanged(Runnable r) { this.onChange = r; return this; }

    /**
     * Agrega (una sola vez) un checkbox opcional a la derecha del campo de
     * búsqueda, por ejemplo para activar la búsqueda dentro del contenido
     * de los archivos en vez de solo por nombre.
     */
    public HeaderSearchFilter withToggle(String label) {
        if (contentToggle != null) return this;
        contentToggle = new JCheckBox(label);
        contentToggle.setOpaque(false);
        contentToggle.setFont(UIConstants.FONT_SMALL);
        contentToggle.setForeground(UIConstants.TEXT_MUTED);
        contentToggle.setFocusPainted(false);
        contentToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        contentToggle.addActionListener(e -> { if (onChange != null) onChange.run(); });
        add(contentToggle, 1);
        revalidate();
        repaint();
        return this;
    }

    /** true si el checkbox agregado con withToggle() está marcado. */
    public boolean isToggleSelected() {
        return contentToggle != null && contentToggle.isSelected();
    }

    /** Texto de busqueda real (sin placeholder) */
    public String getQuery() {
        String t = searchField.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }

    /** Valor del combo por indice. "" = "Todos" */
    public String getFilter(int index) {
        if (index >= combos.size()) return "";
        Object sel = combos.get(index).getSelectedItem();
        if (sel == null) return "";
        String s = sel.toString();
        // Si es el primer item (allLabel) retorna ""
        if (combos.get(index).getSelectedIndex() == 0) return "";
        return s;
    }

    public void clearAll() {
        searchField.setText(placeholder);
        searchField.setForeground(UIConstants.TEXT_MUTED);
        combos.forEach(c -> c.setSelectedIndex(0));
        if (contentToggle != null) contentToggle.setSelected(false);
        if (onChange != null) onChange.run();
    }

    public boolean hasActiveFilters() {
        boolean searchActive = !getQuery().isEmpty();
        boolean comboActive  = combos.stream().anyMatch(c -> c.getSelectedIndex() > 0);
        boolean toggleActive = contentToggle != null && contentToggle.isSelected();
        return searchActive || comboActive || toggleActive;
    }
}
