package baustro.fin.ec.ui.viewer;

/**
 * Implementado por los paneles de vista previa que mantienen abierto
 * un documento (PDDocument, XWPFDocument, Workbook, XMLSlideShow, etc.)
 * para poder liberarlo cuando se cierra la ventana de vista previa.
 */
public interface ViewerCloseable {
    void closeResources();
}
