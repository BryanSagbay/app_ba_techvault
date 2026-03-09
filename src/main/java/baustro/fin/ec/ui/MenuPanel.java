package baustro.fin.ec.ui;

import baustro.fin.ec.modules.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class MenuPanel extends VBox {

    private Button activeBtn;

    public MenuPanel(MainView main) {
        getStyleClass().add("sidebar");
        setSpacing(2);
        setPadding(new Insets(12, 8, 12, 8));
        setPrefWidth(210);

        // App logo area
        Label logo = new Label("⚙ OPS MGR");
        logo.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 4 8 12 8;");

        // Section: Main
        Label secMain = new Label("PRINCIPAL");
        secMain.getStyleClass().add("menu-section-label");

        Button btnDash = menuBtn("🏠  Dashboard");
        btnDash.setOnAction(e -> { activate(btnDash); main.showContent(new DashboardView()); });

        // Section: Operations
        Label secOps = new Label("OPERACIONES");
        secOps.getStyleClass().add("menu-section-label");

        Button btnServers   = menuBtn("🖥  Servidores");
        Button btnIncidents = menuBtn("🔥  Incidentes");
        Button btnTasks     = menuBtn("✅  Tareas");

        btnServers.setOnAction(e   -> { activate(btnServers);   main.showContent(new ServersView()); });
        btnIncidents.setOnAction(e -> { activate(btnIncidents); main.showContent(new IncidentsView()); });
        btnTasks.setOnAction(e     -> { activate(btnTasks);     main.showContent(new TasksView()); });

        // Section: Knowledge
        Label secKnow = new Label("CONOCIMIENTO");
        secKnow.getStyleClass().add("menu-section-label");

        Button btnNotes    = menuBtn("📝  Notas");
        Button btnCommands = menuBtn("💻  Comandos");

        btnNotes.setOnAction(e    -> { activate(btnNotes);    main.showContent(new NotesView()); });
        btnCommands.setOnAction(e -> { activate(btnCommands); main.showContent(new CommandsView()); });

        // Section: Security
        Label secSec = new Label("SEGURIDAD");
        secSec.getStyleClass().add("menu-section-label");

        Button btnPasswords = menuBtn("🔐  Contraseñas");
        btnPasswords.setOnAction(e -> { activate(btnPasswords); main.showContent(new PasswordsView()); });

        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: #313244; -fx-padding: 4 0 4 0;");

        // Activate dashboard by default
        activate(btnDash);

        getChildren().addAll(
            logo,
            secMain, btnDash,
            secOps, btnServers, btnIncidents, btnTasks,
            secKnow, btnNotes, btnCommands,
            secSec, btnPasswords
        );
    }

    private Button menuBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void activate(Button btn) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("menu-btn-active");
        }
        activeBtn = btn;
        btn.getStyleClass().add("menu-btn-active");
    }
}
