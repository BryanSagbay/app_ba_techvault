package baustro.fin.ec.modules;

import baustro.fin.ec.dao.TaskDAO;
import baustro.fin.ec.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class TasksView extends VBox {

    private final TaskDAO dao = new TaskDAO();
    private TableView<Task> table;
    private ObservableList<Task> data;

    public TasksView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        Label title = new Label("Gestion de Tareas");
        title.getStyleClass().add("module-title");

        HBox toolbar = buildToolbar();
        table = buildTable();
        data = FXCollections.observableArrayList(dao.findAll());
        table.setItems(data);

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(title, toolbar, table);
    }

    private HBox buildToolbar() {
        final ComboBox<String> filterStatus = new ComboBox<String>(
            FXCollections.observableArrayList("TODOS", "PENDIENTE", "EN PROGRESO", "COMPLETADO", "CANCELADO"));
        filterStatus.getStyleClass().add("combo-box");
        filterStatus.setValue("TODOS");
        filterStatus.setOnAction(e -> {
            String f = filterStatus.getValue();
            if ("TODOS".equals(f)) {
                data.setAll(dao.findAll());
            } else {
                List<Task> filtered = new ArrayList<Task>();
                for (Task t : dao.findAll()) {
                    if (f.equals(t.getStatus())) filtered.add(t);
                }
                data.setAll(filtered);
            }
        });

        Button btnAdd  = new Button("+ Nueva Tarea"); btnAdd.getStyleClass().add("btn-primary");
        Button btnEdit = new Button("Editar"); btnEdit.getStyleClass().add("btn-secondary");
        Button btnDone = new Button("Completar"); btnDone.getStyleClass().add("btn-primary");
        Button btnDel  = new Button("Eliminar"); btnDel.getStyleClass().add("btn-danger");

        btnAdd.setOnAction(e  -> openForm(null));
        btnEdit.setOnAction(e -> { Task s = table.getSelectionModel().getSelectedItem(); if (s != null) openForm(s); });
        btnDone.setOnAction(e -> markDone());
        btnDel.setOnAction(e  -> deleteSelected());

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, filterStatus, spacer, btnAdd, btnEdit, btnDone, btnDel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private TableView<Task> buildTable() {
        TableView<Task> tv = new TableView<Task>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Task, String> colTitle  = col("Titulo", "title", 250);
        TableColumn<Task, String> colDesc   = col("Descripcion", "description", 200);
        TableColumn<Task, String> colDue    = col("Vence", "dueDate", 100);
        TableColumn<Task, String> colTags   = col("Tags", "tags", 120);
        TableColumn<Task, String> colDate   = col("Creado", "createdAt", 130);

        TableColumn<Task, String> colPrio = new TableColumn<Task, String>("Prioridad");
        colPrio.setCellValueFactory(new PropertyValueFactory<Task, String>("priority"));
        colPrio.setPrefWidth(90);
        colPrio.setCellFactory(tc -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(v);
                String cls;
                if ("ALTA".equals(v)) cls = "badge-red";
                else if ("MEDIA".equals(v)) cls = "badge-yellow";
                else cls = "badge-gray";
                b.getStyleClass().add(cls);
                setGraphic(b); setText(null);
            }
        });

        TableColumn<Task, String> colStatus = new TableColumn<Task, String>("Estado");
        colStatus.setCellValueFactory(new PropertyValueFactory<Task, String>("status"));
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(tc -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(v);
                String cls;
                if ("COMPLETADO".equals(v)) cls = "badge-green";
                else if ("EN PROGRESO".equals(v)) cls = "badge-blue";
                else if ("CANCELADO".equals(v)) cls = "badge-gray";
                else cls = "badge-yellow";
                b.getStyleClass().add(cls);
                setGraphic(b); setText(null);
            }
        });

        tv.getColumns().addAll(colPrio, colStatus, colTitle, colDesc, colDue, colTags, colDate);
        tv.setRowFactory(t -> {
            TableRow<Task> row = new TableRow<Task>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) openForm(row.getItem()); });
            return row;
        });
        return tv;
    }

    private TableColumn<Task, String> col(String h, String p, double w) {
        TableColumn<Task, String> c = new TableColumn<Task, String>(h);
        c.setCellValueFactory(new PropertyValueFactory<Task, String>(p));
        c.setPrefWidth(w);
        return c;
    }

    private void openForm(final Task task) {
        Dialog<Task> dialog = new Dialog<Task>();
        dialog.setTitle(task == null ? "Nueva Tarea" : "Editar Tarea");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2a2a3e;");

        final TextField fTitle = field("Titulo de la tarea");
        final TextArea  fDesc  = textarea("Descripcion detallada");
        final ComboBox<String> fPrio   = combo("ALTA", "MEDIA", "BAJA");
        final ComboBox<String> fStatus = combo("PENDIENTE", "EN PROGRESO", "COMPLETADO", "CANCELADO");
        final TextField fDue  = field("2024-12-31");
        final TextField fTags = field("banco, deployment, fix...");

        if (task != null) {
            fTitle.setText(task.getTitle()); fDesc.setText(task.getDescription());
            fPrio.setValue(task.getPriority()); fStatus.setValue(task.getStatus());
            fDue.setText(task.getDueDate()); fTags.setText(task.getTags());
        }

        int r = 0;
        grid.add(lbl("Titulo *"), 0, r); grid.add(fTitle, 1, r++);
        grid.add(lbl("Prioridad"), 0, r); grid.add(fPrio, 1, r++);
        grid.add(lbl("Estado"), 0, r); grid.add(fStatus, 1, r++);
        grid.add(lbl("Fecha limite"), 0, r); grid.add(fDue, 1, r++);
        grid.add(lbl("Tags"), 0, r); grid.add(fTags, 1, r++);
        grid.add(lbl("Descripcion"), 0, r); grid.add(fDesc, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 420);

        ButtonType btnSave   = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        dialog.setResultConverter(bt -> {
            if (bt == btnSave) {
                Task t = task == null ? new Task() : task;
                t.setTitle(fTitle.getText()); t.setDescription(fDesc.getText());
                t.setPriority(fPrio.getValue()); t.setStatus(fStatus.getValue());
                t.setDueDate(fDue.getText()); t.setTags(fTags.getText());
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            if (t.getTitle() == null || t.getTitle().trim().isEmpty()) return;
            if (task == null) dao.save(t); else dao.update(t);
            data.setAll(dao.findAll());
        });
    }

    private void markDone() {
        Task sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        sel.setStatus("COMPLETADO");
        dao.update(sel);
        data.setAll(dao.findAll());
    }

    private void deleteSelected() {
        Task sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar la tarea \"" + sel.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
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
