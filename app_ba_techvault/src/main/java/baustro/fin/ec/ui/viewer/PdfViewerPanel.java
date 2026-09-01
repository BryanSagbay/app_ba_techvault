package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Vista previa de PDF: renderiza página por página con PDFBox, con navegación Anterior/Siguiente. */
public class PdfViewerPanel extends JPanel implements ViewerCloseable, SearchableViewer {

    /** DPI usado para renderizar cada página como imagen (debe coincidir con renderPage). */
    private static final int RENDER_DPI = 120;
    private static final float SCALE = RENDER_DPI / 72f;
    private static final Color MATCH_COLOR = new Color(255, 235, 59, 130);

    private final PDDocument document;
    private final PDFRenderer renderer;
    private final int pageCount;
    private int currentPage = 0;

    private final JLabel imageLabel = new JLabel();
    private final JLabel pageLabel  = new JLabel();
    private JButton btnPrev;
    private JButton btnNext;

    // Búsqueda (Ctrl+F): como cada página se renderiza como imagen, se salta
    // a la página que contiene la coincidencia y se resaltan en amarillo las
    // apariciones de esa página, usando las coordenadas de texto de PDFBox.
    private String[] pageTexts;
    private List<List<TextPosition>> pagePositions;
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
                return renderer.renderImageWithDPI(index, RENDER_DPI);
            }
            protected void done() {
                try {
                    BufferedImage img = get();
                    paintMatchHighlights(img, index);
                    imageLabel.setIcon(new ImageIcon(img));
                } catch (Exception ignored) {
                    // si falla una página puntual, se deja el contenido anterior visible
                }
            }
        };
        worker.execute();
    }

    /** Pinta en amarillo, sobre la imagen ya renderizada, todas las apariciones de la búsqueda activa en esta página. */
    private void paintMatchHighlights(BufferedImage img, int pageIndex) {
        if (lastQuery == null || lastQuery.isBlank() || pageTexts == null
                || pageIndex >= pageTexts.length || pageTexts[pageIndex] == null) {
            return;
        }
        List<int[]> ranges = findRangesInPage(pageIndex, lastQuery);
        if (ranges.isEmpty()) return;

        List<TextPosition> positions = pagePositions.get(pageIndex);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(MATCH_COLOR);
        for (int[] range : ranges) {
            Rectangle2D.Float rect = unionRect(positions, range[0], range[1]);
            if (rect == null) continue;
            g2.fillRect(
                    Math.round(rect.x * SCALE),
                    Math.round(rect.y * SCALE),
                    Math.max(1, Math.round(rect.width * SCALE)),
                    Math.max(1, Math.round(rect.height * SCALE)));
        }
        g2.dispose();
    }

    private List<int[]> findRangesInPage(int pageIndex, String query) {
        List<int[]> ranges = new ArrayList<>();
        String text = pageTexts[pageIndex];
        String q = query.toLowerCase();
        int idx = 0;
        while ((idx = text.indexOf(q, idx)) >= 0) {
            ranges.add(new int[]{idx, idx + q.length()});
            idx += q.length();
        }
        return ranges;
    }

    /** Rectángulo (en puntos PDF) que envuelve el rango de caracteres [start, end) de una página. */
    private Rectangle2D.Float unionRect(List<TextPosition> positions, int start, int end) {
        Rectangle2D.Float union = null;
        for (int i = start; i < end && i < positions.size(); i++) {
            TextPosition tp = positions.get(i);
            if (tp == null) continue;
            float h = tp.getHeightDir();
            Rectangle2D.Float r = new Rectangle2D.Float(tp.getXDirAdj(), tp.getYDirAdj() - h, tp.getWidthDirAdj(), h);
            union = (union == null) ? r : (Rectangle2D.Float) union.createUnion(r);
        }
        return union;
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

    /** Extrae texto y posiciones de cada página una sola vez (bajo demanda, al buscar por primera vez). */
    private void ensurePageTexts() {
        if (pageTexts != null) return;
        pageTexts = new String[pageCount];
        pagePositions = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            try {
                PositionCapturingStripper stripper = new PositionCapturingStripper();
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                stripper.getText(document);
                pageTexts[i] = stripper.text.toString();
                pagePositions.add(stripper.positions);
            } catch (Exception ex) {
                // Si falla la extracción (PDF escaneado/protegido), esa página simplemente no tendrá resultados
                pageTexts[i] = "";
                pagePositions.add(new ArrayList<>());
            }
        }
    }

    /**
     * Extractor de texto que además guarda, alineada carácter a carácter, la
     * posición de cada uno ({@code null} para espacios/saltos de línea
     * insertados por el propio extractor). Permite luego convertir un rango
     * de coincidencia (inicio, fin) en un rectángulo para resaltar sobre la
     * imagen renderizada de la página.
     */
    private static class PositionCapturingStripper extends PDFTextStripper {
        final StringBuilder text = new StringBuilder();
        final List<TextPosition> positions = new ArrayList<>();

        PositionCapturingStripper() throws IOException { super(); }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) {
            int n = Math.min(string.length(), textPositions.size());
            for (int i = 0; i < n; i++) {
                text.append(Character.toLowerCase(string.charAt(i)));
                positions.add(textPositions.get(i));
            }
            for (int i = n; i < string.length(); i++) {
                text.append(Character.toLowerCase(string.charAt(i)));
                positions.add(null);
            }
        }

        @Override
        protected void writeWordSeparator() {
            text.append(' ');
            positions.add(null);
        }

        @Override
        protected void writeLineSeparator() {
            text.append('\n');
            positions.add(null);
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
        if (matchPages.isEmpty()) { renderPage(currentPage); return false; }
        currentMatch = (currentMatch + 1) % matchPages.size();
        renderPage(matchPages.get(currentMatch));
        return true;
    }

    @Override
    public boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recomputeMatches(query);
        if (matchPages.isEmpty()) { renderPage(currentPage); return false; }
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
