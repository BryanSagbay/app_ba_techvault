package baustro.fin.ec.ui;

import java.awt.*;

public final class UIConstants {

    // COLOR PALETTE
    public static final Color BG_ROW_SEL   = new Color(0x1E, 0x35, 0x5C);
    public static final Color ACCENT       = new Color(0x3D, 0x8E, 0xFF);
    public static final Color BORDER_LINE  = new Color(0x25, 0x2A, 0x3A);
    public static final Color TEXT_1       = new Color(0xE8, 0xEA, 0xF0);
    public static final Color TEXT_2       = new Color(0x8A, 0x90, 0xA8);
    public static final Color TEXT_3       = new Color(0x55, 0x5C, 0x78);
    public static final Color ACCENT_BLUE   = new Color(59, 130, 246);
    public static final Color ACCENT_GREEN  = new Color(34, 197, 94);
    public static final Color ACCENT_ORANGE = new Color(249, 115, 22);
    public static final Color ACCENT_RED    = new Color(239, 68, 68);
    public static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    public static final Color ACCENT_CYAN   = new Color(6, 182, 212);
    public static final Color TEXT_PRIMARY  = new Color(241, 245, 249);
    public static final Color TEXT_SECONDARY= new Color(148, 163, 184);
    public static final Color TEXT_MUTED    = new Color(100, 116, 139);
    public static final Color BORDER        = new Color(55, 68, 90);
    public static final Color BG_BASE        = new Color(28, 35, 49);
    public static final Color BG_SURFACE     = new Color(0x13161E);
    public static final Color BG_CARD        = new Color(0x181C26);
    public static final Color BG_CARD_HOVER  = new Color(0x1E2330);
    public static final Color BORDER_SUBTLE  = new Color(0x252A38);
    public static final Color BORDER_ACTIVE  = new Color(0x2E3447);
    public static final Color TEAL_PRIMARY   = new Color(0x00D2C8);
    public static final Color INDIGO         = new Color(0x6C6FFF);
    public static final Color AMBER          = new Color(0xF5A623);
    public static final Color ROSE           = new Color(0xFF4F6D);
    public static final Color EMERALD        = new Color(0x00C87A);
    public static final Color VIOLET         = new Color(0xA855F7);
    public static final Color SKY            = new Color(0x38BDF8);
    public static final Color TEXT_BRIGHT    = new Color(0xF1F3F9);
    public static final Color TEXT_MID       = new Color(0x8891A8);
    public static final Color TEXT_DIM       = new Color(0x4D566B);

    //  FONTS
    public static final Font FONT_HERO       = new Font("Segoe UI", Font.BOLD, 38);
    public static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_CARD_SUB   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_HEADER     = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BTN        = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_PILL       = new Font("Segoe UI", Font.BOLD, 9);
    public static final Font FONT_META       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    // PRIORIDADES
    public static final String[] PRIORIDADES = {"Alta", "Media", "Baja"};

    // ESTADOS CORRECTIVO
    public static final String[] ESTADOS_CORRECTIVO = {"Abierto", "En Progreso", "Resuelto", "Cerrado"};

    // AMBIENTES
    public static final String[] AMBIENTES = {"Producción", "Desarrollo", "QA", "Staging", "DR"};

    // ESTADA TAREA
    public static final String[] ESTADOS_TAREA = {"Pendiente", "En Progreso", "Completada", "Cancelada"};

    // TIPOS SERVIDOR
    public static final String[] TIPOS_SERVIDOR = {"Aplicación", "Base de Datos", "Web", "Proxy", "Balanceador", "Almacenamiento", "Monitoreo", "Otro"};

    // ESTADO SERVIDOR
    public static final String[] ESTADOS_SERVIDOR = {"Activo", "Inactivo", "Mantenimiento"};

    // SO
    public static final String[] SISTEMAS_OPERATIVOS_CMD = {"Linux", "Windows", "Ambos"};

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
            case "Abierto", "Pendiente" -> ACCENT_ORANGE;
            case "En Progreso" -> ACCENT_BLUE;
            case "Cerrado" -> TEXT_MUTED;
            case "Completada","Activo","Resuelto"-> ACCENT_GREEN;
            case "Cancelada","Inactivo","Mantenimiento"-> ACCENT_RED;
            default -> TEXT_SECONDARY;
        };
    }
}
