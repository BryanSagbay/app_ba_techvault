package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/** Panel mostrado cuando el tipo de archivo no tiene vista previa implementada. */
public class UnsupportedViewerPanel extends JPanel {

    public UnsupportedViewerPanel(File file) {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BG_SURFACE);

        JLabel lbl = new JLabel(
                "<html><center>No hay vista previa disponible para este tipo de archivo.<br>"
                        + "Usa \"Abrir con app externa\" arriba.</center></html>",
                SwingConstants.CENTER);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_2);
        add(lbl);
    }
}
