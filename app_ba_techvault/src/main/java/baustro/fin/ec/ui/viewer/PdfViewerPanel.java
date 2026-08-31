package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Vista previa de PDF: renderiza página por página con PDFBox, con navegación Anterior/Siguiente. */
public class PdfViewerPanel extends JPanel implements ViewerCloseable {

    private final PDDocument document;
    private final PDFRenderer renderer;
    private final int pageCount;
    private int currentPage = 0;

    private final JLabel imageLabel = new JLabel();
    private final JLabel pageLabel  = new JLabel();
    private JButton btnPrev;
    private JButton btnNext;

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
}
