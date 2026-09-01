package baustro.fin.ec.ui.viewer;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de búsqueda de texto compartida por los visores basados en
 * {@link JTextComponent} (texto plano y DOCX). Encuentra todas las
 * coincidencias de una consulta, las resalta en amarillo (la actual en
 * ámbar) y hace scroll hasta la coincidencia activa.
 */
final class TextSearchSupport {

    private static final Highlighter.HighlightPainter PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 235, 59));
    private static final Highlighter.HighlightPainter PAINTER_CURRENT =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 160, 0));

    private final JTextComponent comp;
    private final List<int[]> matches = new ArrayList<>();
    private int current = -1;
    private String lastQuery = "";

    TextSearchSupport(JTextComponent comp) {
        this.comp = comp;
    }

    /**
     * Busca coincidencias comparando carácter por carácter (regionMatches),
     * SIN pasar el texto completo a minúsculas. Esto es a propósito: hacer
     * String.toLowerCase() sobre todo el documento puede cambiar la cantidad
     * de caracteres con ciertos símbolos, desalineando los índices de todo
     * lo que viene después y pintando texto que no tiene nada que ver.
     */
    private void recompute(String query) {
        matches.clear();
        current = -1;
        lastQuery = query;
        if (query == null || query.isBlank()) return;

        String text = comp.getText();
        int qLen = query.length();
        int limit = text.length() - qLen;
        for (int i = 0; i <= limit; i++) {
            if (text.regionMatches(true, i, query, 0, qLen)) {
                matches.add(new int[]{i, i + qLen});
            }
        }
    }

    boolean findNext(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) { comp.getHighlighter().removeAllHighlights(); return false; }
        current = (current + 1) % matches.size();
        applyHighlights();
        return true;
    }

    boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) { comp.getHighlighter().removeAllHighlights(); return false; }
        current = (current - 1 + matches.size()) % matches.size();
        applyHighlights();
        return true;
    }

    private void applyHighlights() {
        Highlighter hl = comp.getHighlighter();
        hl.removeAllHighlights();
        for (int i = 0; i < matches.size(); i++) {
            int[] m = matches.get(i);
            try {
                hl.addHighlight(m[0], m[1], i == current ? PAINTER_CURRENT : PAINTER);
            } catch (BadLocationException ex) {
                // si UNA coincidencia falla, seguimos con las demás en vez de abortar todo el resaltado
            }
        }
        try {
            int[] cur = matches.get(current);
            comp.setCaretPosition(cur[0]);
            Rectangle r = comp.modelToView2D(cur[0]).getBounds();
            if (r != null) comp.scrollRectToVisible(r);
        } catch (BadLocationException ignored) {}
    }

    int getMatchCount() { return matches.size(); }

    int getCurrentMatchIndex() { return current; }

    String getMatchedText() {
        if (current < 0 || current >= matches.size()) return "";
        int[] m = matches.get(current);
        try {
            return comp.getDocument().getText(m[0], m[1] - m[0]);
        } catch (BadLocationException e) {
            return "";
        }
    }

    void clear() {
        comp.getHighlighter().removeAllHighlights();
        matches.clear();
        current = -1;
        lastQuery = "";
    }
}