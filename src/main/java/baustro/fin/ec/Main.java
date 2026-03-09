package baustro.fin.ec;

import baustro.fin.ec.database.DBConnection;
import baustro.fin.ec.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Asegurar que el directorio data/ existe
        new File("data").mkdirs();

        // Inicializar base de datos y crear tablas si no existen
        DBConnection.initDatabase();

        MainView root = new MainView();
        Scene scene = new Scene(root, 1200, 760);

        stage.setTitle("OPS Manager — Área TI");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
