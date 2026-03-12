package baustro.fin.ec.ui.components;

import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Campo de busqueda con icono search.png a la izquierda.
 * Uso: new SearchPanel("placeholder", e -> doSearch(e.getText()))
 */
public class SearchPanel extends JPanel {

    private final JTextField textField;

    public SearchPanel(String placeholder, java.util.function.Consumer<String> onSearch) {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BG_INPUT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        setPreferredSize(new Dimension(280, 34));

        // Icono de busqueda (izquierda)
        JLabel iconLabel = new JLabel();
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
        iconLabel.setBackground(UIConstants.BG_INPUT);
        iconLabel.setOpaque(true);
        ImageIcon searchIcon = IconManager.getSmallIcon(IconManager.ICON_SEARCH);
        if (searchIcon != null && searchIcon.getIconWidth() > 1) {
            iconLabel.setIcon(searchIcon);
        } else {
            // fallback texto si no hay icono
            iconLabel.setText("Q");
            iconLabel.setFont(UIConstants.FONT_SMALL.deriveFont(Font.BOLD));
            iconLabel.setForeground(UIConstants.TEXT_MUTED);
        }

        // Campo de texto
        textField = new JTextField();
        textField.setBackground(UIConstants.BG_INPUT);
        textField.setForeground(UIConstants.TEXT_PRIMARY);
        textField.setCaretColor(UIConstants.TEXT_PRIMARY);
        textField.setFont(UIConstants.FONT_BODY);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        textField.setOpaque(true);

        // Placeholder gris
        textField.setForeground(UIConstants.TEXT_MUTED);
        textField.setText(placeholder);
        textField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(UIConstants.TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(UIConstants.TEXT_MUTED);
                }
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = textField.getText();
                if (!q.equals(placeholder)) {
                    onSearch.accept(q);
                }
            }
        });

        add(iconLabel, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
    }

    public String getText() {
        String t = textField.getText();
        // Retorna vacío si es el placeholder
        return t.startsWith("Buscar") || t.startsWith("Search") ? "" : t;
    }

    public void clear() {
        textField.setText("");
    }
}
