package baustro.fin.ec.modules;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class IncidentsView extends VBox {

    public IncidentsView() {

        Label title = new Label("Incidentes / Correctivos");

        getChildren().add(title);
    }
}