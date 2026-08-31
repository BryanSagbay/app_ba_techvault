package baustro.fin.ec.ui.viewer;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de búsqueda de texto compartida por los visores basados en
 * {@link JTextComponent} (texto plano y DOCX). Encuentra todas las
 * coincidencias de una consulta y selecciona/hace scroll a la actual.
 */
final class TextSearchSupport {

    private final JTextComponent comp;
    private final List<int[]> matches = new ArrayList<>();
    private int current = -1;
    private String lastQuery = "";

    TextSearchSupport(JTextComponent comp) {
        this.comp = comp;
    }

    private void recompute(String query) {
        matches.clear();
        current = -1;
        lastQuery = query;
        if (query == null || query.isBlank()) return;

        String text = comp.getText().toLowerCase();
        String q = query.toLowerCase();
        int idx = 0;
        while ((idx = text.indexOf(q, idx)) >= 0) {
            matches.add(new int[]{idx, idx + q.length()});
            idx += q.length();
        }
    }

    boolean findNext(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) return false;
        current = (current + 1) % matches.size();
        select();
        return true;
    }

    boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) return false;
        current = (current - 1 + matches.size()) % matches.size();
        select();
        return true;
    }

    private void select() {
        int[] m = matches.get(current);
        comp.requestFocusInWindow();
        comp.select(m[0], m[1]);
    }

    int getMatchCount() { return matches.size(); }

    int getCurrentMatchIndex() { return current; }

    void clear() {
        comp.select(0, 0);
        matches.clear();
        current = -1;
        lastQuery = "";
    }
}
