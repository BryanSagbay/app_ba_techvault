package baustro.fin.ec.ui.components;

import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class StyledComponents {

    private StyledComponents() {}

    // Botón primario con icono opcional
    public static JButton primaryButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIConstants.FONT_BODY);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        Color hover = color.brighter();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(color); }
        });
        return btn;
    }

    public static JButton primaryButton(String text) {
        return primaryButton(text, UIConstants.ACCENT_BLUE);
    }

    public static JButton iconTextButton(String text, String iconName, Color color) {
        JButton btn = primaryButton(text, color);
        ImageIcon icon = IconManager.getSmallIcon(iconName);
        if (icon != null && icon.getIconWidth() > 1) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        return btn;
    }

    public static JButton dangerButton(String text) {
        return iconTextButton(text, IconManager.ICON_DELETE, UIConstants.ACCENT_RED);
    }

    public static JButton successButton(String text) {
        return iconTextButton(text, IconManager.ICON_SAVE, UIConstants.ACCENT_GREEN);
    }

    public static JButton addButton(String text) {
        return iconTextButton(text, IconManager.ICON_ADD, UIConstants.ACCENT_BLUE);
    }

    public static JButton editButton(String text) {
        return iconTextButton(text, IconManager.ICON_EDIT, UIConstants.ACCENT_BLUE);
    }

    public static JButton copyButton(String text) {
        return iconTextButton(text, IconManager.ICON_COPY, UIConstants.ACCENT_CYAN);
    }

    public static JButton cancelButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(UIConstants.BG_CARD);
        btn.setForeground(UIConstants.TEXT_PRIMARY);
        btn.setFont(UIConstants.FONT_BODY);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        ImageIcon icon = IconManager.getSmallIcon(IconManager.ICON_CANCEL);
        if (icon != null && icon.getIconWidth() > 1) {
            btn.setIcon(icon);
            btn.setIconTextGap(6);
        }
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(UIConstants.BG_CARD_HOVER);
                btn.setForeground(UIConstants.TEXT_PRIMARY);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UIConstants.BG_CARD);
                btn.setForeground(UIConstants.TEXT_PRIMARY);
            }
        });
        return btn;
    }

    public static JButton iconButton(ImageIcon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setBackground(UIConstants.BG_CARD);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(UIConstants.BG_SURFACE); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(UIConstants.BG_CARD); }
        });
        return btn;
    }

    // Campo de texto
    public static JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField();
        styleTextField(tf);
        return tf;
    }

    public static void styleTextField(JTextField tf) {
        tf.setBackground(UIConstants.BG_SURFACE);
        tf.setForeground(UIConstants.TEXT_PRIMARY);
        tf.setCaretColor(UIConstants.TEXT_PRIMARY);
        tf.setFont(UIConstants.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setBackground(UIConstants.BG_SURFACE);
        pf.setForeground(UIConstants.TEXT_PRIMARY);
        pf.setCaretColor(UIConstants.TEXT_PRIMARY);
        pf.setFont(UIConstants.FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    // TextArea
    public static JTextArea styledTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setBackground(UIConstants.BG_SURFACE);
        ta.setForeground(UIConstants.TEXT_PRIMARY);
        ta.setCaretColor(UIConstants.TEXT_PRIMARY);
        ta.setFont(UIConstants.FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return ta;
    }

    public static JTextArea monoTextArea(int rows, int cols) {
        JTextArea ta = styledTextArea(rows, cols);
        ta.setFont(UIConstants.FONT_MONO);
        return ta;
    }

    // ComboBox
    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(UIConstants.BG_SURFACE);
        cb.setForeground(UIConstants.TEXT_PRIMARY);
        cb.setFont(UIConstants.FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        return cb;
    }

    // Label
    public static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    public static JLabel heading(String text) {
        return label(text, UIConstants.FONT_HEADING, UIConstants.TEXT_PRIMARY);
    }

    public static JLabel muted(String text) {
        return label(text, UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
    }

    // ScrollPane
    public static JScrollPane darkScrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        sp.getVerticalScrollBar().setBackground(UIConstants.BG_CARD);
        sp.getHorizontalScrollBar().setBackground(UIConstants.BG_CARD);
        return sp;
    }

    // Tabla oscura
    public static void styleTable(JTable table) {
        table.setBackground(UIConstants.BG_CARD);
        table.setForeground(UIConstants.TEXT_PRIMARY);
        table.setSelectionBackground(UIConstants.ACCENT_BLUE);
        table.setSelectionForeground(UIConstants.TEXT_BRIGHT);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(UIConstants.BG_BASE);
        header.setForeground(UIConstants.TEXT_SECONDARY);
        header.setFont(UIConstants.FONT_HEADING);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? UIConstants.BG_CARD : UIConstants.BG_CARD_HOVER);
                    c.setForeground(UIConstants.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    // Search bar
    public static JTextField searchBar(String placeholder) {
        JTextField tf = styledTextField(placeholder);
        tf.setPreferredSize(new Dimension(260, 34));
        // Icono de búsqueda si disponible
        return tf;
    }
}