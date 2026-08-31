package baustro.fin.ec.ui.viewer;

import baustro.fin.ec.ui.UIConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** Vista previa de imágenes (PNG, JPG, GIF, BMP) usando javax.imageio. */
public class ImageViewerPanel extends JPanel {

    public ImageViewerPanel(File file) throws Exception {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SURFACE);

        BufferedImage img = ImageIO.read(file);
        if (img == null) throw new Exception("Formato de imagen no soportado.");

        JLabel lbl = new JLabel(new ImageIcon(img));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIConstants.BG_SURFACE);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrapper.add(lbl);

        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIConstants.BG_SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }
}
