package baustro.fin.ec.ui.components;

import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Campo de busqueda con icono search.png a la izquierda integrado.
 * Uso: new SearchBar("Buscar...", e -> miMetodo())
 */
public class SearchBar extends JPanel {

    private final JTextField field;

    public SearchBar(String placeholder, KeyAdapter onKey) {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BG_SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        setPreferredSize(new Dimension(280, 34));

        // Icono izquierdo
        JLabel iconLabel = new JLabel();
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
        iconLabel.setBackground(UIConstants.BG_SURFACE);
        iconLabel.setOpaque(true);
        ImageIcon searchIcon = IconManager.getSmallIcon(IconManager.ICON_SEARCH);
        if (searchIcon != null && searchIcon.getIconWidth() > 1) {
            iconLabel.setIcon(searchIcon);
        } else {
            // Fallback: simbolo de lupa en texto
            iconLabel.setText("?");
            iconLabel.setFont(UIConstants.FONT_SMALL);
            iconLabel.setForeground(UIConstants.TEXT_MUTED);
        }

        // Campo de texto
        field = new JTextField();
        field.setBackground(UIConstants.BG_SURFACE);
        field.setForeground(UIConstants.TEXT_PRIMARY);
        field.setCaretColor(UIConstants.TEXT_PRIMARY);
        field.setFont(UIConstants.FONT_BODY);
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        field.setOpaque(true);

        // Placeholder gris
        showPlaceholder(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIConstants.TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) showPlaceholder(placeholder);
            }
        });

        if (onKey != null) field.addKeyListener(onKey);

        // Hover border highlight
        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIConstants.ACCENT_BLUE),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            }
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIConstants.BORDER),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
            }
        };
        addMouseListener(hover);
        iconLabel.addMouseListener(hover);

        add(iconLabel, BorderLayout.WEST);
        add(field,     BorderLayout.CENTER);
    }

    private void showPlaceholder(String text) {
        field.setText(text);
        field.setForeground(UIConstants.TEXT_MUTED);
    }

    /** Devuelve el texto real (sin placeholder) */
    public String getQuery() {
        String t = field.getText().trim();
        return t.equals(field.getToolTipText()) ? "" : t;
    }

    public String getText() { return field.getText(); }
    public void setText(String t) { field.setText(t); }
    public JTextField getField() { return field; }

    /** Limpia el campo */
    public void clear(String placeholder) {
        showPlaceholder(placeholder);
    }
}
