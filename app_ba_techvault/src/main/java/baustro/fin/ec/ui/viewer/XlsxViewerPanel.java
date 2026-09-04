package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/** Vista previa de XLSX: cada hoja se muestra como una tabla en una pestaña. */
public class XlsxViewerPanel extends JPanel implements ViewerCloseable, SearchableViewer {

    private static final Color MATCH_COLOR = new Color(255, 235, 59);
    private static final Color MATCH_COLOR_CURRENT = new Color(255, 160, 0);

    /** Ancho mínimo y máximo (en px) permitido por columna al autoajustar según su contenido. */
    private static final int MIN_COL_WIDTH = 70;
    private static final int MAX_COL_WIDTH = 420;

    private final Workbook workbook;
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<JTable> sheetTables = new ArrayList<>();
    private final List<int[]> matches = new ArrayList<>(); // [sheetIndex, row, col]
    // Color de fondo original de cada celda del archivo, tal como viene en el Excel
    // (por hoja: [fila][columna] -> color, o null si la celda no tiene relleno).
    private final List<Color[][]> sheetCellColors = new ArrayList<>();
    private int current = -1;
    private String lastQuery = "";

    public XlsxViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        try (FileInputStream fis = new FileInputStream(file)) {
            workbook = WorkbookFactory.create(fis);
        }

        tabs.setFont(UIConstants.FONT_SMALL);
        tabs.setBackground(UIConstants.BG_CARD);
        tabs.setForeground(UIConstants.TEXT_1);

        DataFormatter fmt = new DataFormatter();
        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            Sheet sheet = workbook.getSheetAt(s);
            tabs.addTab(sheet.getSheetName(), buildSheetPage(sheet, fmt, s));
        }

        if (tabs.getTabCount() == 0) {
            JLabel empty = new JLabel("El archivo no tiene hojas.", SwingConstants.CENTER);
            empty.setForeground(UIConstants.TEXT_2);
            add(empty, BorderLayout.CENTER);
        } else {
            add(tabs, BorderLayout.CENTER);
        }
    }

    private JComponent buildSheetPage(Sheet sheet, DataFormatter fmt, int sheetIndex) {
        int maxCols = 0;
        for (Row row : sheet) {
            maxCols = Math.max(maxCols, row.getLastCellNum());
        }
        if (maxCols < 0) maxCols = 0;

        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int c = 0; c < maxCols; c++) model.addColumn(columnName(c));

        int[] colWidth = new int[maxCols];
        for (int c = 0; c < maxCols; c++) colWidth[c] = MIN_COL_WIDTH;

        List<Color[]> colorRows = new ArrayList<>();
        for (Row row : sheet) {
            Object[] values = new Object[maxCols];
            Color[] rowColors = new Color[maxCols];
            for (int c = 0; c < maxCols; c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String text = fmt.formatCellValue(cell);
                values[c] = text;
                rowColors[c] = extractCellColor(cell);
                // Ancho aproximado según la longitud del texto, para que las columnas
                // no se compriman cuando hay muchas: se calcula el ancho ideal y se
                // deja scroll horizontal en vez de forzar todo dentro del panel visible.
                int estimated = 14 + text.length() * 7;
                colWidth[c] = Math.max(colWidth[c], Math.min(estimated, MAX_COL_WIDTH));
            }
            model.addRow(values);
            colorRows.add(rowColors);
        }
        sheetCellColors.add(colorRows.toArray(new Color[0][]));

        JTable table = new JTable(model);
        table.setBackground(UIConstants.BG_CARD);
        table.setForeground(UIConstants.TEXT_1);
        table.setGridColor(UIConstants.BORDER_LINE);
        table.setRowHeight(24);
        table.setSelectionBackground(UIConstants.BG_ROW_SEL);
        table.setFillsViewportHeight(false);
        // Selección por celda (como en Excel real) en vez de seleccionar la fila
        // completa al hacer clic; arrastrando el mouse se puede marcar un rango.
        table.setCellSelectionEnabled(true);
        table.getTableHeader().setBackground(UIConstants.BG_SURFACE);
        table.getTableHeader().setForeground(UIConstants.TEXT_2);
        table.setDefaultRenderer(Object.class, new HighlightCellRenderer(sheetIndex));

        // Autoajuste de ancho de columna según contenido, sin comprimir: la tabla no
        // se reduce al ancho del viewport (autoResize OFF) y aparece scroll horizontal
        // cuando la suma de columnas supera el ancho visible.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int c = 0; c < maxCols; c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(colWidth[c]);
        }

        sheetTables.add(table);

        // La tabla vive en su propio scroll (con encabezado fijo al desplazar filas y
        // scroll horizontal para hojas con muchas columnas).
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(UIConstants.BG_CARD);
        tableScroll.setBorder(null);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // "Tarjeta" con borde propio: la hoja ya no ocupa todo el panel de lado a lado,
        // queda enmarcada y con un margen respecto al resto del visor, igual que el
        // documento Word se muestra como una página en vez de estirarse por completo.
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.BG_CARD);
        card.setBorder(new LineBorder(UIConstants.BORDER_LINE, 1));
        card.add(tableScroll, BorderLayout.CENTER);

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(UIConstants.BG_SURFACE);
        page.setBorder(new EmptyBorder(18, 18, 18, 18));
        page.add(card, BorderLayout.CENTER);

        return page;
    }

    /**
     * Obtiene el color de relleno original de la celda (tal como se ve en Excel), para
     * poder diferenciar filas/celdas resaltadas por el usuario en su archivo. Solo se
     * resuelve el color directo (RGB) definido en la celda; si el archivo usa colores de
     * tema sin RGB explícito, o no tiene relleno, se devuelve null y se pinta el fondo normal.
     */
    private Color extractCellColor(Cell cell) {
        if (cell == null) return null;
        CellStyle style = cell.getCellStyle();
        if (style == null || style.getFillPattern() != FillPatternType.SOLID_FOREGROUND) return null;

        if (style instanceof XSSFCellStyle xssfStyle) {
            XSSFColor xc = xssfStyle.getFillForegroundColorColor();
            if (xc == null) return null;
            byte[] rgb = xc.getRGB();
            if (rgb == null) return null;
            return new Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
        }
        // HSSF (.xls) u otras implementaciones: se omite el color indexado por simplicidad.
        return null;
    }

    /** Pinta de amarillo las celdas que coinciden con la búsqueda y de ámbar la actual. */
    private class HighlightCellRenderer extends DefaultTableCellRenderer {
        private final int sheetIndex;

        HighlightCellRenderer(int sheetIndex) { this.sheetIndex = sheetIndex; }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            Color originalColor = getOriginalColor(sheetIndex, row, col);
            if (isCurrentMatch(sheetIndex, row, col)) {
                c.setBackground(MATCH_COLOR_CURRENT);
                c.setForeground(Color.BLACK);
            } else if (isMatch(sheetIndex, row, col)) {
                c.setBackground(MATCH_COLOR);
                c.setForeground(Color.BLACK);
            } else if (isSelected) {
                // se conserva el color de selección por defecto de Swing
            } else if (originalColor != null) {
                // Respeta el color de relleno tal como viene en el archivo Excel, para que
                // el usuario pueda seguir distinguiendo qué filas debe completar.
                c.setBackground(originalColor);
                c.setForeground(bestTextColorFor(originalColor));
            } else {
                c.setBackground(UIConstants.BG_CARD);
                c.setForeground(UIConstants.TEXT_1);
            }
            return c;
        }
    }

    /** Color de fondo original (según el Excel) para una celda, o null si no tenía relleno. */
    private Color getOriginalColor(int sheetIndex, int row, int col) {
        if (sheetIndex < 0 || sheetIndex >= sheetCellColors.size()) return null;
        Color[][] rows = sheetCellColors.get(sheetIndex);
        if (row < 0 || row >= rows.length) return null;
        Color[] cols = rows[row];
        if (col < 0 || col >= cols.length) return null;
        return cols[col];
    }

    /** Elige texto negro o blanco según el brillo del fondo, para mantener buen contraste. */
    private Color bestTextColorFor(Color bg) {
        double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        return luminance > 0.6 ? Color.BLACK : Color.WHITE;
    }

    private boolean isMatch(int sheetIndex, int row, int col) {
        for (int[] m : matches) {
            if (m[0] == sheetIndex && m[1] == row && m[2] == col) return true;
        }
        return false;
    }

    private boolean isCurrentMatch(int sheetIndex, int row, int col) {
        if (current < 0 || current >= matches.size()) return false;
        int[] m = matches.get(current);
        return m[0] == sheetIndex && m[1] == row && m[2] == col;
    }

    private void repaintTables() {
        for (JTable t : sheetTables) t.repaint();
    }

    /** 0 -> "A", 1 -> "B", ... 26 -> "AA" ... */
    private String columnName(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    @Override
    public void closeResources() {
        try { workbook.close(); } catch (Exception ignored) {}
    }

    private void recompute(String query) {
        matches.clear();
        current = -1;
        lastQuery = query;
        if (query == null || query.isBlank()) { repaintTables(); return; }

        String q = query.toLowerCase();
        for (int s = 0; s < sheetTables.size(); s++) {
            JTable t = sheetTables.get(s);
            for (int r = 0; r < t.getRowCount(); r++) {
                for (int c = 0; c < t.getColumnCount(); c++) {
                    Object val = t.getValueAt(r, c);
                    if (val != null && val.toString().toLowerCase().contains(q)) {
                        matches.add(new int[]{s, r, c});
                    }
                }
            }
        }
    }

    private void goToMatch() {
        int[] m = matches.get(current);
        tabs.setSelectedIndex(m[0]);
        JTable t = sheetTables.get(m[0]);
        t.scrollRectToVisible(t.getCellRect(m[1], m[2], true));
        repaintTables();
    }

    @Override
    public boolean findNext(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) { repaintTables(); return false; }
        current = (current + 1) % matches.size();
        goToMatch();
        return true;
    }

    @Override
    public boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) { repaintTables(); return false; }
        current = (current - 1 + matches.size()) % matches.size();
        goToMatch();
        return true;
    }

    @Override public int getMatchCount() { return matches.size(); }
    @Override public int getCurrentMatchIndex() { return current; }

    @Override
    public void clearHighlights() {
        matches.clear();
        current = -1;
        lastQuery = "";
        repaintTables();
    }
}
