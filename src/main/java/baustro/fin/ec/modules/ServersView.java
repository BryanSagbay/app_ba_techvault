package baustro.fin.ec.modules;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ServersView extends VBox {

    public ServersView() {

        setSpacing(10);

        Label title = new Label("Gestión de Servidores");

        getChildren().add(title);
    }
}