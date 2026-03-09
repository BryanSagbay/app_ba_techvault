package baustro.fin.ec.modules;

import baustro.fin.ec.dao.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class DashboardView extends VBox {

    public DashboardView() {
        setSpacing(24);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        // Header
        Label title = new Label("🖥  Dashboard");
        title.getStyleClass().add("module-title");
        Label sub = new Label("Resumen general del sistema");
        sub.getStyleClass().add("module-subtitle");

        VBox header = new VBox(4, title, sub);

        // Stats row
        HBox stats = buildStats();

        // Recent incidents
        Label recentTitle = new Label("Actividad reciente");
        recentTitle.getStyleClass().add("form-label");
        recentTitle.setPadding(new Insets(8, 0, 4, 0));

        Label hint = new Label("Selecciona un módulo del menú lateral para comenzar.");
        hint.getStyleClass().add("module-subtitle");

        getChildren().addAll(header, stats, recentTitle, hint);
    }

    private HBox buildStats() {
        ServerDAO serverDAO = new ServerDAO();
        IncidentDAO incidentDAO = new IncidentDAO();
        TaskDAO taskDAO = new TaskDAO();

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(
            statCard("🖥", String.valueOf(serverDAO.count()), "Servidores", "stat-blue"),
            statCard("🔥", String.valueOf(incidentDAO.countByStatus("ABIERTO")), "Incidentes Abiertos", "stat-red"),
            statCard("✅", String.valueOf(taskDAO.countByStatus("COMPLETADO")), "Tareas Completadas", "stat-green"),
            statCard("⏳", String.valueOf(taskDAO.countByStatus("PENDIENTE")), "Tareas Pendientes", "stat-yellow")
        );
        return row;
    }

    private VBox statCard(String icon, String number, String label, String colorClass) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 24px;");

        Label numLbl = new Label(number);
        numLbl.getStyleClass().addAll("stat-number", colorClass);

        Label lblText = new Label(label);
        lblText.getStyleClass().add("stat-label");

        VBox card = new VBox(6, iconLbl, numLbl, lblText);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }
}
