package baustro.fin.ec.modules;

import baustro.fin.ec.dao.CommandDAO;
import baustro.fin.ec.model.Command;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

public class CommandsView extends VBox {

    private final CommandDAO dao = new CommandDAO();
    private TableView<Command> table;
    private ObservableList<Command> data;

    public CommandsView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        Label title = new Label("Biblioteca de Comandos");
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
        search.setPromptText("Buscar comandos...");
        search.setPrefWidth(280);
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((o, old, val) -> {
            if (val == null || val.trim().isEmpty()) data.setAll(dao.findAll());
            else data.setAll(dao.search(val));
        });

        Button btnAdd  = new Button("+ Nuevo Comando"); btnAdd.getStyleClass().add("btn-primary");
        Button btnEdit = new Button("Editar"); btnEdit.getStyleClass().add("btn-secondary");
        Button btnCopy = new Button("Copiar"); btnCopy.getStyleClass().add("btn-secondary");
        Button btnDel  = new Button("Eliminar"); btnDel.getStyleClass().add("btn-danger");

        btnAdd.setOnAction(e  -> openForm(null));
        btnEdit.setOnAction(e -> { Command s = table.getSelectionModel().getSelectedItem(); if (s != null) openForm(s); });
        btnCopy.setOnAction(e -> copyCommand());
        btnDel.setOnAction(e  -> deleteSelected());

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, search, spacer, btnAdd, btnEdit, btnCopy, btnDel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private TableView<Command> buildTable() {
        TableView<Command> tv = new TableView<Command>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Command, String> colTitle  = col("Titulo", "title", 180);
        TableColumn<Command, String> colDesc   = col("Descripcion", "description", 200);
        TableColumn<Command, String> colCat    = col("Categoria", "category", 120);

        TableColumn<Command, String> colOs = new TableColumn<Command, String>("OS");
        colOs.setCellValueFactory(new PropertyValueFactory<Command, String>("os"));
        colOs.setPrefWidth(80);
        colOs.setCellFactory(tc -> new TableCell<Command, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(v);
                String cls;
                if ("LINUX".equals(v)) cls = "badge-blue";
                else if ("WINDOWS".equals(v)) cls = "badge-green";
                else if ("AIX".equals(v)) cls = "badge-yellow";
                else cls = "badge-gray";
                b.getStyleClass().add(cls);
                setGraphic(b); setText(null);
            }
        });

        TableColumn<Command, String> colCmd = new TableColumn<Command, String>("Comando");
        colCmd.setCellValueFactory(new PropertyValueFactory<Command, String>("command"));
        colCmd.setPrefWidth(280);
        colCmd.setCellFactory(tc -> new TableCell<Command, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-family: 'Consolas', monospace; -fx-text-fill: #a6e3a1;");
            }
        });

        tv.getColumns().addAll(colTitle, colOs, colCmd, colCat, colDesc);
        tv.setRowFactory(t -> {
            TableRow<Command> row = new TableRow<Command>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) openForm(row.getItem()); });
            return row;
        });
        return tv;
    }

    private TableColumn<Command, String> col(String h, String p, double w) {
        TableColumn<Command, String> c = new TableColumn<Command, String>(h);
        c.setCellValueFactory(new PropertyValueFactory<Command, String>(p));
        c.setPrefWidth(w);
        return c;
    }

    private void copyCommand() {
        Command sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        ClipboardContent cc = new ClipboardContent();
        cc.putString(sel.getCommand());
        Clipboard.getSystemClipboard().setContent(cc);
    }

    private void openForm(final Command cmd) {
        Dialog<Command> dialog = new Dialog<Command>();
        dialog.setTitle(cmd == null ? "Nuevo Comando" : "Editar Comando");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2a2a3e;");

        final TextField fTitle = field("Nombre descriptivo del comando");
        final TextArea  fCmd   = new TextArea();
        fCmd.setPromptText("comando --flags argumentos"); fCmd.setPrefRowCount(3);
        fCmd.getStyleClass().add("text-area"); fCmd.setPrefWidth(340);
        fCmd.setStyle("-fx-font-family: 'Consolas', monospace;");
        final TextArea fDesc   = textarea("Que hace este comando?");
        final ComboBox<String> fCat = combo("GENERAL","SISTEMA","RED","PROCESOS","LOGS","BASE_DATOS","DOCKER","JAVA","GIT");
        final ComboBox<String> fOs  = combo("LINUX","WINDOWS","AIX","MULTIPLATAFORMA");
        final TextField fTags  = field("monitoreo, red, proceso...");

        if (cmd != null) {
            fTitle.setText(cmd.getTitle()); fCmd.setText(cmd.getCommand());
            fDesc.setText(cmd.getDescription()); fCat.setValue(cmd.getCategory());
            fOs.setValue(cmd.getOs()); fTags.setText(cmd.getTags());
        }

        int r = 0;
        grid.add(lbl("Titulo *"), 0, r); grid.add(fTitle, 1, r++);
        grid.add(lbl("Comando *"), 0, r); grid.add(fCmd, 1, r++);
        grid.add(lbl("OS"), 0, r); grid.add(fOs, 1, r++);
        grid.add(lbl("Categoria"), 0, r); grid.add(fCat, 1, r++);
        grid.add(lbl("Tags"), 0, r); grid.add(fTags, 1, r++);
        grid.add(lbl("Descripcion"), 0, r); grid.add(fDesc, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(540, 480);

        ButtonType btnSave   = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        dialog.setResultConverter(bt -> {
            if (bt == btnSave) {
                Command c = cmd == null ? new Command() : cmd;
                c.setTitle(fTitle.getText()); c.setCommand(fCmd.getText());
                c.setDescription(fDesc.getText()); c.setCategory(fCat.getValue());
                c.setOs(fOs.getValue()); c.setTags(fTags.getText());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            if (c.getTitle() == null || c.getTitle().trim().isEmpty()) return;
            if (cmd == null) dao.save(c); else dao.update(c);
            data.setAll(dao.findAll());
        });
    }

    private void deleteSelected() {
        Command sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar el comando \"" + sel.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { dao.delete(sel.getId()); data.setAll(dao.findAll()); }
        });
    }

    private TextField field(String p) { TextField tf = new TextField(); tf.setPromptText(p); tf.getStyleClass().add("text-field"); tf.setPrefWidth(340); return tf; }
    private TextArea textarea(String p) { TextArea ta = new TextArea(); ta.setPromptText(p); ta.setPrefRowCount(3); ta.getStyleClass().add("text-area"); ta.setPrefWidth(340); return ta; }
    private ComboBox<String> combo(String... items) { ComboBox<String> cb = new ComboBox<String>(FXCollections.observableArrayList(items)); cb.getStyleClass().add("combo-box"); cb.setValue(items[0]); cb.setPrefWidth(340); return cb; }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
}
