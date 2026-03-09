package baustro.fin.ec.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentPanel extends StackPane {

    public ContentPanel() {
        setStyle("-fx-background-color: #1e1e2e;");
    }

    public void setContent(Node node) {
        getChildren().clear();
        getChildren().add(node);
    }
}
