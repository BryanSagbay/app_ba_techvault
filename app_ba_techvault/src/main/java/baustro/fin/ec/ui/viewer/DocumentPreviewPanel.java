package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Panel de vista previa de documentos, embebido dentro de la misma pestaña
 * (Manuales / Scripts) en lugar de abrir una ventana aparte. Incluye un botón
 * "Regresar" para volver al listado de archivos.
 * Soporta PDF, DOCX, XLSX, PPTX, TXT e imágenes comunes (PNG/JPG/GIF/BMP).
 */
public class DocumentPreviewPanel extends JPanel {

    private final JPanel contentHolder = new JPanel(new BorderLayout());
    private JComponent currentViewer;

    // Barra de búsqueda (Ctrl+F) dentro del documento
    private JPanel findBar;
    private JTextField findField;
    private JLabel findCounter;

    /**
     * @param file   documento a previsualizar
     * @param onBack acción a ejecutar cuando el usuario presiona "Regresar"
     */
    public DocumentPreviewPanel(File file, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.setOpaque(false);
        topWrap.add(buildHeader(file, onBack), BorderLayout.NORTH);
        topWrap.add(buildFindBar(), BorderLayout.SOUTH);
        add(topWrap, BorderLayout.NORTH);

        contentHolder.setOpaque(true);
        contentHolder.setBackground(UIConstants.BG_SURFACE);
        add(contentHolder, BorderLayout.CENTER);

        registerFindShortcut();

        showLoading();
        loadAsync(file);
    }

    /** Ctrl+F abre la barra de búsqueda, sin importar qué componente interno tenga el foco. */
    private void registerFindShortcut() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "openFind");
        am.put("openFind", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { showFindBar(); }
        });
    }

    private JPanel buildHeader(File file, Runnable onBack) {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(UIConstants.BG_CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_LINE),
                new EmptyBorder(12, 24, 12, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JButton btnBack = ghostBtn("‹ Regresar");
        btnBack.addActionListener(e -> onBack.run());
        left.add(btnBack);

        JLabel name = new JLabel(file.getName());
        name.setFont(UIConstants.FONT_HEADING);
        name.setForeground(UIConstants.TEXT_1);
        left.add(name);

        bar.add(left, BorderLayout.WEST);

        JButton btnExternal = ghostBtn("Abrir con app externa");
        btnExternal.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo con la app externa.");
            }
        });
        bar.add(btnExternal, BorderLayout.EAST);
        return bar;
    }

    private JButton ghostBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.FONT_SMALL);
        b.setForeground(UIConstants.TEXT_2);
        b.setBackground(UIConstants.BG_BASE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Barra de búsqueda tipo "Ctrl+F", oculta hasta que el usuario la invoca. */
    private JPanel buildFindBar() {
        findBar = new JPanel(new BorderLayout(10, 0));
        findBar.setBackground(UIConstants.BG_CARD);
        findBar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_LINE),
                new EmptyBorder(8, 24, 8, 24)));
        findBar.setVisible(false);

        findField = new JTextField();
        findField.setFont(UIConstants.FONT_BODY);
        findField.setBackground(UIConstants.BG_SURFACE);
        findField.setForeground(UIConstants.TEXT_1);
        findField.setCaretColor(UIConstants.TEXT_1);
        findField.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_LINE, 1, true),
                new EmptyBorder(4, 10, 4, 10)));

        findCounter = new JLabel(" ");
        findCounter.setFont(UIConstants.FONT_SMALL);
        findCounter.setForeground(UIConstants.TEXT_2);
        findCounter.setPreferredSize(new Dimension(90, 20));

        JButton btnPrev  = ghostBtn("‹");
        JButton btnNext  = ghostBtn("›");
        JButton btnClose = ghostBtn("Cerrar (Esc)");

        findField.addActionListener(e -> doFind(true));
        btnNext.addActionListener(e -> doFind(true));
        btnPrev.addActionListener(e -> doFind(false));
        btnClose.addActionListener(e -> hideFindBar());

        // Enter busca hacia adelante; Esc cierra la barra
        findField.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeFind");
        findField.getActionMap().put("closeFind", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { hideFindBar(); }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        right.setOpaque(false);
        right.add(findCounter);
        right.add(btnPrev);
        right.add(btnNext);
        right.add(btnClose);

        findBar.add(findField, BorderLayout.CENTER);
        findBar.add(right, BorderLayout.EAST);
        return findBar;
    }

    private void showFindBar() {
        findBar.setVisible(true);
        revalidate();
        findField.requestFocusInWindow();
        findField.selectAll();
        if (!findField.getText().isBlank()) doFind(true);
    }

    private void hideFindBar() {
        findBar.setVisible(false);
        if (currentViewer instanceof SearchableViewer sv) sv.clearHighlights();
        findCounter.setText(" ");
        revalidate();
        requestFocusInWindow();
    }

    private void doFind(boolean forward) {
        if (!(currentViewer instanceof SearchableViewer sv)) {
            findCounter.setText("No disponible");
            return;
        }
        String q = findField.getText();
        if (q.isBlank()) { findCounter.setText(" "); return; }

        boolean found = forward ? sv.findNext(q) : sv.findPrevious(q);
        findCounter.setText(found
                ? (sv.getCurrentMatchIndex() + 1) + " / " + sv.getMatchCount()
                : "Sin resultados");
    }

    private void showLoading() {
        JLabel lbl = new JLabel("Cargando vista previa...", SwingConstants.CENTER);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_2);
        swapContent(lbl);
    }

    private void showError(Throwable ex) {
        JLabel lbl = new JLabel(
                "<html><center>No se pudo generar la vista previa.<br>" + escapeMessage(ex) + "</center></html>",
                SwingConstants.CENTER);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_2);
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(lbl);
        swapContent(p);
    }

    private void swapContent(JComponent c) {
        contentHolder.removeAll();
        contentHolder.add(c, BorderLayout.CENTER);
        contentHolder.revalidate();
        contentHolder.repaint();
    }

    private String escapeMessage(Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.isBlank()) m = ex.getClass().getSimpleName();
        return m.replace("<", "&lt;").replace(">", "&gt;");
    }

    private void loadAsync(File file) {
        String ext = getExtension(file.getName());
        SwingWorker<JComponent, Void> worker = new SwingWorker<>() {
            protected JComponent doInBackground() throws Exception {
                switch (ext) {
                    case "TXT": case "LOG": case "MD": case "JSON": case "XML":
                    case "CSV": case "BAT": case "SH": case "SQL": case "PS1":
                    case "YML": case "YAML": case "INI": case "CFG":
                        return new TxtViewerPanel(file);
                    case "PDF":
                        return new PdfViewerPanel(file);
                    case "DOCX":
                        return new DocxViewerPanel(file);
                    case "XLSX":
                        return new XlsxViewerPanel(file);
                    case "PPTX":
                        return new PptxViewerPanel(file);
                    case "PNG": case "JPG": case "JPEG": case "GIF": case "BMP":
                        return new ImageViewerPanel(file);
                    default:
                        return new UnsupportedViewerPanel(file);
                }
            }

            protected void done() {
                try {
                    currentViewer = get();
                    swapContent(currentViewer);
                } catch (Exception ex) {
                    showError(ex.getCause() != null ? ex.getCause() : ex);
                }
            }
        };
        worker.execute();
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i + 1).toUpperCase() : "";
    }

    /** Libera los recursos del documento cargado (si aplica). Se llama al presionar "Regresar". */
    public void closeResources() {
        if (currentViewer instanceof ViewerCloseable vc) vc.closeResources();
    }
}
