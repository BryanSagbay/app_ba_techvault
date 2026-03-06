package baustro.fin.ec.modules;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TasksView extends VBox {

    public TasksView() {

        Label title = new Label("Gestión de Tareas");

        getChildren().add(title);
    }
}