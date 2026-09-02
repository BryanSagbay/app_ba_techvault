package baustro.fin.ec.ui.viewer;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de búsqueda de texto compartida por los visores basados en
 * {@link JTextComponent} (texto plano y DOCX). Encuentra todas las
 * coincidencias de una consulta y selecciona/hace scroll a la actual
 * usando {@code select()} (el mismo mecanismo con el que Swing pinta la
 * selección normal de texto).
 *
 * <p>Nota: NO se usa {@link javax.swing.text.Highlighter} con pintores
 * personalizados (como se hizo antes) porque {@code DefaultHighlightPainter}
 * calcula el rectángulo a pintar en base a la geometría de las vistas, y esa
 * geometría se calcula mal cuando el documento mezcla tamaños de fuente
 * distintos en la misma línea o tiene íconos incrustados (exactamente el
 * caso de DOCX: negritas, encabezados, imágenes). El resultado visible era
 * que se "resaltaba" texto que no correspondía a la coincidencia real,
 * aunque el offset calculado internamente sí era correcto. {@code select()}
 * no tiene ese problema porque usa el mismo pintado nativo carácter por
 * carácter que usa Swing para cualquier selección manual de texto.
 */
public final class TextSearchSupport {

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
     * lo que viene después.
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
        if (matches.isEmpty()) { comp.select(0, 0); return false; }
        current = (current + 1) % matches.size();
        select();
        return true;
    }

    boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) { comp.select(0, 0); return false; }
        current = (current - 1 + matches.size()) % matches.size();
        select();
        return true;
    }

    private void select() {
        int[] m = matches.get(current);
        comp.requestFocusInWindow();
        // select() usa el pintado nativo de selección de Swing: preciso
        // carácter por carácter, sin los problemas de alineación visual
        // que tiene un Highlighter personalizado en texto con estilos mixtos.
        comp.select(m[0], m[1]);
    }

    int getMatchCount() { return matches.size(); }

    int getCurrentMatchIndex() { return current; }

    /** Texto real que corresponde a la coincidencia actual (útil para depurar desalineamientos). */
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
        comp.select(0, 0);
        matches.clear();
        current = -1;
        lastQuery = "";
    }
}