package baustro.fin.ec.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Extrae el texto plano de un documento (PDF, DOCX, XLSX, PPTX, TXT) para
 * poder buscar dentro de su contenido. Uso "best-effort": si el archivo no
 * se puede leer (corrupto, protegido, formato inesperado) devuelve cadena
 * vacía en lugar de lanzar una excepción, para no romper la búsqueda.
 */
public final class DocumentTextExtractor {

    private DocumentTextExtractor() {}

    public static String extractText(File file) {
        String ext = getExtension(file.getName());
        try {
            return switch (ext) {
                case "TXT", "LOG", "MD", "JSON", "XML", "CSV", "BAT", "SH",
                     "SQL", "PS1", "YML", "YAML", "INI", "CFG" -> extractTxt(file);
                case "PDF"  -> extractPdf(file);
                case "DOCX" -> extractDocx(file);
                case "XLSX" -> extractXlsx(file);
                case "PPTX" -> extractPptx(file);
                default -> "";
            };
        } catch (Exception ex) {
            return "";
        }
    }

    private static String extractTxt(File file) throws Exception {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
        }
    }

    private static String extractPdf(File file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String extractDocx(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            StringBuilder sb = new StringBuilder();
            for (IBodyElement el : doc.getBodyElements()) {
                if (el instanceof XWPFParagraph p) {
                    sb.append(p.getText()).append('\n');
                } else if (el instanceof XWPFTable table) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            sb.append(cell.getText()).append(' ');
                        }
                    }
                    sb.append('\n');
                }
            }
            return sb.toString();
        }
    }

    private static String extractXlsx(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            DataFormatter fmt = new DataFormatter();
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                for (Row row : sheet) {
                    short lastCell = row.getLastCellNum();
                    for (int c = 0; c < lastCell; c++) {
                        Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        sb.append(fmt.formatCellValue(cell)).append(' ');
                    }
                    sb.append('\n');
                }
            }
            return sb.toString();
        }
    }

    private static String extractPptx(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape ts) {
                        sb.append(ts.getText()).append('\n');
                    }
                }
            }
            return sb.toString();
        }
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i + 1).toUpperCase() : "";
    }
}
