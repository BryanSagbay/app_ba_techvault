package baustro.fin.ec.ui;

import java.awt.*;

public final class UIConstants {

    //  SEMANTIC COLORS (mutable – change with theme)
    public static Color BG_ROW_SEL;
    public static Color ACCENT;
    public static Color BORDER_LINE;
    public static Color TEXT_1;
    public static Color TEXT_2;
    public static Color TEXT_3;
    public static Color TEXT_PRIMARY;
    public static Color TEXT_SECONDARY;
    public static Color TEXT_MUTED;
    public static Color BORDER;
    public static Color BG_BASE;
    public static Color BG_SURFACE;
    public static Color BG_CARD;
    public static Color BG_CARD_HOVER;
    public static Color BORDER_SUBTLE;
    public static Color BORDER_ACTIVE;
    public static Color TEXT_BRIGHT;
    public static Color TEXT_MID;
    public static Color TEXT_DIM;

    //  ACCENT COLORS (same in both themes)
    public static final Color ACCENT_BLUE   = new Color(59,  130, 246);
    public static final Color ACCENT_GREEN  = new Color(34,  197,  94);
    public static final Color ACCENT_ORANGE = new Color(249, 115,  22);
    public static final Color ACCENT_RED    = new Color(239,  68,  68);
    public static final Color ACCENT_PURPLE = new Color(139,  92, 246);
    public static final Color ACCENT_CYAN   = new Color(  6, 182, 212);
    public static final Color TEAL_PRIMARY  = new Color(0x00, 0xD2, 0xC8);
    public static final Color INDIGO        = new Color(0x6C, 0x6F, 0xFF);
    public static final Color AMBER         = new Color(0xF5, 0xA6, 0x23);
    public static final Color ROSE          = new Color(0xFF, 0x4F, 0x6D);
    public static final Color EMERALD       = new Color(0x00, 0xC8, 0x7A);
    public static final Color VIOLET        = new Color(0xA8, 0x55, 0xF7);
    public static final Color SKY           = new Color(0x38, 0xBD, 0xF8);

    //  FONTS (unchanged)
    public static final Font FONT_HERO       = new Font("Segoe UI", Font.BOLD,  38);
    public static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_CARD_SUB   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font FONT_PILL       = new Font("Segoe UI", Font.BOLD,   9);
    public static final Font FONT_META       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO       = new Font("Consolas",  Font.PLAIN, 12);

    //  DOMAIN CONSTANTS
    public static final String[] PRIORIDADES          = {"Alta", "Media", "Baja"};
    public static final String[] AMBIENTES            = {"Producción", "Desarrollo", "QA", "Staging", "DR"};
    public static final String[] ESTADOS_TAREA        = {"Pendiente", "En Progreso", "Completada", "Cancelada"};
    public static final String[] TIPOS_SERVIDOR       = {"Aplicación", "Base de Datos", "Web", "Proxy", "Balanceador", "Almacenamiento", "Monitoreo", "Otro"};
    public static final String[] ESTADOS_SERVIDOR     = {"Activo", "Inactivo", "Mantenimiento"};
    public static final String[] SISTEMAS_OPERATIVOS_CMD = {"Linux", "Windows"};

    // Apply dark theme by default at class load
    static { applyTheme(ThemeManager.Theme.DARK); }

    public static void applyTheme(ThemeManager.Theme theme) {
        if (theme == ThemeManager.Theme.DARK) {
            BG_BASE        = new Color(28,  35,  49);
            BG_SURFACE     = new Color(0x13, 0x16, 0x1E);
            BG_CARD        = new Color(0x18, 0x1C, 0x26);
            BG_CARD_HOVER  = new Color(0x1E, 0x23, 0x30);
            BORDER         = new Color(55,  68,  90);
            BORDER_SUBTLE  = new Color(0x25, 0x2A, 0x38);
            BORDER_ACTIVE  = new Color(0x2E, 0x34, 0x47);
            TEXT_PRIMARY   = new Color(241, 245, 249);
            TEXT_SECONDARY = new Color(148, 163, 184);
            TEXT_MUTED     = new Color(100, 116, 139);
            TEXT_BRIGHT    = new Color(0xF1, 0xF3, 0xF9);
            TEXT_MID       = new Color(0x88, 0x91, 0xA8);
            TEXT_DIM       = new Color(0x4D, 0x56, 0x6B);
            BG_ROW_SEL     = new Color(0x1E, 0x35, 0x5C);
            ACCENT         = new Color(0x3D, 0x8E, 0xFF);
            BORDER_LINE    = new Color(0x25, 0x2A, 0x3A);
            TEXT_1         = new Color(0xE8, 0xEA, 0xF0);
            TEXT_2         = new Color(0x8A, 0x90, 0xA8);
            TEXT_3         = new Color(0x55, 0x5C, 0x78);
        } else {
            // LIGHT THEME — colores con contraste suficiente sobre fondos claros
            BG_BASE        = new Color(235, 239, 248);
            BG_SURFACE     = new Color(220, 226, 240);
            BG_CARD        = new Color(255, 255, 255);
            BG_CARD_HOVER  = new Color(243, 246, 255);
            BORDER         = new Color(185, 198, 220);
            BORDER_SUBTLE  = new Color(205, 215, 232);
            BORDER_ACTIVE  = new Color(170, 185, 210);
            // Textos oscuros con contraste alto sobre fondos blancos/claros
            TEXT_PRIMARY   = new Color(15,  25,  50);   // casi negro azulado
            TEXT_SECONDARY = new Color(55,  75, 110);   // azul oscuro legible
            TEXT_MUTED     = new Color(90,  108, 140);  // gris azulado medio — antes era demasiado claro
            TEXT_BRIGHT    = new Color(8,   18,  42);   // negro puro casi
            TEXT_MID       = new Color(60,  80, 118);   // antes (80,100,135) — más oscuro ahora
            TEXT_DIM       = new Color(100, 118, 150);  // antes (160,175,200) — era invisible, ahora visible
            BG_ROW_SEL     = new Color(205, 222, 255);
            ACCENT         = new Color(0x3D, 0x8E, 0xFF);
            BORDER_LINE    = new Color(185, 198, 220);
            TEXT_1         = new Color(15,  25,  50);   // = TEXT_PRIMARY
            TEXT_2         = new Color(55,  75, 110);   // = TEXT_SECONDARY
            TEXT_3         = new Color(90, 108, 140);   // = TEXT_MUTED
        }
    }

    private UIConstants() {}

    public static Color getPrioridadColor(String prioridad) {
        if (prioridad == null) return TEXT_SECONDARY;
        return switch (prioridad) {
            case "Alta"  -> ACCENT_RED;
            case "Media" -> ACCENT_ORANGE;
            case "Baja"  -> ACCENT_GREEN;
            default      -> TEXT_SECONDARY;
        };
    }

    public static Color getEstadoColor(String estado) {
        if (estado == null) return TEXT_SECONDARY;
        return switch (estado) {
            case "Abierto", "Pendiente", "Nuevo" -> ACCENT_BLUE;
            case "En Progreso"                   -> ACCENT_ORANGE;
            case "Cerrado"                       -> TEXT_MUTED;
            case "Completada", "Completado", "Activo", "Resuelto" -> ACCENT_GREEN;
            case "Cancelada", "Inactivo", "Mantenimiento"         -> ACCENT_RED;
            default -> TEXT_SECONDARY;
        };
    }
}