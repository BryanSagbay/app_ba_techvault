package baustro.fin.ec.ui;

import baustro.fin.ec.modules.DashboardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class MainView extends BorderPane {

    private ContentPanel contentPanel;

    public MainView() {
        getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        setTop(buildTopBar());

        MenuPanel menu = new MenuPanel(this);
        setLeft(menu);

        contentPanel = new ContentPanel();
        setCenter(contentPanel);

        // Show dashboard by default
        showContent(new DashboardView());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));

        Label appTitle = new Label("⚙  OPS MANAGER");
        appTitle.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("🔍  Buscar en servidores, incidentes, comandos...");
        search.getStyleClass().add("search-field");

        Label version = new Label("v1.0");
        version.setStyle("-fx-text-fill: #45475a; -fx-font-size: 11px;");

        bar.getChildren().addAll(appTitle, spacer, search, version);
        return bar;
    }

    public void showContent(javafx.scene.Node node) {
        contentPanel.setContent(node);
    }
}
