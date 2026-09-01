package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
/**
 * Vista previa de DOCX: recorre párrafos y tablas del documento con Apache POI (XWPF)
 * y los renderiza como texto con negrita/cursiva/tamaño aproximados. No es una
 * réplica pixel-perfect del documento, pero permite leer el contenido sin salir de la app.
 */
public class DocxViewerPanel extends JPanel implements ViewerCloseable, SearchableViewer {

    private final XWPFDocument doc;
    private final TextSearchSupport search;

    public DocxViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        try (FileInputStream fis = new FileInputStream(file)) {
            doc = new XWPFDocument(fis);
        }

        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(UIConstants.BG_CARD);
        pane.setBorder(new EmptyBorder(24, 32, 24, 32));

        renderBody(pane);
        search = new TextSearchSupport(pane);

        JScrollPane sp = new JScrollPane(pane);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        add(sp, BorderLayout.CENTER);
    }

    private void renderBody(JTextPane pane) throws Exception {
        for (IBodyElement el : doc.getBodyElements()) {
            if (el instanceof XWPFParagraph p) {
                appendParagraph(pane, p);
            } else if (el instanceof XWPFTable table) {
                appendTable(pane.getStyledDocument(), table);
            }
        }
    }

    private void appendParagraph(JTextPane pane, XWPFParagraph p) throws Exception {
        StyledDocument sdoc = pane.getStyledDocument();
        if (p.getRuns().isEmpty()) {
            sdoc.insertString(sdoc.getLength(), "\n", plainStyle());
            return;
        }
        for (XWPFRun run : p.getRuns()) {
            for (XWPFPicture pic : run.getEmbeddedPictures()) {
                insertPicture(pane, pic);
            }

            String text = run.getText(0);
            if (text == null) continue;

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, UIConstants.TEXT_1);
            StyleConstants.setBold(attrs, run.isBold());
            StyleConstants.setItalic(attrs, run.isItalic());
            StyleConstants.setUnderline(attrs, run.getUnderline() != null
                    && run.getUnderline() != org.apache.poi.xwpf.usermodel.UnderlinePatterns.NONE);
            int size = run.getFontSize();
            StyleConstants.setFontSize(attrs, size > 0 ? Math.min(size, 28) : 12);
            StyleConstants.setFontFamily(attrs, "Segoe UI");
            sdoc.insertString(sdoc.getLength(), text, attrs);
        }
        sdoc.insertString(sdoc.getLength(), "\n", plainStyle());
    }

    /** Decodifica la imagen incrustada del run y la inserta en el visor, escalada si es muy ancha. */
    private void insertPicture(JTextPane pane, XWPFPicture pic) {
        try {
            byte[] bytes = pic.getPictureData().getData();
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return;

            int maxWidth = 480;
            int w = img.getWidth(), h = img.getHeight();
            if (w > maxWidth) {
                h = Math.round(h * (maxWidth / (float) w));
                w = maxWidth;
            }
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);

            StyledDocument sdoc = pane.getStyledDocument();
            pane.setCaretPosition(sdoc.getLength());
            pane.insertIcon(new ImageIcon(scaled));
            sdoc.insertString(sdoc.getLength(), "\n", plainStyle());
        } catch (Exception ignored) {
            // si la imagen no se puede decodificar, se omite sin romper la vista previa
        }
    }

    private void appendTable(StyledDocument sdoc, XWPFTable table) throws Exception {
        SimpleAttributeSet mono = new SimpleAttributeSet();
        StyleConstants.setFontFamily(mono, "Consolas");
        StyleConstants.setForeground(mono, UIConstants.TEXT_2);

        for (XWPFTableRow row : table.getRows()) {
            StringBuilder line = new StringBuilder();
            for (XWPFTableCell cell : row.getTableCells()) {
                line.append(cell.getText().replace("\n", " ")).append("  |  ");
            }
            sdoc.insertString(sdoc.getLength(), line + "\n", mono);
        }
        sdoc.insertString(sdoc.getLength(), "\n", mono);
    }

    private SimpleAttributeSet plainStyle() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, UIConstants.TEXT_1);
        StyleConstants.setFontFamily(attrs, "Segoe UI");
        StyleConstants.setFontSize(attrs, 12);
        return attrs;
    }

    @Override
    public void closeResources() {
        try { doc.close(); } catch (Exception ignored) {}
    }

    @Override public boolean findNext(String query) { return search.findNext(query); }
    @Override public boolean findPrevious(String query) { return search.findPrevious(query); }
    @Override public int getMatchCount() { return search.getMatchCount(); }
    @Override public int getCurrentMatchIndex() { return search.getCurrentMatchIndex(); }
    @Override public void clearHighlights() { search.clear(); }
    @Override public String getMatchedText() { return search.getMatchedText(); }

}
