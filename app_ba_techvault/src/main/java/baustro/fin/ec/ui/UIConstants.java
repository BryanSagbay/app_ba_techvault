package baustro.fin.ec.ui;

import java.awt.*;

public final class UIConstants {

    // COLOR PALETTE (Dark professional theme)
    public static final Color BG_DARK       = new Color(28, 35, 49);
    public static final Color BG_PANEL      = new Color(36, 45, 61);
    public static final Color BG_CARD       = new Color(44, 55, 74);
    public static final Color BG_INPUT      = new Color(52, 65, 86);
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
    public static final Color TABLE_ROW_ALT = new Color(40, 50, 68);

    // FONTS
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    //public static final Font FONT_MONO_SM = new Font("Consolas", Font.PLAIN, 11);

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
