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

/** Vista previa de XLSX: cada hoja se muestra como una tabla en una pestaña. */
public class XlsxViewerPanel extends JPanel implements ViewerCloseable {

    private final Workbook workbook;

    public XlsxViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        try (FileInputStream fis = new FileInputStream(file)) {
            workbook = WorkbookFactory.create(fis);
        }

        JTabbedPane tabs = new JTabbedPane();
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
}
