package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Vista previa simple para archivos de texto plano (TXT, LOG, SQL, JSON, etc.). */
public class TxtViewerPanel extends JPanel {

    public TxtViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        String text;
        try {
            text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Archivo no UTF-8: se intenta con Latin-1 para no fallar la vista previa
            text = Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
        }

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(UIConstants.FONT_MONO);
        area.setBackground(UIConstants.BG_CARD);
        area.setForeground(UIConstants.TEXT_1);
        area.setCaretColor(UIConstants.TEXT_1);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        add(sp, BorderLayout.CENTER);
    }
}
