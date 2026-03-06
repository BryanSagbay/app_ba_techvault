package baustro.fin.ec.ui;

import baustro.fin.ec.modules.IncidentsView;
import baustro.fin.ec.modules.ServersView;
import baustro.fin.ec.modules.TasksView;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


public class MenuPanel extends VBox {

    public MenuPanel(MainView main) {

        setSpacing(10);
        setPadding(new Insets(10));
        setPrefWidth(200);

        Button servers = new Button("Servidores");
        Button incidents = new Button("Incidentes");
        Button tasks = new Button("Tareas");

        servers.setMaxWidth(Double.MAX_VALUE);
        incidents.setMaxWidth(Double.MAX_VALUE);
        tasks.setMaxWidth(Double.MAX_VALUE);

        servers.setOnAction(e -> main.showContent(new ServersView()));
        incidents.setOnAction(e -> main.showContent(new IncidentsView()));
        tasks.setOnAction(e -> main.showContent(new TasksView()));

        getChildren().addAll(servers, incidents, tasks);
    }
}