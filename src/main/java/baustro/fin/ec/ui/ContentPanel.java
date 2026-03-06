package baustro.fin.ec.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ContentPanel extends StackPane {

    public void setContent(Node node) {
        getChildren().clear();
        getChildren().add(node);
    }
}
