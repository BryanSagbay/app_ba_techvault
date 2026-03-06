package baustro.fin.ec.ui;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class MainView extends BorderPane {

    private ContentPanel contentPanel;

    public MainView() {

        TextField search = new TextField();
        search.setPromptText("Buscar en servidores, incidentes, comandos...");

        setTop(search);

        MenuPanel menu = new MenuPanel(this);
        setLeft(menu);

        contentPanel = new ContentPanel();
        setCenter(contentPanel);

        BorderPane.setMargin(search, new Insets(10));
    }

    public void showContent(javafx.scene.Node node) {
        contentPanel.setContent(node);
    }
}