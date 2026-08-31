package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/** Vista previa de XLSX: cada hoja se muestra como una tabla en una pestaña. */
public class XlsxViewerPanel extends JPanel implements ViewerCloseable, SearchableViewer {

    private final Workbook workbook;
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<JTable> sheetTables = new ArrayList<>();
    private final List<int[]> matches = new ArrayList<>(); // [sheetIndex, row, col]
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
            tabs.addTab(sheet.getSheetName(), buildSheetTable(sheet, fmt));
        }

        if (tabs.getTabCount() == 0) {
            JLabel empty = new JLabel("El archivo no tiene hojas.", SwingConstants.CENTER);
            empty.setForeground(UIConstants.TEXT_2);
            add(empty, BorderLayout.CENTER);
        } else {
            add(tabs, BorderLayout.CENTER);
        }
    }

    private JScrollPane buildSheetTable(Sheet sheet, DataFormatter fmt) {
        int maxCols = 0;
        for (Row row : sheet) {
            maxCols = Math.max(maxCols, row.getLastCellNum());
        }
        if (maxCols < 0) maxCols = 0;

        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int c = 0; c < maxCols; c++) model.addColumn(columnName(c));

        for (Row row : sheet) {
            Object[] values = new Object[maxCols];
            for (int c = 0; c < maxCols; c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                values[c] = fmt.formatCellValue(cell);
            }
            model.addRow(values);
        }

        JTable table = new JTable(model);
        table.setBackground(UIConstants.BG_CARD);
        table.setForeground(UIConstants.TEXT_1);
        table.setGridColor(UIConstants.BORDER_LINE);
        table.setRowHeight(24);
        table.setSelectionBackground(UIConstants.BG_ROW_SEL);
        table.getTableHeader().setBackground(UIConstants.BG_SURFACE);
        table.getTableHeader().setForeground(UIConstants.TEXT_2);
        sheetTables.add(table);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(UIConstants.BG_CARD);
        sp.setBorder(null);
        return sp;
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
        if (query == null || query.isBlank()) return;

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
        t.changeSelection(m[1], m[2], false, false);
        t.scrollRectToVisible(t.getCellRect(m[1], m[2], true));
    }

    @Override
    public boolean findNext(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) return false;
        current = (current + 1) % matches.size();
        goToMatch();
        return true;
    }

    @Override
    public boolean findPrevious(String query) {
        if (!query.equalsIgnoreCase(lastQuery)) recompute(query);
        if (matches.isEmpty()) return false;
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
    }
}
