package baustro.fin.ec.modules;

import baustro.fin.ec.dao.PasswordDAO;
import baustro.fin.ec.model.Password;
import baustro.fin.ec.util.CryptoUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

public class PasswordsView extends VBox {

    private final PasswordDAO dao = new PasswordDAO();
    private TableView<Password> table;
    private ObservableList<Password> data;

    public PasswordsView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        Label title = new Label("Gestor de Contrasenas");
        title.getStyleClass().add("module-title");
        Label warn = new Label("Las contrasenas se cifran con AES-256 y se almacenan localmente.");
        warn.getStyleClass().add("module-subtitle");
        warn.setStyle("-fx-text-fill: #f9e2af;");

        HBox toolbar = buildToolbar();
        table = buildTable();
        data = FXCollections.observableArrayList(dao.findAll());
        table.setItems(data);

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(title, warn, toolbar, table);
    }

    private HBox buildToolbar() {
        TextField search = new TextField();
        search.setPromptText("Buscar servicio, usuario...");
        search.setPrefWidth(260);
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((o, old, val) -> {
            if (val == null || val.trim().isEmpty()) data.setAll(dao.findAll());
            else data.setAll(dao.search(val));
        });

        Button btnAdd  = new Button("+ Nueva Credencial"); btnAdd.getStyleClass().add("btn-primary");
        Button btnEdit = new Button("Editar"); btnEdit.getStyleClass().add("btn-secondary");
        Button btnCopy = new Button("Copiar contrasena"); btnCopy.getStyleClass().add("btn-secondary");
        Button btnDel  = new Button("Eliminar"); btnDel.getStyleClass().add("btn-danger");

        btnAdd.setOnAction(e  -> openForm(null));
        btnEdit.setOnAction(e -> { Password s = table.getSelectionModel().getSelectedItem(); if (s != null) openForm(s); });
        btnCopy.setOnAction(e -> copyPassword());
        btnDel.setOnAction(e  -> deleteSelected());

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, search, spacer, btnAdd, btnEdit, btnCopy, btnDel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private TableView<Password> buildTable() {
        TableView<Password> tv = new TableView<Password>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Password, String> colService  = col("Servicio", "serviceName", 180);
        TableColumn<Password, String> colUser     = col("Usuario", "username", 160);
        TableColumn<Password, String> colCategory = col("Categoria", "category", 110);
        TableColumn<Password, String> colUrl      = col("URL / Host", "url", 180);
        TableColumn<Password, String> colDate     = col("Creado", "createdAt", 130);

        TableColumn<Password, String> colPwd = new TableColumn<Password, String>("Contrasena");
        colPwd.setPrefWidth(150);
        colPwd.setCellValueFactory(new PropertyValueFactory<Password, String>("passwordEncrypted"));
        colPwd.setCellFactory(tc -> new TableCell<Password, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : "**********");
            }
        });

        tv.getColumns().addAll(colService, colUser, colPwd, colCategory, colUrl, colDate);
        tv.setRowFactory(t -> {
            TableRow<Password> row = new TableRow<Password>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) openForm(row.getItem()); });
            return row;
        });
        return tv;
    }

    private TableColumn<Password, String> col(String h, String p, double w) {
        TableColumn<Password, String> c = new TableColumn<Password, String>(h);
        c.setCellValueFactory(new PropertyValueFactory<Password, String>(p));
        c.setPrefWidth(w);
        return c;
    }

    private void copyPassword() {
        Password sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String plain = CryptoUtil.decrypt(sel.getPasswordEncrypted());
        ClipboardContent cc = new ClipboardContent();
        cc.putString(plain);
        Clipboard.getSystemClipboard().setContent(cc);
        Alert info = new Alert(Alert.AlertType.INFORMATION,
            "Contrasena copiada al portapapeles.\nRecuerda limpiar el portapapeles despues.");
        info.setTitle("Contrasena copiada");
        info.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        info.showAndWait();
    }

    private void openForm(final Password password) {
        Dialog<Password> dialog = new Dialog<Password>();
        dialog.setTitle(password == null ? "Nueva Credencial" : "Editar Credencial");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2a2a3e;");

        final TextField fService = field("Nombre del servicio / sistema");
        final TextField fUser    = field("usuario@dominio");
        final PasswordField fPwd = new PasswordField();
        fPwd.setPromptText("Contrasena"); fPwd.getStyleClass().add("password-field"); fPwd.setPrefWidth(240);
        final TextField fPwdVisible = field("Contrasena visible");
        fPwdVisible.setVisible(false); fPwdVisible.setManaged(false);

        final Button toggleBtn = new Button("Mostrar");
        toggleBtn.getStyleClass().add("btn-secondary");
        toggleBtn.setOnAction(e -> {
            boolean showing = fPwdVisible.isVisible();
            fPwdVisible.setVisible(!showing); fPwdVisible.setManaged(!showing);
            fPwd.setVisible(showing); fPwd.setManaged(showing);
            if (!showing) { fPwdVisible.setText(fPwd.getText()); }
            else { fPwd.setText(fPwdVisible.getText()); }
            toggleBtn.setText(showing ? "Mostrar" : "Ocultar");
        });

        HBox pwdRow = new HBox(8, fPwd, fPwdVisible, toggleBtn);

        final TextField fUrl   = field("192.168.x.x o https://...");
        final ComboBox<String> fCat = combo("GENERAL","SERVIDORES","BASES_DE_DATOS","APLICACIONES","VPN","BANCO");
        final TextArea fNotes  = textarea("Notas adicionales");

        if (password != null) {
            fService.setText(password.getServiceName());
            fUser.setText(password.getUsername());
            String plain = CryptoUtil.decrypt(password.getPasswordEncrypted());
            fPwd.setText(plain); fPwdVisible.setText(plain);
            fUrl.setText(password.getUrl());
            fCat.setValue(password.getCategory());
            fNotes.setText(password.getNotes());
        }

        int r = 0;
        grid.add(lbl("Servicio *"), 0, r); grid.add(fService, 1, r++);
        grid.add(lbl("Usuario"), 0, r); grid.add(fUser, 1, r++);
        grid.add(lbl("Contrasena *"), 0, r); grid.add(pwdRow, 1, r++);
        grid.add(lbl("URL / Host"), 0, r); grid.add(fUrl, 1, r++);
        grid.add(lbl("Categoria"), 0, r); grid.add(fCat, 1, r++);
        grid.add(lbl("Notas"), 0, r); grid.add(fNotes, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(520, 440);

        ButtonType btnSave   = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        dialog.setResultConverter(bt -> {
            if (bt == btnSave) {
                Password p = password == null ? new Password() : password;
                p.setServiceName(fService.getText());
                p.setUsername(fUser.getText());
                String rawPwd = fPwdVisible.isVisible() ? fPwdVisible.getText() : fPwd.getText();
                p.setPasswordEncrypted(CryptoUtil.encrypt(rawPwd));
                p.setUrl(fUrl.getText());
                p.setCategory(fCat.getValue());
                p.setNotes(fNotes.getText());
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            if (p.getServiceName() == null || p.getServiceName().trim().isEmpty()) return;
            if (password == null) dao.save(p); else dao.update(p);
            data.setAll(dao.findAll());
        });
    }

    private void deleteSelected() {
        Password sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar las credenciales de \"" + sel.getServiceName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { dao.delete(sel.getId()); data.setAll(dao.findAll()); }
        });
    }

    private TextField field(String p) { TextField tf = new TextField(); tf.setPromptText(p); tf.getStyleClass().add("text-field"); tf.setPrefWidth(280); return tf; }
    private TextArea textarea(String p) { TextArea ta = new TextArea(); ta.setPromptText(p); ta.setPrefRowCount(3); ta.getStyleClass().add("text-area"); ta.setPrefWidth(280); return ta; }
    private ComboBox<String> combo(String... items) { ComboBox<String> cb = new ComboBox<String>(FXCollections.observableArrayList(items)); cb.getStyleClass().add("combo-box"); cb.setValue(items[0]); cb.setPrefWidth(280); return cb; }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
}
