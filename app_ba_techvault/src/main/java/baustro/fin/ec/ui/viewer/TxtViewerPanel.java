package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Vista previa simple para archivos de texto plano (TXT, LOG, SQL, JSON, CSV, etc.). */
public class TxtViewerPanel extends JPanel implements SearchableViewer {

    private final JTextArea area;
    private final TextSearchSupport search;

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

        area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(UIConstants.FONT_MONO);
        area.setBackground(UIConstants.BG_CARD);
        area.setForeground(UIConstants.TEXT_1);
        area.setCaretColor(UIConstants.TEXT_1);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        search = new TextSearchSupport(area);

        JScrollPane textScroll = new JScrollPane(area);
        textScroll.setBorder(null);
        textScroll.getViewport().setBackground(UIConstants.BG_CARD);

        // Misma "tarjeta" enmarcada que Word/Excel: el texto no toca los bordes del
        // panel, queda dentro de un recuadro con margen sobre el fondo gris del visor.
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.BG_CARD);
        card.setBorder(new LineBorder(UIConstants.BORDER_LINE, 1));
        card.add(textScroll, BorderLayout.CENTER);

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(UIConstants.BG_SURFACE);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));
        page.add(card, BorderLayout.CENTER);

        add(page, BorderLayout.CENTER);
    }

    @Override public boolean findNext(String query) { return search.findNext(query); }
    @Override public boolean findPrevious(String query) { return search.findPrevious(query); }
    @Override public int getMatchCount() { return search.getMatchCount(); }
    @Override public int getCurrentMatchIndex() { return search.getCurrentMatchIndex(); }
    @Override public void clearHighlights() { search.clear(); }
    @Override public String getMatchedText() { return search.getMatchedText(); }

}
