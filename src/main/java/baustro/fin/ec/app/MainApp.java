package baustro.fin.ec.app;
import baustro.fin.ec.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        MainView root = new MainView();

        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("Ops Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}