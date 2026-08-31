package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/** Vista previa de PPTX: renderiza cada diapositiva a imagen con Apache POI (XSLF). */
public class PptxViewerPanel extends JPanel implements ViewerCloseable, SearchableViewer {

    private final XMLSlideShow ppt;
    private final List<XSLFSlide> slides;
    private final Dimension pageSize;
    private int current = 0;

    private final JLabel imageLabel = new JLabel();
    private final JLabel pageLabel  = new JLabel();
    private JButton btnPrev;
    private JButton btnNext;

    // Búsqueda (Ctrl+F): cada diapositiva se renderiza como imagen, así que
    // se salta a la diapositiva que contiene la coincidencia.
    private String[] slideTexts;
    private final List<Integer> matchSlides = new ArrayList<>();
    private int currentMatch = -1;
    private String lastQuery = "";

    public PptxViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        try (FileInputStream fis = new FileInputStream(file)) {
            ppt = new XMLSlideShow(fis);
        }
        slides = ppt.getSlides();
        pageSize = ppt.getPageSize();

        add(buildNavBar(), BorderLayout.NORTH);
        add(buildImageArea(), BorderLayout.CENTER);

        if (!slides.isEmpty()) renderSlide(0);
        else pageLabel.setText("Presentación vacía");
    }

    private JPanel buildNavBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bar.setBackground(UIConstants.BG_CARD);

        btnPrev = navBtn("‹ Anterior");
        btnNext = navBtn("Siguiente ›");
        pageLabel.setFont(UIConstants.FONT_SMALL);
        pageLabel.setForeground(UIConstants.TEXT_2);

        btnPrev.addActionListener(e -> { if (current > 0) renderSlide(current - 1); });
        btnNext.addActionListener(e -> { if (current < slides.size() - 1) renderSlide(current + 1); });

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

    private void renderSlide(int index) {
        current = index;
        pageLabel.setText("Diapositiva " + (index + 1) + " / " + slides.size());
        btnPrev.setEnabled(index > 0);
        btnNext.setEnabled(index < slides.size() - 1);

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            protected BufferedImage doInBackground() {
                int w = Math.max(1, pageSize.width);
                int h = Math.max(1, pageSize.height);
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);
                slides.get(index).draw(g2);
                g2.dispose();
                return img;
            }
            protected void done() {
                try {
                    imageLabel.setIcon(new ImageIcon(get()));
                } catch (Exception ignored) {
                    // si falla una diapositiva puntual, se deja el contenido anterior visible
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
        try { ppt.close(); } catch (Exception ignored) {}
    }

    /** Extrae el texto de cada diapositiva una sola vez (bajo demanda, al buscar por primera vez). */
    private void ensureSlideTexts() {
        if (slideTexts != null) return;
        slideTexts = new String[slides.size()];
        for (int i = 0; i < slides.size(); i++) {
            StringBuilder sb = new StringBuilder();
            for (XSLFShape shape : slides.get(i).getShapes()) {
                if (shape instanceof XSLFTextShape ts) {
                    sb.append(ts.getText()).append('\n');
                }
            }
            slideTexts[i] = sb.toString().toLowerCase();
        }
    }

    private void recomputeMatches(String query) {
        matchSlides.clear();
        currentMatch = -1;
        lastQuery = query;
        if (query == null || query.isBlank()) return;

        ensureSlideTexts();
        String q = query.toLowerCase();
        for (int i = 0; i < slideTexts.length; i++) {
            if (slideTexts[i] != null && slideTexts[i].contains(q)) matchSlides.add(i);
        }
    }

    @Override
    public boolean findNext(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recomputeMatches(query);
        if (matchSlides.isEmpty()) return false;
        currentMatch = (currentMatch + 1) % matchSlides.size();
        renderSlide(matchSlides.get(currentMatch));
        return true;
    }

    @Override
    public boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recomputeMatches(query);
        if (matchSlides.isEmpty()) return false;
        currentMatch = (currentMatch - 1 + matchSlides.size()) % matchSlides.size();
        renderSlide(matchSlides.get(currentMatch));
        return true;
    }

    @Override public int getMatchCount() { return matchSlides.size(); }
    @Override public int getCurrentMatchIndex() { return currentMatch; }

    @Override
    public void clearHighlights() {
        matchSlides.clear();
        currentMatch = -1;
        lastQuery = "";
    }
}
