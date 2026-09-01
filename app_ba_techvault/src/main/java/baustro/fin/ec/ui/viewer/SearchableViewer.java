package baustro.fin.ec.ui.viewer;

/**
 * Implementado por los paneles de vista previa que permiten buscar texto
 * dentro del documento (Ctrl+F). Cada implementación decide cómo se
 * "salta" a la siguiente coincidencia (selección de texto, celda de una
 * tabla, página/diapositiva, etc.).
 */
public interface SearchableViewer {

    /** Busca la siguiente coincidencia de {@code query} y salta a ella. */
    boolean findNext(String query);

    /** Busca la coincidencia anterior de {@code query} y salta a ella. */
    boolean findPrevious(String query);

    /** Total de coincidencias de la última búsqueda. */
    int getMatchCount();

    /** Índice (0-based) de la coincidencia actual, o -1 si no hay ninguna activa. */
    int getCurrentMatchIndex();

    /** Limpia la selección/resaltado y el estado de búsqueda. */
    void clearHighlights();

    /** Texto real que corresponde a la coincidencia actual (para depurar desalineamientos). */
    default String getMatchedText() { return ""; }
}
