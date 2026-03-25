package baustro.fin.ec.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el tema de la aplicación (oscuro / claro).
 * Los componentes que dependen del tema se registran como listeners
 * y reciben una notificación cuando el tema cambia.
 */
public final class ThemeManager {

    public enum Theme { DARK, LIGHT }

    private static Theme currentTheme = Theme.DARK;
    private static final List<Runnable> listeners = new ArrayList<>();

    private ThemeManager() {}

    public static Theme getTheme() { return currentTheme; }
    public static boolean isDark()  { return currentTheme == Theme.DARK; }

    public static void setTheme(Theme theme) {
        if (currentTheme == theme) return;
        currentTheme = theme;
        UIConstants.applyTheme(theme);
        listeners.forEach(Runnable::run);
    }

    public static void toggle() {
        setTheme(currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK);
    }

    public static void addChangeListener(Runnable r) { listeners.add(r); }
    public static void removeChangeListener(Runnable r) { listeners.remove(r); }

    /** Recorre toda la jerarquía de componentes y los repinta. */
    public static void repaintAll(Component root) {
        if (root == null) return;
        root.repaint();
        if (root instanceof Container c) {
            for (Component child : c.getComponents()) repaintAll(child);
        }
    }
}
