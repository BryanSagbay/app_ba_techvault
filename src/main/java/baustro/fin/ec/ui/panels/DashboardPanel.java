package baustro.fin.ec.ui.panels;

import baustro.fin.ec.db.DatabaseManager;
import baustro.fin.ec.ui.UIConstants;
import baustro.fin.ec.ui.components.StyledComponents;
import baustro.fin.ec.util.IconManager;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BG_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JLabel title = new JLabel("TechOps Manager - Dashboard");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);
        ImageIcon _ico6 = IconManager.getIcon(IconManager.ICON_DASHBOARD, 26);
        if (_ico6 != null && _ico6.getIconWidth() > 1) title.setIcon(_ico6);
        title.setIconTextGap(10);
        header.add(title, BorderLayout.WEST);

        JLabel dbPath = StyledComponents.muted("BD: " + DatabaseManager.getDbPath());
        header.add(dbPath, BorderLayout.EAST);

        // Stats grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setBackground(UIConstants.BG_DARK);
        statsGrid.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        statsGrid.add(statCard("Correctivos", countOf("correctivos"), "Total registrados", UIConstants.ACCENT_BLUE));
        statsGrid.add(statCard("Abiertos", countWhere("correctivos", "estado='Abierto'"), "Pendientes", UIConstants.ACCENT_RED));
        statsGrid.add(statCard("Servidores", countOf("servidores"), "Inventario", UIConstants.ACCENT_CYAN));
        statsGrid.add(statCard("Tareas", countOf("tareas"), "Total tareas", UIConstants.ACCENT_GREEN));
        statsGrid.add(statCard("Pendientes", countWhere("tareas", "estado='Pendiente'"), "Sin iniciar", UIConstants.ACCENT_ORANGE));
        statsGrid.add(statCard("Contrasenas", countOf("contrasenas"), "Almacenadas", UIConstants.ACCENT_PURPLE));

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(UIConstants.BG_PANEL);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setBackground(UIConstants.BG_PANEL);
        info.setForeground(UIConstants.TEXT_SECONDARY);
        info.setFont(UIConstants.FONT_BODY);
        info.setBorder(null);
        info.setText(
            "TechOps Manager v1.0  |  Java 21 + Swing + SQLite  |  Datos almacenados 100% local\n\n" +
            "Modulos: Correctivos | Servidores & IPs | Contrasenas | Tareas | Notas | Comandos\n\n" +
            "Base de datos: " + DatabaseManager.getDbPath());

        infoPanel.add(info);

        add(header, BorderLayout.NORTH);
        add(statsGrid, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private JPanel statCard(String title, int count, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UIConstants.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(UIConstants.FONT_HEADING);
        lTitle.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel lCount = new JLabel(String.valueOf(count));
        lCount.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lCount.setForeground(accentColor);

        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(UIConstants.FONT_SMALL);
        lSub.setForeground(UIConstants.TEXT_MUTED);

        // Accent bar at bottom
        JPanel bar = new JPanel();
        bar.setBackground(accentColor);
        bar.setPreferredSize(new Dimension(0, 3));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 4));
        center.setOpaque(false);
        center.add(lCount);
        center.add(lSub);

        card.add(lTitle, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bar, BorderLayout.SOUTH);

        return card;
    }

    private int countOf(String table) {
        try (Statement s = DatabaseManager.getInstance().getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private int countWhere(String table, String where) {
        try (Statement s = DatabaseManager.getInstance().getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + where)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }
}
