package baustro.fin.ec.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IconManager {

    private static final Map<String, ImageIcon> cache = new HashMap<>();

    // Nombres de iconos — deben existir en src/main/resources/icons/<name>.png
    public static final String ICON_APP = "app";
    public static final String ICON_DASHBOARD = "dashboard";
    public static final String ICON_CORRECTIVO = "correctivo";
    public static final String ICON_SERVIDOR = "servidor";
    public static final String ICON_PASSWORD = "contrasenas";
    public static final String ICON_TAREA = "tarea";
    public static final String ICON_NOTA = "nota";
    public static final String ICON_COMANDO = "comandos";
    public static final String ICON_ADD = "add";
    public static final String ICON_EDIT = "edit";
    public static final String ICON_DELETE = "delete";
    public static final String ICON_COPY = "copy";
    //public static final String ICON_EYE = "eye";
    public static final String ICON_LOCK = "lock";
    //public static final String ICON_SETTINGS = "settings";
    //public static final String ICON_EXPORT = "export";
    //public static final String ICON_FOLDER = "folder";
    public static final String ICON_SEARCH = "search";
    public static final String ICON_SAVE = "save";
    public static final String ICON_CANCEL = "cancel";
    public static final String ICON_MANUAL = "manual";
    public static final String ICON_TRX = "trx";
    public static final String ICON_AZURE = "azure";
    //public static final String ICON_REFRESH = "refresh";
    //public static final String ICON_WARNING = "warning";
    //public static final String ICON_SUCCESS = "success";
    //public static final String ICON_INFO = "info";
    //public static final String ICON_FILTER = "filter";
    //public static final String ICON_EYE_OFF = "eye_off";
    //public static final String ICON_UNLOCK = "unlock";
    //public static final String ICON_IMPORT = "import";

    public static ImageIcon getIcon(String name, int size) {
        String key = name + "_" + size;
        if (cache.containsKey(key)) return cache.get(key);

        // Try loading from resources/icons/
        String[] paths = {
            "/icons/" + name + ".png",
            "/icons/" + name + ".PNG",
        };

        for (String path : paths) {
            try {
                URL url = IconManager.class.getResource(path);
                if (url != null) {
                    ImageIcon raw = new ImageIcon(url);
                    if (raw.getIconWidth() > 0) {
                        Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(scaled);
                        cache.put(key, icon);
                        return icon;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fallback: transparent icon (app still works without icons)
        ImageIcon empty = transparentIcon(size);
        cache.put(key, empty);
        return empty;
    }

    //public static ImageIcon getIcon(String name) { return getIcon(name, 20); }
    public static ImageIcon getNavIcon(String name) { return getIcon(name, 22); }
    public static ImageIcon getSmallIcon(String name) { return getIcon(name, 16); }
    public static ImageIcon getLargeIcon(String name) { return getIcon(name, 32); }

    private static ImageIcon transparentIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        return new ImageIcon(img);
    }
}
