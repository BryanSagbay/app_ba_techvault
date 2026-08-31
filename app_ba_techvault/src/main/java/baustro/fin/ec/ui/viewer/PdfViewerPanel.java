package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Vista previa de PDF: renderiza página por página con PDFBox, con navegación Anterior/Siguiente. */
public class PdfViewerPanel extends JPanel implements ViewerCloseable, SearchableViewer {

    private final PDDocument document;
    private final PDFRenderer renderer;
    private final int pageCount;
    private int currentPage = 0;

    private final JLabel imageLabel = new JLabel();
    private final JLabel pageLabel  = new JLabel();
    private JButton btnPrev;
    private JButton btnNext;

    // Búsqueda (Ctrl+F): como cada página se renderiza como imagen, se salta
    // a la página que contiene la coincidencia (extraída con PDFTextStripper).
    private String[] pageTexts;
    private final List<Integer> matchPages = new ArrayList<>();
    private int currentMatch = -1;
    private String lastQuery = "";

    public PdfViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        document = Loader.loadPDF(file);
        renderer = new PDFRenderer(document);
        pageCount = document.getNumberOfPages();

        add(buildNavBar(), BorderLayout.NORTH);
        add(buildImageArea(), BorderLayout.CENTER);

        renderPage(0);
    }

    private JPanel buildNavBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bar.setBackground(UIConstants.BG_CARD);

        btnPrev = navBtn("‹ Anterior");
        btnNext = navBtn("Siguiente ›");
        pageLabel.setFont(UIConstants.FONT_SMALL);
        pageLabel.setForeground(UIConstants.TEXT_2);

        btnPrev.addActionListener(e -> { if (currentPage > 0) renderPage(currentPage - 1); });
        btnNext.addActionListener(e -> { if (currentPage < pageCount - 1) renderPage(currentPage + 1); });

        bar.add(btnPrev);
        bar.add(pageLabel);
        bar.add(btnNext);
        return bar;
    }

    private JScrollPane buildImageArea() {
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIConstants.BG_SURFACE);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrapper.add(imageLabel);

        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private void renderPage(int index) {
        currentPage = index;
        pageLabel.setText("Página " + (index + 1) + " / " + pageCount);
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < pageCount - 1);

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            protected BufferedImage doInBackground() throws Exception {
                return renderer.renderImageWithDPI(index, 120);
            }
            protected void done() {
                try {
                    imageLabel.setIcon(new ImageIcon(get()));
                } catch (Exception ignored) {
                    // si falla una página puntual, se deja el contenido anterior visible
                }
            }
        };
        worker.execute();
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.FONT_SMALL);
        b.setForeground(UIConstants.TEXT_2);
        b.setBackground(UIConstants.BG_BASE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    public void closeResources() {
        try { document.close(); } catch (Exception ignored) {}
    }

    /** Extrae el texto de cada página una sola vez (bajo demanda, al buscar por primera vez). */
    private void ensurePageTexts() {
        if (pageTexts != null) return;
        pageTexts = new String[pageCount];
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int i = 0; i < pageCount; i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                pageTexts[i] = stripper.getText(document).toLowerCase();
            }
        } catch (Exception ex) {
            // Si falla la extracción (PDF escaneado/protegido), la búsqueda simplemente no encuentra nada
            pageTexts = new String[pageCount];
        }
    }

    private void recomputeMatches(String query) {
        matchPages.clear();
        currentMatch = -1;
        lastQuery = query;
        if (query == null || query.isBlank()) return;

        ensurePageTexts();
        String q = query.toLowerCase();
        for (int i = 0; i < pageTexts.length; i++) {
            if (pageTexts[i] != null && pageTexts[i].contains(q)) matchPages.add(i);
        }
    }

    @Override
    public boolean findNext(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recomputeMatches(query);
        if (matchPages.isEmpty()) return false;
        currentMatch = (currentMatch + 1) % matchPages.size();
        renderPage(matchPages.get(currentMatch));
        return true;
    }

    @Override
    public boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recomputeMatches(query);
        if (matchPages.isEmpty()) return false;
        currentMatch = (currentMatch - 1 + matchPages.size()) % matchPages.size();
        renderPage(matchPages.get(currentMatch));
        return true;
    }

    @Override public int getMatchCount() { return matchPages.size(); }
    @Override public int getCurrentMatchIndex() { return currentMatch; }

    @Override
    public void clearHighlights() {
        matchPages.clear();
        currentMatch = -1;
        lastQuery = "";
    }
}
