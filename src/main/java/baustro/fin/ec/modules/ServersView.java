package baustro.fin.ec.modules;

import baustro.fin.ec.dao.ServerDAO;
import baustro.fin.ec.model.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class ServersView extends VBox {

    private final ServerDAO dao = new ServerDAO();
    private TableView<Server> table;
    private ObservableList<Server> data;

    public ServersView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        Label title = new Label("Gestion de Servidores");
        title.getStyleClass().add("module-title");

        HBox toolbar = buildToolbar();
        table = buildTable();
        data = FXCollections.observableArrayList(dao.findAll());
        table.setItems(data);

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(title, toolbar, table);
    }

    private HBox buildToolbar() {
        TextField search = new TextField();
        search.setPromptText("Buscar por nombre, IP, tags...");
        search.setPrefWidth(280);
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((o, old, val) -> {
            if (val == null || val.trim().isEmpty()) data.setAll(dao.findAll());
            else data.setAll(dao.search(val));
        });

        Button btnAdd = new Button("+ Nuevo Servidor");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> openForm(null));

        Button btnEdit = new Button("Editar");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setOnAction(e -> {
            Server sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) openForm(sel);
        });

        Button btnDel = new Button("Eliminar");
        btnDel.getStyleClass().add("btn-danger");
        btnDel.setOnAction(e -> deleteSelected());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, search, spacer, btnAdd, btnEdit, btnDel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private TableView<Server> buildTable() {
        TableView<Server> tv = new TableView<Server>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Server, String> colName = col("Nombre", "name", 180);
        TableColumn<Server, String> colIp   = col("IP", "ip", 130);
        TableColumn<Server, String> colEnv  = col("Ambiente", "environment", 100);
        TableColumn<Server, String> colOs   = col("OS", "os", 100);
        TableColumn<Server, String> colUser = col("Usuario SSH", "sshUser", 110);
        TableColumn<Server, String> colTags = col("Tags", "tags", 150);

        TableColumn<Server, String> colStatus = new TableColumn<Server, String>("Estado");
        colStatus.setCellValueFactory(new PropertyValueFactory<Server, String>("status"));
        colStatus.setPrefWidth(90);
        colStatus.setCellFactory(tc -> new TableCell<Server, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(val);
                badge.getStyleClass().add("ACTIVO".equals(val) ? "badge-green" : "badge-gray");
                setGraphic(badge);
                setText(null);
            }
        });

        tv.getColumns().addAll(colName, colIp, colEnv, colOs, colUser, colTags, colStatus);
        tv.setRowFactory(t -> {
            TableRow<Server> row = new TableRow<Server>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) openForm(row.getItem()); });
            return row;
        });
        return tv;
    }

    private TableColumn<Server, String> col(String header, String prop, double width) {
        TableColumn<Server, String> c = new TableColumn<Server, String>(header);
        c.setCellValueFactory(new PropertyValueFactory<Server, String>(prop));
        c.setPrefWidth(width);
        return c;
    }

    private void openForm(final Server server) {
        Dialog<Server> dialog = new Dialog<Server>();
        dialog.setTitle(server == null ? "Nuevo Servidor" : "Editar Servidor");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2a2a3e;");

        final TextField fName = field("Nombre del servidor");
        final TextField fIp   = field("192.168.x.x");
        final ComboBox<String> fEnv = new ComboBox<String>(FXCollections.observableArrayList("PRODUCCION","QA","DESARROLLO","STAGING"));
        fEnv.getStyleClass().add("combo-box"); fEnv.setValue("PRODUCCION"); fEnv.setPrefWidth(280);
        final TextField fOs   = field("Linux RHEL / Windows Server / AIX");
        final TextField fUser = field("usuario_ssh");
        final TextField fPort = field("22");
        final TextField fTags = field("banco, core, bdd, api...");
        final TextArea  fDesc = new TextArea();
        fDesc.setPromptText("Descripcion del servidor"); fDesc.setPrefRowCount(3); fDesc.getStyleClass().add("text-area");
        final ComboBox<String> fStatus = new ComboBox<String>(FXCollections.observableArrayList("ACTIVO","INACTIVO","MANTENIMIENTO"));
        fStatus.getStyleClass().add("combo-box"); fStatus.setValue("ACTIVO"); fStatus.setPrefWidth(280);

        if (server != null) {
            fName.setText(server.getName()); fIp.setText(server.getIp());
            fEnv.setValue(server.getEnvironment()); fOs.setText(server.getOs());
            fUser.setText(server.getSshUser()); fPort.setText(String.valueOf(server.getSshPort()));
            fTags.setText(server.getTags()); fDesc.setText(server.getDescription());
            fStatus.setValue(server.getStatus());
        }

        int r = 0;
        grid.add(lbl("Nombre *"), 0, r); grid.add(fName, 1, r++);
        grid.add(lbl("IP *"), 0, r); grid.add(fIp, 1, r++);
        grid.add(lbl("Ambiente"), 0, r); grid.add(fEnv, 1, r++);
        grid.add(lbl("Sistema Operativo"), 0, r); grid.add(fOs, 1, r++);
        grid.add(lbl("Usuario SSH"), 0, r); grid.add(fUser, 1, r++);
        grid.add(lbl("Puerto SSH"), 0, r); grid.add(fPort, 1, r++);
        grid.add(lbl("Tags"), 0, r); grid.add(fTags, 1, r++);
        grid.add(lbl("Estado"), 0, r); grid.add(fStatus, 1, r++);
        grid.add(lbl("Descripcion"), 0, r); grid.add(fDesc, 1, r++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #2a2a3e; -fx-border-color: transparent;");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefSize(520, 520);

        ButtonType btnSave   = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        dialog.setResultConverter(bt -> {
            if (bt == btnSave) {
                Server s = server == null ? new Server() : server;
                s.setName(fName.getText()); s.setIp(fIp.getText());
                s.setEnvironment(fEnv.getValue()); s.setOs(fOs.getText());
                s.setSshUser(fUser.getText());
                try { s.setSshPort(Integer.parseInt(fPort.getText())); } catch (Exception ex) { s.setSshPort(22); }
                s.setTags(fTags.getText()); s.setDescription(fDesc.getText());
                s.setStatus(fStatus.getValue());
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            if (s.getName() == null || s.getName().trim().isEmpty()) return;
            if (server == null) dao.save(s); else dao.update(s);
            data.setAll(dao.findAll());
        });
    }

    private void deleteSelected() {
        Server sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar el servidor \"" + sel.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminacion");
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { dao.delete(sel.getId()); data.setAll(dao.findAll()); }
        });
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt); tf.getStyleClass().add("text-field"); tf.setPrefWidth(280);
        return tf;
    }
    private Label lbl(String text) {
        Label l = new Label(text); l.getStyleClass().add("form-label"); return l;
    }
}
